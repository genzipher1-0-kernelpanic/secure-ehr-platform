package com.genzipher.identityservice.DTO;

import com.genzipher.identityservice.Model.RoleName;

public record RegisterResponse(
        Long id,
        String email,
        String password,
        RoleName role
) {}
