package com.team.ehr.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CryptoServiceTest {

    @Test
    void encryptDecryptJsonRoundTrip() {
        String masterKey = Base64.getEncoder().encodeToString(new byte[32]);
        CryptoService cryptoService = new CryptoService(masterKey, "test");
        String payload = "{\"a\":1,\"b\":\"test\"}";

        CryptoResult encrypted = cryptoService.encryptJson(payload);
        assertNotNull(encrypted.getCiphertext());
        String decrypted = cryptoService.decryptJson(encrypted.getCiphertext());

        assertEquals(payload, decrypted);
    }

    @Test
    void encryptDecryptFileRoundTrip() throws Exception {
        String masterKey = Base64.getEncoder().encodeToString(new byte[32]);
        CryptoService cryptoService = new CryptoService(masterKey, "test");
        byte[] data = "lab-binary-data".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        EncryptedFileResult result = cryptoService.encryptFile(new ByteArrayInputStream(data), encryptedOut);

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(encryptedOut.toByteArray());
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
        cryptoService.decryptFile(encryptedIn, decryptedOut, result.getEncryptedDataKey());

        assertEquals("lab-binary-data", decryptedOut.toString(StandardCharsets.UTF_8));
    }
}
