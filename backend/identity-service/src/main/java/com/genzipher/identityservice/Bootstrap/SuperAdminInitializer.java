package com.genzipher.identityservice.Bootstrap;

import com.genzipher.identityservice.Config.SuperAdminProperties;
import com.genzipher.identityservice.Model.Role;
import com.genzipher.identityservice.Model.RoleName;
import com.genzipher.identityservice.Model.User;
import com.genzipher.identityservice.Model.UserStatus;
import com.genzipher.identityservice.Repository.RoleRepository;
import com.genzipher.identityservice.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Component
public class SuperAdminInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminProperties props;

    public SuperAdminInitializer(RoleRepository roleRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 SuperAdminProperties props) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    private static final Logger log =
            LoggerFactory.getLogger(SuperAdminInitializer.class);


    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 1) Ensure roles exist
        for (RoleName rn : EnumSet.allOf(RoleName.class)) {
            roleRepository.findByName(rn).orElseGet(() ->
                    roleRepository.save(Role.builder().name(rn).build())
            );
        }

        // 2) If a SUPER_ADMIN already exists, do nothing
        long superAdminCount = userRepository.countUsersWithRole(RoleName.SUPER_ADMIN);
        if (superAdminCount > 0) return;

        // 3) Validate bootstrap credentials
        if (props.email() == null || props.email().isBlank()
                || props.password() == null || props.password().isBlank()) {
            throw new IllegalStateException("""
                No SUPER_ADMIN exists yet, but bootstrap.super-admin.email/password are not set.
                Set them via config-server or environment variables to create the initial SUPER_ADMIN.
                """);
        }

        // Extra safety: don’t create if email is already used (even if not super admin)
        if (userRepository.existsByEmail(props.email())) {
            throw new IllegalStateException("bootstrap.super-admin.email already exists but is not SUPER_ADMIN. Resolve manually.");
        }

        Role superAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role missing"));

        // 4) Create the one and only SUPER_ADMIN
        User u = User.builder()
                .email(props.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(props.password()))
                .status(UserStatus.ACTIVE)
                .build();

        if (u.getRoles() == null) {
            u.setRoles(new java.util.HashSet<>());
        }
        u.getRoles().add(superAdminRole);

        userRepository.save(u);

        // 🚨 VERY IMPORTANT LOG — PRINTED ONLY ON FIRST BOOTSTRAP
        log.warn("""
        ============================================================
        🚨 SUPER ADMIN CREATED 🚨
        Email    : {}
        Password : {}
        ⚠️  CHANGE THIS PASSWORD IMMEDIATELY AFTER FIRST LOGIN
        ============================================================
        """, props.email(), props.password());
    }

}
