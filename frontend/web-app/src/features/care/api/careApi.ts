import { careHttpClient } from '@/lib/http/careClient';
import { CARE_ENDPOINTS } from '@/lib/config/constants';
import type {
  InternalProfileCreateRequest,
  ProfileCreateResponse,
  PatientProfileDto,
  DoctorProfileDto,
  AssignmentCreateRequest,
  AssignmentResponse,
  ConsentCreateRequest,
  ConsentResponse,
  AccessDecisionResponse,
} from '../types';

// ─── Profile creation (internal) ──────────────────────────────────────────────

/**
 * Create a PATIENT or DOCTOR profile via the internal endpoint.
 * Called by Receptionist when registering new patients / doctors.
 */
export async function createProfile(
  payload: InternalProfileCreateRequest
): Promise<ProfileCreateResponse> {
  return careHttpClient.post<ProfileCreateResponse>(
    CARE_ENDPOINTS.INTERNAL.CREATE_PROFILE,
    payload
  );
}

// ─── Patient ──────────────────────────────────────────────────────────────────

/** Get a patient profile by patientId. */
export async function getPatient(patientId: string): Promise<PatientProfileDto> {
  return careHttpClient.get<PatientProfileDto>(
    CARE_ENDPOINTS.PATIENTS.GET(patientId)
  );
}

// ─── Doctor ───────────────────────────────────────────────────────────────────

/** Get the currently logged-in doctor's profile. */
export async function getMyDoctorProfile(): Promise<DoctorProfileDto> {
  return careHttpClient.get<DoctorProfileDto>(CARE_ENDPOINTS.DOCTORS.ME);
}

/** Get a doctor profile by doctorId. */
export async function getDoctor(doctorId: string): Promise<DoctorProfileDto> {
  return careHttpClient.get<DoctorProfileDto>(
    CARE_ENDPOINTS.DOCTORS.GET(doctorId)
  );
}

/** Get a doctor profile by userId (internal). */
export async function getDoctorByUserId(userId: string): Promise<DoctorProfileDto> {
  return careHttpClient.get<DoctorProfileDto>(
    CARE_ENDPOINTS.INTERNAL.GET_DOCTOR_BY_USER(userId)
  );
}

// ─── Assignments ──────────────────────────────────────────────────────────────

/** Assign a doctor to a patient. */
export async function createAssignment(
  payload: AssignmentCreateRequest
): Promise<AssignmentResponse> {
  return careHttpClient.post<AssignmentResponse>(
    CARE_ENDPOINTS.ASSIGNMENTS.CREATE,
    payload
  );
}

/** End an existing doctor-patient assignment. */
export async function endAssignment(assignmentId: string): Promise<void> {
  return careHttpClient.put<void>(
    CARE_ENDPOINTS.ASSIGNMENTS.END(assignmentId)
  );
}

// ─── Consents ─────────────────────────────────────────────────────────────────

/** Create a new consent grant. */
export async function createConsent(
  payload: ConsentCreateRequest
): Promise<ConsentResponse> {
  return careHttpClient.post<ConsentResponse>(
    CARE_ENDPOINTS.CONSENTS.CREATE,
    payload
  );
}

/** Revoke an existing consent. */
export async function revokeConsent(consentId: string): Promise<void> {
  return careHttpClient.put<void>(
    CARE_ENDPOINTS.CONSENTS.REVOKE(consentId)
  );
}

// ─── Access ───────────────────────────────────────────────────────────────────

/**
 * Check whether a user has access to a specific patient resource.
 * Primarily used internally for guard checks.
 */
export async function checkAccess(
  params: { patientId: string; requesterId: string }
): Promise<AccessDecisionResponse> {
  return careHttpClient.get<AccessDecisionResponse>(
    CARE_ENDPOINTS.INTERNAL.CHECK_ACCESS,
    { params }
  );
}
