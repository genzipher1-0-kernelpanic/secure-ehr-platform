package com.team.care.service;

import com.team.care.dto.DoctorProfileDto;
import com.team.care.dto.InternalProfileCreateRequest;
import com.team.care.dto.PatientProfileDto;
import com.team.care.dto.Role;
import com.team.care.entity.Doctor;
import com.team.care.entity.Patient;
import com.team.care.repository.DoctorRepository;
import com.team.care.repository.PatientRepository;
import com.team.care.service.exception.BadRequestException;
import com.team.care.service.exception.ConflictException;
import com.team.care.service.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public ProfileService(PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public Long createProfile(InternalProfileCreateRequest request) {
        if (request.getRole() == Role.PATIENT) {
            if (request.getPatientProfile() == null || request.getDoctorProfile() != null) {
                throw new BadRequestException("Patient profile payload required for PATIENT role");
            }
            if (patientRepository.existsByUserId(request.getUserId())) {
                throw new ConflictException("Patient profile already exists for userId");
            }
            PatientProfileDto profile = request.getPatientProfile();
            Patient patient = new Patient();
            patient.setUserId(request.getUserId());
            patient.setFullName(profile.getFullName());
            patient.setDob(profile.getDob());
            patient.setSex(profile.getSex());
            patient.setPhone(profile.getPhone());
            patient.setAddress(profile.getAddress());
            patient.setEmergencyContact(profile.getEmergencyContact());
            return patientRepository.save(patient).getId();
        }

        if (request.getRole() == Role.DOCTOR) {
            if (request.getDoctorProfile() == null || request.getPatientProfile() != null) {
                throw new BadRequestException("Doctor profile payload required for DOCTOR role");
            }
            if (doctorRepository.existsByUserId(request.getUserId())) {
                throw new ConflictException("Doctor profile already exists for userId");
            }
            DoctorProfileDto profile = request.getDoctorProfile();
            Doctor doctor = new Doctor();
            doctor.setUserId(request.getUserId());
            doctor.setFullName(profile.getFullName());
            doctor.setSpecialization(profile.getSpecialization());
            doctor.setLicenseNumber(profile.getLicenseNumber());
            doctor.setPhone(profile.getPhone());
            return doctorRepository.save(doctor).getId();
        }

        throw new BadRequestException("Unsupported role");
    }

    public PatientProfileDto getPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
        PatientProfileDto dto = new PatientProfileDto();
        dto.setId(patient.getId());
        dto.setUserId(patient.getUserId());
        dto.setFullName(patient.getFullName());
        dto.setDob(patient.getDob());
        dto.setSex(patient.getSex());
        dto.setPhone(patient.getPhone());
        dto.setAddress(patient.getAddress());
        dto.setEmergencyContact(patient.getEmergencyContact());
        return dto;
    }

    public DoctorProfileDto getDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
        DoctorProfileDto dto = new DoctorProfileDto();
        dto.setId(doctor.getId());
        dto.setUserId(doctor.getUserId());
        dto.setFullName(doctor.getFullName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setPhone(doctor.getPhone());
        return dto;
    }
}
