package com.genzipher.identityservice.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @Email @NotBlank String email,
        @NotBlank String code,
        @NotBlank String newPassword,
        @NotBlank String confirmPassword
) {}
