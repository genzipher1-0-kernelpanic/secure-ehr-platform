package com.team.ehr.controller;

import com.team.ehr.dto.LabMetaRequest;
import com.team.ehr.dto.LabObjectDto;
import com.team.ehr.dto.LabUploadResponse;
import com.team.ehr.entity.EhrLabObject;
import com.team.ehr.service.LabService;
import com.team.ehr.crypto.CryptoService;
import com.team.ehr.storage.StorageService;
import com.team.ehr.exception.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/ehr/patients/{patientId}/labs")
public class LabController {

    private static final Logger log = LoggerFactory.getLogger(LabController.class);

    private final LabService labService;
    private final StorageService storageService;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public LabController(LabService labService,
                         StorageService storageService,
                         CryptoService cryptoService,
                         ObjectMapper objectMapper,
                         Validator validator) {
        this.labService = labService;
        this.storageService = storageService;
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LabUploadResponse upload(
            @PathVariable Long patientId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("meta") String metaJson
    ) {
        log.info("Lab upload patientId={} filename={} size={}",
                patientId, file.getOriginalFilename(), file.getSize());
        LabMetaRequest meta = parseMeta(metaJson);
        return labService.upload(patientId, file, meta);
    }

    @GetMapping
    public List<LabObjectDto> list(@PathVariable Long patientId) {
        log.info("Lab list patientId={}", patientId);
        return labService.list(patientId);
    }

    @GetMapping("/{objectId}/download")
    public ResponseEntity<StreamingResponseBody> download(
            @PathVariable Long patientId,
            @PathVariable String objectId
    ) {
        log.info("Lab download patientId={} objectId={}", patientId, objectId);
        EhrLabObject lab = labService.getLabObjectForDownload(patientId, objectId);
        StreamingResponseBody body = outputStream -> streamDecrypted(lab, outputStream);
        String filename = sanitizeFilename(lab.getTitle(), lab.getMimeType());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(lab.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(body);
    }

    private void streamDecrypted(EhrLabObject lab, OutputStream outputStream) throws IOException {
        try (InputStream inputStream = storageService.open(lab.getObjectPath())) {
            cryptoService.decryptFile(inputStream, outputStream, lab.getEncryptedDataKey());
        }
    }

    private LabMetaRequest parseMeta(String metaJson) {
        try {
            LabMetaRequest meta = objectMapper.readValue(metaJson, LabMetaRequest.class);
            Set<ConstraintViolation<LabMetaRequest>> violations = validator.validate(meta);
            if (!violations.isEmpty()) {
                throw new BadRequestException("Invalid meta payload");
            }
            return meta;
        } catch (Exception ex) {
            throw new BadRequestException("Invalid meta payload");
        }
    }

    private String sanitizeFilename(String title, String mimeType) {
        if (title == null || title.isBlank()) {
            return "lab-report" + extensionForMime(mimeType);
        }
        String safe = title.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safe.length() > 120) {
            safe = safe.substring(0, 120);
        }
        if (!safe.contains(".")) {
            safe = safe + extensionForMime(mimeType);
        }
        return safe;
    }

    private String extensionForMime(String mimeType) {
        if (MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(mimeType)) {
            return ".pdf";
        }
        if (MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(mimeType)) {
            return ".png";
        }
        return ".bin";
    }
}
