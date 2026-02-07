package com.team.ehr.storage;

public class StorageResult {

    private final String objectPath;
    private final byte[] encryptedDataKey;
    private final String hashHex;
    private final long sizeBytes;

    public StorageResult(String objectPath, byte[] encryptedDataKey, String hashHex, long sizeBytes) {
        this.objectPath = objectPath;
        this.encryptedDataKey = encryptedDataKey;
        this.hashHex = hashHex;
        this.sizeBytes = sizeBytes;
    }

    public String getObjectPath() {
        return objectPath;
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
