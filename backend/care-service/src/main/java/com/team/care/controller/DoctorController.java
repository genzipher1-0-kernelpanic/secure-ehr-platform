package com.team.care.controller;

import com.team.care.dto.DoctorProfileDto;
import com.team.care.dto.PatientProfileDto;
import com.team.care.service.DoctorPatientService;
import com.team.care.service.ProfileService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/care/doctors")
public class DoctorController {

    private final ProfileService profileService;
    private final DoctorPatientService doctorPatientService;

    public DoctorController(ProfileService profileService,
                            DoctorPatientService doctorPatientService) {
        this.profileService = profileService;
        this.doctorPatientService = doctorPatientService;
    }

    @GetMapping("/{doctorId}")
    public DoctorProfileDto getDoctor(@PathVariable Long doctorId) {
        return profileService.getDoctor(doctorId);
    }

    @GetMapping("/{doctorUserId}/patients")
    public List<PatientProfileDto> getAssignedPatients(@PathVariable Long doctorUserId) {
        return doctorPatientService.listAssignedPatients(doctorUserId);
    }
}
