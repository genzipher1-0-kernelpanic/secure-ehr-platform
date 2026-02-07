package com.team.ehr.crypto;

public class CryptoResult {

    private final byte[] ciphertext;
    private final String hashHex;

    public CryptoResult(byte[] ciphertext, String hashHex) {
        this.ciphertext = ciphertext;
        this.hashHex = hashHex;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

    public String getHashHex() {
        return hashHex;
    }
}
