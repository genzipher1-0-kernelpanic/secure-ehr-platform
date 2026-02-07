package com.team.ehr.crypto;

public class EncryptedFileResult {

    private final byte[] encryptedDataKey;
    private final String hashHex;
    private final long sizeBytes;

    public EncryptedFileResult(byte[] encryptedDataKey, String hashHex, long sizeBytes) {
        this.encryptedDataKey = encryptedDataKey;
        this.hashHex = hashHex;
        this.sizeBytes = sizeBytes;
    }

    public byte[] getEncryptedDataKey() {
        return encryptedDataKey;
    }

    public String getHashHex() {
        return hashHex;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }
}
