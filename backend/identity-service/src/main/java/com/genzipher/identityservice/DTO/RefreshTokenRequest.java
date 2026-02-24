package com.genzipher.identityservice.DTO;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank String refreshToken

) {
}
