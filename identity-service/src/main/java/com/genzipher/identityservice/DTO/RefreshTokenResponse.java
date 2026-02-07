package com.genzipher.identityservice.DTO;

public record RefreshTokenResponse(

        String accessToken,
        String refreshToken

) {
}
