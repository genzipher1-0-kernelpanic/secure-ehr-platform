package com.team.ehr.service;

import com.team.ehr.dto.EhrExportResponse;
import com.team.ehr.dto.LabObjectDto;
import com.team.ehr.entity.EhrCategory;
import com.team.ehr.exception.TooManyRequestsException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

    private final AccessControlService accessControlService;
    private final EhrRecordService ehrRecordService;
    private final LabService labService;
    private final RateLimiterService rateLimiterService;
    private final AuditService auditService;

    public ExportService(AccessControlService accessControlService,
                         EhrRecordService ehrRecordService,
                         LabService labService,
                         RateLimiterService rateLimiterService,
                         AuditService auditService) {
        this.accessControlService = accessControlService;
        this.ehrRecordService = ehrRecordService;
        this.labService = labService;
        this.rateLimiterService = rateLimiterService;
        this.auditService = auditService;
    }

    public EhrExportResponse export(Long patientId, boolean sessionFresh) {
        accessControlService.assertCanExport(patientId);
        Long userId = 0L;
        if (!rateLimiterService.tryConsume("user:" + userId)
                || !rateLimiterService.tryConsume("patient:" + patientId)) {
            throw new TooManyRequestsException("Rate limit exceeded");
        }

        EhrExportResponse response = new EhrExportResponse();
        response.setPatientId(patientId);

        List<LabObjectDto> labs = labService.list(patientId);
        response.setLabs(labs);

        if (accessControlService.canRead(patientId, EhrCategory.CLINICAL)) {
            response.setClinical(ehrRecordService.readPatient(patientId, EhrCategory.CLINICAL, null).getClinical());
        }
        if (accessControlService.canRead(patientId, EhrCategory.TREATMENTS)) {
            response.setTreatments(ehrRecordService.readPatient(patientId, EhrCategory.TREATMENTS, null).getTreatments());
        }

        auditService.log("EXPORT", patientId, null, null, null, null);
        return response;
    }
}
