package com.team.care.controller;

import com.team.care.dto.PatientProfileDto;
import com.team.care.service.ProfileService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/care/patients")
public class PatientController {

    private final ProfileService profileService;

    public PatientController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{patientId}")
    public PatientProfileDto getPatient(@PathVariable Long patientId) {
        return profileService.getPatient(patientId);
    }

    @GetMapping
    public List<PatientProfileDto> listPatients() {
        return profileService.listPatients();
    }
}
