package com.team.ehr.service;

import com.team.ehr.dto.LabMetaRequest;
import com.team.ehr.dto.LabObjectDto;
import com.team.ehr.dto.LabUploadResponse;
import com.team.ehr.entity.EhrLabObject;
import com.team.ehr.exception.BadRequestException;
import com.team.ehr.exception.NotFoundException;
import com.team.ehr.repository.EhrLabObjectRepository;
import com.team.ehr.repository.EhrRecordCurrentRepository;
import com.team.ehr.storage.StorageResult;
import com.team.ehr.storage.StorageService;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LabService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE
    );

    private final EhrLabObjectRepository labRepository;
    private final EhrRecordCurrentRepository currentRepository;
    private final StorageService storageService;
    private final AccessControlService accessControlService;
    private final AuditService auditService;

    public LabService(EhrLabObjectRepository labRepository,
                      EhrRecordCurrentRepository currentRepository,
                      StorageService storageService,
                      AccessControlService accessControlService,
                      AuditService auditService) {
        this.labRepository = labRepository;
        this.currentRepository = currentRepository;
        this.storageService = storageService;
        this.accessControlService = accessControlService;
        this.auditService = auditService;
    }

    @Transactional
    public LabUploadResponse upload(Long patientId, MultipartFile file, LabMetaRequest meta) {
        accessControlService.assertCanAccessLabs(patientId);
        validateMimeType(file.getContentType());
        String objectId = UUID.randomUUID().toString();

        try (InputStream inputStream = file.getInputStream()) {
            StorageResult storageResult = storageService.saveLabFile(patientId, objectId, inputStream);
            EhrLabObject lab = new EhrLabObject();
            lab.setObjectId(objectId);
            lab.setPatientId(patientId);
            lab.setObjectPath(storageResult.getObjectPath());
            lab.setEncryptedDataKey(storageResult.getEncryptedDataKey());
            lab.setFileHash(storageResult.getHashHex());
            lab.setMimeType(file.getContentType());
            lab.setSizeBytes(storageResult.getSizeBytes());
            lab.setCreatedByUserId(0L);
            lab.setCreatedByRole("SYSTEM");
            lab.setReportType(meta.getReportType());
            lab.setTitle(meta.getTitle());
            lab.setStudyDate(meta.getStudyDate());
            if (meta.getRelatedCategory() != null) {
                Long relatedEhrId = currentRepository
                        .findByPatientIdAndCategory(patientId, meta.getRelatedCategory())
                        .map(record -> record.getId())
                        .orElse(null);
                lab.setRelatedEhrId(relatedEhrId);
            }
            lab.setRelatedVersion(meta.getRelatedVersion());
            labRepository.save(lab);

            auditService.log("ATTACHMENT_UPLOAD", patientId, meta.getRelatedCategory(), null, objectId, null);
            return new LabUploadResponse(objectId);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to upload lab");
        }
    }

    public List<LabObjectDto> list(Long patientId) {
        accessControlService.assertCanAccessLabs(patientId);
        return labRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toDto)
                .toList();
    }

    public LabObjectDto getMetadata(Long patientId, String objectId) {
        accessControlService.assertCanAccessLabs(patientId);
        EhrLabObject lab = labRepository.findByPatientIdAndObjectId(patientId, objectId)
                .orElseThrow(() -> new NotFoundException("Lab object not found"));
        return toDto(lab);
    }

    public EhrLabObject getLabObject(Long patientId, String objectId) {
        accessControlService.assertCanAccessLabs(patientId);
        return labRepository.findByPatientIdAndObjectId(patientId, objectId)
                .orElseThrow(() -> new NotFoundException("Lab object not found"));
    }

    public EhrLabObject getLabObjectForDownload(Long patientId, String objectId) {
        EhrLabObject lab = getLabObject(patientId, objectId);
        auditService.log("ATTACHMENT_DOWNLOAD", patientId, null, null, objectId, null);
        return lab;
    }

    private LabObjectDto toDto(EhrLabObject lab) {
        LabObjectDto dto = new LabObjectDto();
        dto.setObjectId(lab.getObjectId());
        dto.setReportType(lab.getReportType());
        dto.setTitle(lab.getTitle());
        dto.setStudyDate(lab.getStudyDate());
        dto.setMimeType(lab.getMimeType());
        dto.setSizeBytes(lab.getSizeBytes());
        dto.setCreatedAt(lab.getCreatedAt());
        dto.setRelatedVersion(lab.getRelatedVersion());
        return dto;
    }

    private void validateMimeType(String mimeType) {
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new BadRequestException("Unsupported file type");
        }
    }
}
