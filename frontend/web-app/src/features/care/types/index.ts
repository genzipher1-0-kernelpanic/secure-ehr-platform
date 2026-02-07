// ─── Enums ────────────────────────────────────────────────────────────────────

export type Sex = 'MALE' | 'FEMALE';

export type ProfileRole = 'PATIENT' | 'DOCTOR';

export type ConsentScope =
  | 'EHR_READ'
  | 'EHR_WRITE'
  | 'APPOINTMENT_READ'
  | 'APPOINTMENT_WRITE'
  | 'ALL';

// ─── Patient ──────────────────────────────────────────────────────────────────

export interface PatientProfileDto {
  id?: string;
  userId?: string;
  fullName: string;
  dob: string;           // ISO date string
  sex: Sex;
  phone: string;
  address?: string;
  emergencyContact?: string;
}

// ─── Doctor ───────────────────────────────────────────────────────────────────

export interface DoctorProfileDto {
  id?: string;
  userId?: string;
  fullName: string;
  specialization: string;
  licenseNumber: string;
  phone: string;
}

// ─── Profile Creation (Internal) ──────────────────────────────────────────────

export interface InternalProfileCreateRequest {
  userId: string;
  role: ProfileRole;
  patientProfile?: PatientProfileDto;
  doctorProfile?: DoctorProfileDto;
}

export interface ProfileCreateResponse {
  profileId: string;
}

// ─── Assignments ──────────────────────────────────────────────────────────────

export interface AssignmentCreateRequest {
  patientId: string;
  doctorUserId: string;
  reason?: string;
}

export interface AssignmentResponse {
  assignmentId: string;
}

// ─── Consents ─────────────────────────────────────────────────────────────────

export interface ConsentCreateRequest {
  patientId: string;
  granteeUserId: string;
  scope: ConsentScope;
  validTo?: string;       // ISO datetime string
}

export interface ConsentResponse {
  consentId: string;
}

// ─── Access Decision ──────────────────────────────────────────────────────────

export interface AccessDecisionResponse {
  allowed: boolean;
  reason: string;
}

// ─── Composite types for UI ───────────────────────────────────────────────────

export interface PatientWithDetails extends PatientProfileDto {
  assignedDoctorName?: string;
  lastVisit?: string;
}

export interface DoctorWithStatus extends DoctorProfileDto {
  status?: string;
  email?: string;
}
