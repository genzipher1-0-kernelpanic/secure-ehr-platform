package com.genzipher.identityservice.DTO;

import com.genzipher.identityservice.Model.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotNull RoleName role
) {}