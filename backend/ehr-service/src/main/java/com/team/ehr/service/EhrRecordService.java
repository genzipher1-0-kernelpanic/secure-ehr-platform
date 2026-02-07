package com.team.ehr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.team.ehr.crypto.CryptoResult;
import com.team.ehr.crypto.CryptoService;
import com.team.ehr.dto.EhrCreateRequest;
import com.team.ehr.dto.EhrCreateResponse;
import com.team.ehr.dto.EhrPatientResponse;
import com.team.ehr.dto.EhrRecordDto;
import com.team.ehr.dto.EhrUpdateRequest;
import com.team.ehr.dto.EhrUpdateResponse;
import com.team.ehr.dto.EhrVersionDto;
import com.team.ehr.entity.EhrCategory;
import com.team.ehr.entity.EhrRecordCurrent;
import com.team.ehr.entity.EhrRecordVersion;
import com.team.ehr.exception.BadRequestException;
import com.team.ehr.exception.ConflictException;
import com.team.ehr.exception.ForbiddenException;
import com.team.ehr.exception.NotFoundException;
import com.team.ehr.repository.EhrRecordCurrentRepository;
import com.team.ehr.repository.EhrRecordVersionRepository;
import com.team.ehr.security.SecurityUtil;
import com.team.ehr.security.UserRole;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class EhrRecordService {

    private final EhrRecordCurrentRepository currentRepository;
    private final EhrRecordVersionRepository versionRepository;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;
    private final AccessControlService accessControlService;
    private final AuditService auditService;

    public EhrRecordService(EhrRecordCurrentRepository currentRepository,
                            EhrRecordVersionRepository versionRepository,
                            CryptoService cryptoService,
                            ObjectMapper objectMapper,
                            AccessControlService accessControlService,
                            AuditService auditService) {
        this.currentRepository = currentRepository;
        this.versionRepository = versionRepository;
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper;
        this.accessControlService = accessControlService;
        this.auditService = auditService;
    }

    public EhrPatientResponse readPatient(Long patientId, EhrCategory category, Integer version) {
        if (category == null && version != null) {
            throw new BadRequestException("category is required when requesting a version");
        }
        if (category != null) {
            accessControlService.assertCanRead(patientId, category);
            EhrRecordDto record = readSingle(patientId, category, version);
            EhrPatientResponse response = new EhrPatientResponse();
            response.setPatientId(patientId);
            if (category == EhrCategory.CLINICAL) {
                response.setClinical(record);
            } else {
                response.setTreatments(record);
            }
            return response;
        }

        accessControlService.assertCanRead(patientId, EhrCategory.CLINICAL);
        accessControlService.assertCanRead(patientId, EhrCategory.TREATMENTS);
        EhrPatientResponse response = new EhrPatientResponse();
        response.setPatientId(patientId);
        response.setClinical(readSingle(patientId, EhrCategory.CLINICAL, null));
        response.setTreatments(readSingle(patientId, EhrCategory.TREATMENTS, null));
        return response;
    }

    public List<EhrVersionDto> listVersions(Long patientId, EhrCategory category) {
        if (category == null) {
            accessControlService.assertCanRead(patientId, EhrCategory.CLINICAL);
            accessControlService.assertCanRead(patientId, EhrCategory.TREATMENTS);
        } else {
            accessControlService.assertCanRead(patientId, category);
        }
        List<EhrRecordVersion> versions = category == null
                ? versionRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                : versionRepository.findByPatientIdAndCategoryOrderByVersionDesc(patientId, category);
        return versions.stream().map(this::toVersionDto).toList();
    }

    @Transactional
    public EhrCreateResponse createRecord(Long patientId, EhrCreateRequest request) {
        EhrCategory category = request.getCategory();
        accessControlService.assertCanUpdate(patientId, category);
        validatePatchPolicy(category, request.getData());
        if (currentRepository.existsByPatientIdAndCategory(patientId, category)) {
            throw new ConflictException("EHR record already exists");
        }
        if (!request.getData().isObject()) {
            throw new BadRequestException("EHR payload must be a JSON object");
        }

        try {
            String json = objectMapper.writeValueAsString(request.getData());
            CryptoResult encrypted = cryptoService.encryptJson(json);
            int version = 1;

            EhrRecordCurrent current = new EhrRecordCurrent();
            current.setPatientId(patientId);
            current.setCategory(category);
            current.setCurrentVersion(version);
            current.setCiphertext(encrypted.getCiphertext());
            current.setContentHash(encrypted.getHashHex());
            current.setKeyId(cryptoService.getKeyId());
            currentRepository.save(current);

            EhrRecordVersion recordVersion = new EhrRecordVersion();
            recordVersion.setEhrId(current.getId());
            recordVersion.setPatientId(patientId);
            recordVersion.setCategory(category);
            recordVersion.setVersion(version);
            recordVersion.setCiphertext(encrypted.getCiphertext());
            recordVersion.setContentHash(encrypted.getHashHex());
            recordVersion.setKeyId(cryptoService.getKeyId());
            recordVersion.setCreatedByUserId(SecurityUtil.getUserId());
            recordVersion.setCreatedByRole(SecurityUtil.getRole().name());
            versionRepository.save(recordVersion);

            auditService.log("CREATE", patientId, category, current.getId(), null, version);
            return new EhrCreateResponse(version);
        } catch (Exception ex) {
            throw new BadRequestException("Unable to create record");
        }
    }

    @Transactional
    public EhrUpdateResponse updateRecord(Long patientId, EhrUpdateRequest request) {
        EhrCategory category = request.getCategory();
        accessControlService.assertCanUpdate(patientId, category);
        validatePatchPolicy(category, request.getPatch());
        EhrRecordCurrent current = currentRepository.findByPatientIdAndCategory(patientId, category)
                .orElseThrow(() -> new NotFoundException("EHR record not found"));
        if (!current.getCurrentVersion().equals(request.getExpectedVersion())) {
            throw new ConflictException("Version mismatch");
        }

        try {
            ObjectNode existing = parseJsonObject(cryptoService.decryptJson(current.getCiphertext()));
            ObjectNode patched = applyPatch(existing, request.getPatch());
            String updatedJson = objectMapper.writeValueAsString(patched);
            CryptoResult encrypted = cryptoService.encryptJson(updatedJson);
            int newVersion = current.getCurrentVersion() + 1;

            current.setCiphertext(encrypted.getCiphertext());
            current.setContentHash(encrypted.getHashHex());
            current.setCurrentVersion(newVersion);
            current.setKeyId(cryptoService.getKeyId());
            currentRepository.save(current);

            EhrRecordVersion version = new EhrRecordVersion();
            version.setEhrId(current.getId());
            version.setPatientId(patientId);
            version.setCategory(category);
            version.setVersion(newVersion);
            version.setCiphertext(encrypted.getCiphertext());
            version.setContentHash(encrypted.getHashHex());
            version.setKeyId(cryptoService.getKeyId());
            version.setCreatedByUserId(SecurityUtil.getUserId());
            version.setCreatedByRole(SecurityUtil.getRole().name());
            versionRepository.save(version);

            auditService.log("UPDATE", patientId, category, current.getId(), null, newVersion);
            return new EhrUpdateResponse(newVersion);
        } catch (OptimisticLockingFailureException ex) {
            throw new ConflictException("Concurrent update detected");
        } catch (Exception ex) {
            throw new BadRequestException("Unable to update record");
        }
    }

    private EhrRecordDto readSingle(Long patientId, EhrCategory category, Integer version) {
        if (version != null) {
            EhrRecordVersion versionEntity = versionRepository
                    .findByPatientIdAndCategoryAndVersion(patientId, category, version)
                    .orElseThrow(() -> new NotFoundException("Version not found"));
            return toRecordDto(versionEntity);
        }
        EhrRecordCurrent current = currentRepository.findByPatientIdAndCategory(patientId, category)
                .orElseThrow(() -> new NotFoundException("EHR record not found"));
        return toRecordDto(current);
    }

    private EhrRecordDto toRecordDto(EhrRecordCurrent current) {
        EhrRecordDto dto = new EhrRecordDto();
        dto.setCategory(current.getCategory());
        dto.setVersion(current.getCurrentVersion());
        dto.setUpdatedAt(current.getUpdatedAt());
        dto.setData(parseJson(cryptoService.decryptJson(current.getCiphertext())));
        return dto;
    }

    private EhrRecordDto toRecordDto(EhrRecordVersion version) {
        EhrRecordDto dto = new EhrRecordDto();
        dto.setCategory(version.getCategory());
        dto.setVersion(version.getVersion());
        dto.setUpdatedAt(version.getCreatedAt());
        dto.setData(parseJson(cryptoService.decryptJson(version.getCiphertext())));
        return dto;
    }

    private EhrVersionDto toVersionDto(EhrRecordVersion version) {
        EhrVersionDto dto = new EhrVersionDto();
        dto.setVersion(version.getVersion());
        dto.setCategory(version.getCategory());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setCreatedByRole(version.getCreatedByRole());
        return dto;
    }

    private ObjectNode parseJsonObject(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isObject()) {
                throw new BadRequestException("EHR payload must be an object");
            }
            return (ObjectNode) node;
        } catch (Exception ex) {
            throw new BadRequestException("Invalid JSON payload");
        }
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid JSON payload");
        }
    }

    private ObjectNode applyPatch(ObjectNode existing, JsonNode patch) {
        if (!patch.isObject()) {
            throw new BadRequestException("Patch must be a JSON object");
        }
        try {
            return (ObjectNode) objectMapper.readerForUpdating(existing).readValue(patch);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid patch payload");
        }
    }

    private void validatePatchPolicy(EhrCategory category, JsonNode patch) {
        UserRole role = SecurityUtil.getRole();
        if (!patch.isObject()) {
            throw new BadRequestException("Patch must be a JSON object");
        }
        Set<String> allowed;
        if (role == UserRole.PATIENT) {
            if (category == EhrCategory.TREATMENTS) {
                throw new ForbiddenException("Patients cannot update treatments");
            }
            allowed = Set.of("emergencyContact", "address", "allergies");
        } else {
            if (category == EhrCategory.CLINICAL) {
                allowed = Set.of("conditions", "vitals", "clinicalNotes", "allergies");
            } else {
                allowed = Set.of("medications", "procedures", "carePlans");
            }
        }
        for (String field : iterableFields(patch)) {
            if (!allowed.contains(field)) {
                throw new ForbiddenException("Field not allowed: " + field);
            }
        }
    }

    private Iterable<String> iterableFields(JsonNode patch) {
        return () -> patch.fieldNames();
    }
}
