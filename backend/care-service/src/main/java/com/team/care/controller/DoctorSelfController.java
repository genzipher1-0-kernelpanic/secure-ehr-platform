package com.team.care.controller;

import com.team.care.dto.DoctorProfileDto;
import com.team.care.service.ProfileService;
import com.team.care.service.exception.NotFoundException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
public class DoctorSelfController {
    private final ProfileService profileService;

    public DoctorSelfController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public DoctorProfileDto getMyProfile() {
        List<DoctorProfileDto> doctors = profileService.listDoctors();
        if (doctors.isEmpty()) {
            throw new NotFoundException("Doctor not found");
        }
        return doctors.get(0);
    }
}
