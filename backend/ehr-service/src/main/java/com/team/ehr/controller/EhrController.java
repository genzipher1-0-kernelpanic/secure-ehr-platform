package com.team.ehr.controller;

import com.team.ehr.dto.EhrPatientResponse;
import com.team.ehr.dto.EhrUpdateRequest;
import com.team.ehr.dto.EhrUpdateResponse;
import com.team.ehr.dto.EhrVersionDto;
import com.team.ehr.dto.EhrExportResponse;
import com.team.ehr.dto.EhrCreateRequest;
import com.team.ehr.dto.EhrCreateResponse;
import com.team.ehr.entity.EhrCategory;
import com.team.ehr.service.EhrRecordService;
import com.team.ehr.service.ExportService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ehr/patients")
public class EhrController {

    private final EhrRecordService ehrRecordService;
    private final ExportService exportService;

    public EhrController(EhrRecordService ehrRecordService, ExportService exportService) {
        this.ehrRecordService = ehrRecordService;
        this.exportService = exportService;
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("isAuthenticated()")
    public EhrPatientResponse readPatient(
            @PathVariable Long patientId,
            @RequestParam(required = false) EhrCategory category,
            @RequestParam(required = false) Integer version
    ) {
        return ehrRecordService.readPatient(patientId, category, version);
    }

    @PatchMapping("/{patientId}")
    @PreAuthorize("isAuthenticated()")
    public EhrUpdateResponse updatePatient(
            @PathVariable Long patientId,
            @Valid @RequestBody EhrUpdateRequest request
    ) {
        return ehrRecordService.updateRecord(patientId, request);
    }

    @PostMapping("/{patientId}")
    @PreAuthorize("isAuthenticated()")
    public EhrCreateResponse createPatientRecord(
            @PathVariable Long patientId,
            @Valid @RequestBody EhrCreateRequest request
    ) {
        return ehrRecordService.createRecord(patientId, request);
    }

    @GetMapping("/{patientId}/versions")
    @PreAuthorize("isAuthenticated()")
    public List<EhrVersionDto> versions(
            @PathVariable Long patientId,
            @RequestParam(required = false) EhrCategory category
    ) {
        return ehrRecordService.listVersions(patientId, category);
    }

    @PostMapping("/{patientId}/export")
    @PreAuthorize("isAuthenticated()")
    public EhrExportResponse export(
            @PathVariable Long patientId,
            @RequestHeader(value = "X-Session-Fresh", required = false) String sessionFresh
    ) {
        boolean fresh = "true".equalsIgnoreCase(sessionFresh);
        return exportService.export(patientId, fresh);
    }
}
