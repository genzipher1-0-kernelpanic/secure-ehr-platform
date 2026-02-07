package com.team.care.service;

import com.team.care.dto.ConsentCreateRequest;
import com.team.care.entity.Consent;
import com.team.care.repository.ConsentRepository;
import com.team.care.repository.DoctorRepository;
import com.team.care.repository.PatientRepository;
import com.team.care.service.exception.NotFoundException;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public ConsentService(ConsentRepository consentRepository,
                          PatientRepository patientRepository,
                          DoctorRepository doctorRepository) {
        this.consentRepository = consentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public Long createConsent(ConsentCreateRequest request) {
        if (!patientRepository.existsById(request.getPatientId())) {
            throw new NotFoundException("Patient not found");
        }
        if (!doctorRepository.existsByUserId(request.getGranteeUserId())) {
            throw new NotFoundException("Doctor not found");
        }
        Consent consent = new Consent();
        consent.setPatientId(request.getPatientId());
        consent.setGranteeUserId(request.getGranteeUserId());
        consent.setScope(request.getScope());
        if (request.getValidTo() != null) {
            consent.setValidTo(request.getValidTo().toInstant());
        }
        return consentRepository.save(consent).getId();
    }

    @Transactional
    public void revokeConsent(Long consentId) {
        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new NotFoundException("Consent not found"));
        if (consent.getRevokedAt() == null) {
            consent.setRevokedAt(Instant.now());
            consentRepository.save(consent);
        }
    }
}
