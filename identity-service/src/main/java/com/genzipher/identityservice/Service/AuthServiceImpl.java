package com.genzipher.identityservice.Service;

import com.genzipher.identityservice.Bootstrap.SuperAdminInitializer;
import com.genzipher.identityservice.DTO.*;
import com.genzipher.identityservice.Model.Role;
import com.genzipher.identityservice.Model.RoleName;
import com.genzipher.identityservice.Model.Token;
import com.genzipher.identityservice.Model.TokenType;
import com.genzipher.identityservice.Repository.PasswordResetCodeRepository;
import com.genzipher.identityservice.Repository.RoleRepository;
import com.genzipher.identityservice.Repository.TokenRepository;
import com.genzipher.identityservice.Repository.UserRepository;
import com.genzipher.identityservice.Util.TokenHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService{

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final MailService mailService;

    @Value("${app.password-reset.code-ttl-seconds:300}")
    private long resetCodeTtlSeconds;

    private static final String LOWER =
            "abcdefghjkmnpqrstuvwxyz"; // removed l, i, o

    private static final String UPPER =
            "ABCDEFGHJKMNPQRSTUVWXYZ"; // removed I, O

    private static final String DIGITS =
            "23456789"; // removed 0, 1

    private static final String SYMBOLS =
            "@#$%^&*()-_=+?"; // removed !

    private static final String SYMBOL_SET = "@#$%^&*()-_=+?";


    private static final Logger log =
            LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           TokenRepository tokenRepository,
                           JwtService jwtService,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, PasswordResetCodeRepository passwordResetCodeRepository, MailService mailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.mailService = mailService;
    }

    // role priority (highest wins)
    private static final Map<RoleName, Integer> ROLE_PRIORITY = new EnumMap<>(RoleName.class);
    static {
        ROLE_PRIORITY.put(RoleName.SUPER_ADMIN, 5);
        ROLE_PRIORITY.put(RoleName.SYSTEM_ADMIN, 4);
        ROLE_PRIORITY.put(RoleName.ADMIN, 3);
        ROLE_PRIORITY.put(RoleName.DOCTOR, 2);
        ROLE_PRIORITY.put(RoleName.PATIENT, 1);
    }

    private void validateNewPasswordOrThrow(String pw, String confirm) {
        if (!pw.equals(confirm)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (pw.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        boolean hasUpper = pw.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = pw.chars().anyMatch(Character::isDigit);
        boolean hasSymbol = pw.chars().mapToObj(c -> (char) c).anyMatch(ch -> SYMBOL_SET.indexOf(ch) >= 0);

        if (!hasUpper || !hasDigit || !hasSymbol) {
            throw new IllegalArgumentException("Password must contain 1 uppercase, 1 number, and 1 symbol");
        }
    }

    @Override
    @Transactional
    public RegisterResponse register(Authentication authentication, RegisterRequest request) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("Not authenticated");
        }

        // Who is creating?
        String creatorEmail = authentication.getName();
        var creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new IllegalStateException("Creator user not found: " + creatorEmail));

        RoleName creatorHighestRole = creator.getRoles().stream()
                .map(Role::getName)
                .max((a, b) -> Integer.compare(
                        ROLE_PRIORITY.getOrDefault(a, 0),
                        ROLE_PRIORITY.getOrDefault(b, 0)
                ))
                .orElseThrow(() -> new IllegalStateException("Creator has no roles"));

        RoleName targetRole = request.role();

        // SUPER_ADMIN must not be created via this endpoint
        if (targetRole == RoleName.SUPER_ADMIN) {
            throw new IllegalArgumentException("SUPER_ADMIN cannot be registered via this endpoint");
        }

        // Enforce who can create what
        switch (creatorHighestRole) {
            case SUPER_ADMIN -> {
                // can create anything except SUPER_ADMIN (already checked)
            }
            case SYSTEM_ADMIN -> {
                if (targetRole == RoleName.SYSTEM_ADMIN) {
                    throw new org.springframework.security.access.AccessDeniedException("SYSTEM_ADMIN cannot create SYSTEM_ADMIN");
                }
            }
            case ADMIN -> {
                if (!(targetRole == RoleName.DOCTOR || targetRole == RoleName.PATIENT)) {
                    throw new org.springframework.security.access.AccessDeniedException("ADMIN can only create DOCTOR or PATIENT");
                }
            }
            default -> {
                // DOCTOR / PATIENT / others
                throw new org.springframework.security.access.AccessDeniedException("You are not allowed to register users");
            }
        }

        // Enforce limits (global)
        if (targetRole == RoleName.SYSTEM_ADMIN) {
            long systemAdminCount = userRepository.countUsersWithRole(RoleName.SYSTEM_ADMIN);
            if (systemAdminCount >= 3) {
                throw new IllegalStateException("SYSTEM_ADMIN limit reached (3)");
            }
        }

        if (targetRole == RoleName.ADMIN) {
            long adminCount = userRepository.countUsersWithRole(RoleName.ADMIN);
            if (adminCount >= 10) {
                throw new IllegalStateException("ADMIN limit reached (10)");
            }
        }

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role roleEntity = roleRepository.findByName(targetRole)
                .orElseThrow(() -> new IllegalStateException("Role missing in DB: " + targetRole));

        String rawPassword = generatePassword();

        var newUser = com.genzipher.identityservice.Model.User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(com.genzipher.identityservice.Model.UserStatus.ACTIVE)
                .build();

        newUser.getRoles().add(roleEntity);

        var saved = userRepository.save(newUser);

        return new RegisterResponse(saved.getId(), saved.getEmail(), rawPassword, targetRole);
    }


    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        // 1) Authenticate (checks password via your UserDetailsService + PasswordEncoder)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        // 2) Load user entity (for roles/status/token persistence) and revoke all previous refresh tokens
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User missing after authentication: " + email));

        Instant now = Instant.now();

        int revoked = tokenRepository.revokeAllActiveTokenPairs(user.getId(), now);
        if (revoked > 0) {
            log.info("Revoked {} existing token pairs for user {}", revoked, email);
        }

        // 3) Compute "role" (highest role if multiple)
        RoleName highestRole = user.getRoles().stream()
                .map(r -> r.getName())
                .max((a, b) -> Integer.compare(
                        ROLE_PRIORITY.getOrDefault(a, 0),
                        ROLE_PRIORITY.getOrDefault(b, 0)
                ))
                .orElseThrow();

        // 4) JWT claims (include roles if you want downstream services to trust the token)
        Map<String, Object> accessClaims = Map.of(
                "roles", user.getRoles().stream().map(r -> r.getName().name()).toList(),
                "role", highestRole.name()
        );

        String accessToken = jwtService.generateAccessToken(email,user.getId(), accessClaims);

        // Refresh token usually carries minimal claims
        String refreshToken = jwtService.generateRefreshToken(email,user.getId(), Map.of("type", "refresh"));

        // 5) Persist hashed refresh token
        Instant accessExp  = jwtService.extractExpiration(accessToken);
        Instant refreshExp = jwtService.extractExpiration(refreshToken);

        Token tokenPair = Token.builder()
                .user(user)
                .accessTokenHash(TokenHasher.sha512(accessToken))
                .refreshTokenHash(TokenHasher.sha512(refreshToken))
                .accessExpiresAt(accessExp)
                .refreshExpiresAt(refreshExp)
                .build();

        tokenRepository.save(tokenPair);

        return new LoginResponse(accessToken, refreshToken, highestRole.name());
    }

    @Override
    public TokenValidationResponse validateToken(Authentication authentication, String authorization) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return new TokenValidationResponse(false, null, null);
        }

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return new TokenValidationResponse(false, null, null);
        }

        String accessToken = authorization.substring("Bearer ".length()).trim();
        if (accessToken.isEmpty()) {
            return new TokenValidationResponse(false, null, null);
        }

        String email = authentication.getName();
        var user = userRepository.findWithRolesByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User missing after authentication: " + email));

        byte[] accessHash = TokenHasher.sha512(accessToken);
        Instant now = Instant.now();

        boolean valid = tokenRepository
                .findActiveAccessToken(user.getId(), accessHash, now)
                .isPresent();

        if (!valid) {
            return new TokenValidationResponse(false, null, null);
        }

        // Highest role (same logic as login)
        RoleName highestRole = user.getRoles().stream()
                .map(Role::getName)
                .max((a, b) -> Integer.compare(
                        ROLE_PRIORITY.getOrDefault(a, 0),
                        ROLE_PRIORITY.getOrDefault(b, 0)
                ))
                .orElse(null);

        return new TokenValidationResponse(
                true,
                user.getId(),
                highestRole != null ? highestRole.name() : null
        );
    }

    @Override
    @Transactional
    public void logout(String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return; // nothing to revoke
        }

        String accessToken = authorizationHeader.substring("Bearer ".length()).trim();
        if (accessToken.isEmpty()) return;

        Long userId = jwtService.extractUserId(accessToken);
        if (userId == null) return;

        Instant now = Instant.now();

        // Revoke BOTH access + refresh tokens (all active sessions/token-pairs)
        tokenRepository.revokeAllActiveTokenPairs(userId, now);

    }

    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        List<Character> chars = new ArrayList<>();

        // Mandatory characters
        chars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        chars.add(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));

        String all = LOWER + UPPER + DIGITS + SYMBOLS;

        // Fill remaining slots
        while (chars.size() < 8) {
            chars.add(all.charAt(random.nextInt(all.length())));
        }

        // Shuffle for randomness
        Collections.shuffle(chars, random);

        StringBuilder password = new StringBuilder();
        for (char c : chars) {
            password.append(c);
        }

        return password.toString();
    }

    @Override
    @Transactional
    public void deleteUser(Authentication authentication, Long userId) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Not authenticated");
        }

        // Who is deleting?
        String actorEmail = authentication.getName();
        var actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new IllegalStateException("Actor user not found: " + actorEmail));

        RoleName actorHighestRole = actor.getRoles().stream()
                .map(Role::getName)
                .max((a, b) -> Integer.compare(
                        ROLE_PRIORITY.getOrDefault(a, 0),
                        ROLE_PRIORITY.getOrDefault(b, 0)
                ))
                .orElseThrow(() -> new IllegalStateException("Actor has no roles"));

        // Who is being deleted?
        var target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        RoleName targetHighestRole = target.getRoles().stream()
                .map(Role::getName)
                .max((a, b) -> Integer.compare(
                        ROLE_PRIORITY.getOrDefault(a, 0),
                        ROLE_PRIORITY.getOrDefault(b, 0)
                ))
                .orElseThrow(() -> new IllegalStateException("Target has no roles"));

        // SUPER_ADMIN cannot be deleted (ever)
        if (targetHighestRole == RoleName.SUPER_ADMIN) {
            throw new AccessDeniedException("SUPER_ADMIN cannot be deleted");
        }

        // Enforce deletion permissions (same ladder as registration)
        switch (actorHighestRole) {
            case SUPER_ADMIN -> {
                // can delete anyone except SUPER_ADMIN (already blocked)
            }
            case SYSTEM_ADMIN -> {
                if (!(targetHighestRole == RoleName.ADMIN
                        || targetHighestRole == RoleName.DOCTOR
                        || targetHighestRole == RoleName.PATIENT)) {
                    throw new AccessDeniedException("SYSTEM_ADMIN can only delete ADMIN, DOCTOR, PATIENT");
                }
            }
            case ADMIN -> {
                if (!(targetHighestRole == RoleName.DOCTOR || targetHighestRole == RoleName.PATIENT)) {
                    throw new AccessDeniedException("ADMIN can only delete DOCTOR or PATIENT");
                }
            }
            default -> throw new AccessDeniedException("You are not allowed to delete users");
        }

        // Optional safety: prevent self-delete (recommended)
        if (actor.getId().equals(target.getId())) {
            throw new AccessDeniedException("You cannot delete your own account");
        }

        // Kill sessions first
        Instant now = Instant.now();
        tokenRepository.revokeAllActiveTokenPairs(target.getId(), now);

        // Hard delete user (will cascade delete user_roles + tokens due to FK ON DELETE CASCADE)
        userRepository.delete(target);
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        String refreshToken = request.refreshToken().trim();
        if (refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token missing");
        }

        // 1) Extract identity from JWT
        Long userId = jwtService.extractUserId(refreshToken);
        String email = jwtService.extractUsername(refreshToken);

        if (userId == null || email == null) {
            throw new AccessDeniedException("Invalid refresh token");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        // 2) Verify refresh token exists in DB and is active
        byte[] refreshHash = TokenHasher.sha512(refreshToken);
        Instant now = Instant.now();

        Token tokenPair = tokenRepository
                .findActiveRefreshToken(userId, refreshHash, now)
                .orElseThrow(() -> new AccessDeniedException("Refresh token revoked or expired"));

        // 3) Revoke old pair (rotation)
        tokenPair.setAccessRevokedAt(now);
        tokenPair.setRefreshRevokedAt(now);

        // 4) Issue new tokens
        RoleName highestRole = user.getRoles().stream()
                .map(Role::getName)
                .max((a, b) -> Integer.compare(
                        ROLE_PRIORITY.getOrDefault(a, 0),
                        ROLE_PRIORITY.getOrDefault(b, 0)
                ))
                .orElseThrow();

        Map<String, Object> accessClaims = Map.of(
                "roles", user.getRoles().stream().map(r -> r.getName().name()).toList(),
                "role", highestRole.name()
        );

        String newAccessToken = jwtService.generateAccessToken(
                user.getEmail(), user.getId(), accessClaims
        );

        String newRefreshToken = jwtService.generateRefreshToken(
                user.getEmail(), user.getId(), Map.of("type", "refresh")
        );

        Instant accessExp  = jwtService.extractExpiration(newAccessToken);
        Instant refreshExp = jwtService.extractExpiration(newRefreshToken);

        Token newPair = Token.builder()
                .user(user)
                .accessTokenHash(TokenHasher.sha512(newAccessToken))
                .refreshTokenHash(TokenHasher.sha512(newRefreshToken))
                .accessExpiresAt(accessExp)
                .refreshExpiresAt(refreshExp)
                .build();

        tokenRepository.save(newPair);

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        String email = request.email().trim().toLowerCase();
        var userOpt = userRepository.findByEmail(email);

        // Important: do not leak whether an email exists
        if (userOpt.isEmpty()) {
            return;
        }

        var user = userOpt.get();
        Instant now = Instant.now();

        // Invalidate any previous unused codes
        passwordResetCodeRepository.markAllUnusedAsUsed(user.getId(), now);

        // Generate 6-digit numeric code (easy to type)
        SecureRandom rnd = new SecureRandom();
        String code = String.format("%06d", rnd.nextInt(1_000_000));

        var prc = com.genzipher.identityservice.Model.PasswordResetCode.builder()
                .user(user)
                .codeHash(TokenHasher.sha512(code))
                .expiresAt(now.plusSeconds(resetCodeTtlSeconds))
                .build();

        passwordResetCodeRepository.save(prc);

        mailService.sendPasswordResetCode(user.getEmail(), code);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        String email = request.email().trim().toLowerCase();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Invalid code or expired"));

        String code = request.code().trim();
        if (code.isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("Invalid code or expired");
        }

        validateNewPasswordOrThrow(request.newPassword(), request.confirmPassword());

        Instant now = Instant.now();
        byte[] codeHash = TokenHasher.sha512(code);

        var prc = passwordResetCodeRepository.findValidCode(user.getId(), codeHash, now)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Invalid code or expired"));

        // Mark used
        prc.setUsedAt(now);

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));

        // Kill all sessions
        tokenRepository.revokeAllActiveTokenPairs(user.getId(), now);

        // Save changes
        // (user is managed; prc is managed)
    }


}
