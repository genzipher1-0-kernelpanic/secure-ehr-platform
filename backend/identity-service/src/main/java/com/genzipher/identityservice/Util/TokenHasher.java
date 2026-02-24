package com.genzipher.identityservice.Util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class TokenHasher {

    private TokenHasher() {}

    public static byte[] sha512(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            return md.digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 not available", e);
        }
    }

}
