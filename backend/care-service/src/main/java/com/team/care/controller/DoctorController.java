package com.team.care.controller;

import com.team.care.dto.DoctorProfileDto;
import com.team.care.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/care/doctors")
public class DoctorController {

    private final ProfileService profileService;

    public DoctorController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{doctorId}")
    public DoctorProfileDto getDoctor(@PathVariable Long doctorId) {
        return profileService.getDoctor(doctorId);
    }
}
