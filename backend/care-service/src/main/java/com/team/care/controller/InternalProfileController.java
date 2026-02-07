package com.team.care.controller;

import com.team.care.dto.InternalProfileCreateRequest;
import com.team.care.dto.ProfileCreateResponse;
import com.team.care.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/profiles")
public class InternalProfileController {

    private final ProfileService profileService;

    public InternalProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<ProfileCreateResponse> createProfile(
            @Valid @RequestBody InternalProfileCreateRequest request
    ) {
        Long id = profileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProfileCreateResponse(id));
    }
}
