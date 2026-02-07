package com.genzipher.identityservice.Service;

import com.genzipher.identityservice.DTO.*;
import org.springframework.security.core.Authentication;

public interface AuthService {

    RegisterResponse register(Authentication authentication, RegisterRequest request);

    LoginResponse login(LoginRequest request);

    TokenValidationResponse validateToken(Authentication authentication, String authorization);

    void logout(String authorizationHeader);

    void deleteUser(Authentication authentication, Long userId);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);


}
