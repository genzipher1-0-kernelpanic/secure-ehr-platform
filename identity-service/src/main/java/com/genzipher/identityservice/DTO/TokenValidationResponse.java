package com.genzipher.identityservice.DTO;

public record TokenValidationResponse(

        boolean valid,
        Long user_id,
        String role

) {
}
