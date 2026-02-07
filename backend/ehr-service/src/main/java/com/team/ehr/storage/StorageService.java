package com.team.ehr.storage;

import com.team.ehr.crypto.CryptoService;
import com.team.ehr.crypto.EncryptedFileResult;
import com.team.ehr.exception.BadRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StorageService {

    private final Path baseDir;
    private final CryptoService cryptoService;

    public StorageService(@Value("${storage.baseDir}") String baseDir, CryptoService cryptoService) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
        this.cryptoService = cryptoService;
    }

    public StorageResult saveLabFile(Long patientId, String objectId, InputStream inputStream) throws IOException {
        Path patientDir = safeResolve(baseDir, "patient", String.valueOf(patientId), "lab");
        Files.createDirectories(patientDir);
        Path finalPath = safeResolve(patientDir, objectId + ".bin");
        Path tempPath = safeResolve(patientDir, objectId + ".tmp");

        try (OutputStream out = Files.newOutputStream(tempPath)) {
            EncryptedFileResult encrypted = cryptoService.encryptFile(inputStream, out);
            out.flush();
            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            long size = Files.size(finalPath);
            return new StorageResult(finalPath.toString(), encrypted.getEncryptedDataKey(), encrypted.getHashHex(), size);
        } catch (IOException ex) {
            Files.deleteIfExists(tempPath);
            throw ex;
        }
    }

    public InputStream open(String objectPath) throws IOException {
        Path path = Path.of(objectPath).toAbsolutePath().normalize();
        if (!path.startsWith(baseDir)) {
            throw new BadRequestException("Invalid object path");
        }
        return Files.newInputStream(path);
    }

    private Path safeResolve(Path base, String... parts) {
        Path resolved = base;
        for (String part : parts) {
            resolved = resolved.resolve(part);
        }
        Path normalized = resolved.normalize();
        if (!normalized.startsWith(base)) {
            throw new BadRequestException("Invalid storage path");
        }
        return normalized;
    }
}
