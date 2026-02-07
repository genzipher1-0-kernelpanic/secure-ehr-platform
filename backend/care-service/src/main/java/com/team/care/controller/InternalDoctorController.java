package com.team.care.controller;

import com.team.care.dto.DoctorProfileDto;
import com.team.care.service.ProfileService;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/internal/doctors")
public class InternalDoctorController {

    private final ProfileService profileService;

    public InternalDoctorController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public DoctorProfileDto getDoctorByUserId(@PathVariable @NotNull Long userId) {
        return profileService.getDoctorByUserId(userId);
    }
}
