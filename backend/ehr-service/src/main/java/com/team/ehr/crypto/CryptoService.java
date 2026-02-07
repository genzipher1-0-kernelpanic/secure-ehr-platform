package com.team.ehr.crypto;

import com.team.ehr.exception.BadRequestException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final SecretKey masterKey;
    private final String keyId;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoService(@Value("${crypto.masterKeyBase64}") String masterKeyBase64,
                         @Value("${crypto.keyId}") String keyId) {
        byte[] decoded = Base64.getDecoder().decode(masterKeyBase64);
        if (decoded.length < 32) {
            throw new IllegalArgumentException("crypto.masterKeyBase64 must decode to at least 32 bytes");
        }
        this.masterKey = new SecretKeySpec(decoded, "AES");
        this.keyId = keyId;
    }

    public CryptoResult encryptJson(String plaintextJson) {
        try {
            byte[] iv = randomIv();
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plaintext = plaintextJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintext);
            byte[] full = concat(iv, ciphertext);
            return new CryptoResult(full, sha256Hex(full));
        } catch (Exception ex) {
            throw new BadRequestException("Encryption failed");
        }
    }

    public String decryptJson(byte[] ciphertext) {
        try {
            byte[] iv = slice(ciphertext, 0, IV_BYTES);
            byte[] payload = slice(ciphertext, IV_BYTES, ciphertext.length - IV_BYTES);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plaintext = cipher.doFinal(payload);
            return new String(plaintext, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BadRequestException("Decryption failed");
        }
    }

    public EncryptedFileResult encryptFile(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] dataKey = randomKey();
            byte[] encryptedDataKey = encryptDataKey(dataKey);
            byte[] iv = randomIv();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            out.write(iv);
            digest.update(iv);
            DigestOutputStream digestOut = new DigestOutputStream(out, digest);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(dataKey, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            try (CipherOutputStream cipherOut = new CipherOutputStream(digestOut, cipher)) {
                long size = in.transferTo(cipherOut);
                cipherOut.flush();
                byte[] hash = digest.digest();
                return new EncryptedFileResult(encryptedDataKey, toHex(hash), size + iv.length);
            }
        } catch (Exception ex) {
            throw new IOException("File encryption failed", ex);
        }
    }

    public void decryptFile(InputStream in, OutputStream out, byte[] encryptedDataKey) throws IOException {
        try {
            byte[] dataKey = decryptDataKey(encryptedDataKey);
            byte[] iv = in.readNBytes(IV_BYTES);
            if (iv.length != IV_BYTES) {
                throw new IOException("Invalid ciphertext");
            }
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(dataKey, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            try (CipherInputStream cipherIn = new CipherInputStream(in, cipher)) {
                cipherIn.transferTo(out);
            }
        } catch (Exception ex) {
            throw new IOException("File decryption failed", ex);
        }
    }

    public String getKeyId() {
        return keyId;
    }

    private byte[] encryptDataKey(byte[] dataKey) throws Exception {
        byte[] iv = randomIv();
        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] cipherText = cipher.doFinal(dataKey);
        return concat(iv, cipherText);
    }

    private byte[] decryptDataKey(byte[] encryptedDataKey) throws Exception {
        byte[] iv = slice(encryptedDataKey, 0, IV_BYTES);
        byte[] payload = slice(encryptedDataKey, IV_BYTES, encryptedDataKey.length - IV_BYTES);
        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
        return cipher.doFinal(payload);
    }

    private byte[] randomIv() {
        byte[] iv = new byte[IV_BYTES];
        secureRandom.nextBytes(iv);
        return iv;
    }

    private byte[] randomKey() {
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        return key;
    }

    private String sha256Hex(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return toHex(hash);
    }

    private String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    private byte[] slice(byte[] data, int start, int length) {
        byte[] out = new byte[length];
        System.arraycopy(data, start, out, 0, length);
        return out;
    }
}
