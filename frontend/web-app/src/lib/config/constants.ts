export const AUTH_TOKEN_KEY = 'accessToken';
export const REFRESH_TOKEN_KEY = 'refreshToken';
export const USER_KEY = 'user';

export const ROLES = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  SYSTEM_ADMIN: 'SYSTEM_ADMIN',
  RECEPTIONIST: 'RECEPTIONIST',
  DOCTOR: 'DOCTOR',
} as const;

export type UserRole = typeof ROLES[keyof typeof ROLES];

export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    MFA_SETUP: '/auth/mfa/setup',
    MFA_ENABLE: '/auth/mfa/enable',
    MFA_VERIFY: '/auth/mfa/verify',
  },
  PATIENTS: {
    LIST: '/patients',
    DETAIL: (id: string) => `/patients/${id}`,
    RECORDS: (id: string) => `/patients/${id}/records`,
    SEARCH: '/patients/search',
  },
  APPOINTMENTS: {
    LIST: '/appointments',
    CREATE: '/appointments',
    UPDATE: (id: string) => `/appointments/${id}`,
    CANCEL: (id: string) => `/appointments/${id}/cancel`,
  },
  AUDIT: {
    LOGS: '/audit/logs',
    REPORT: '/audit/report',
  },
} as const;

// ─── Care Service Endpoints (localhost:5005) ────────────────────────────────

export const CARE_ENDPOINTS = {
  // Internal (profile creation / access checks)
  INTERNAL: {
    CREATE_PROFILE: '/internal/profiles',
    CHECK_ACCESS: '/internal/access',
    GET_DOCTOR_BY_USER: (userId: string) => `/internal/doctors/${userId}`,
  },
  // Doctor endpoints
  DOCTORS: {
    ME: '/api/doctors/me',
    GET: (doctorId: string) => `/api/care/doctors/${doctorId}`,
  },
  // Patient endpoints
  PATIENTS: {
    GET: (patientId: string) => `/api/care/patients/${patientId}`,
  },
  // Assignments
  ASSIGNMENTS: {
    CREATE: '/api/care/assignments',
    END: (assignmentId: string) => `/api/care/assignments/${assignmentId}/end`,
  },
  // Consents
  CONSENTS: {
    CREATE: '/api/care/consents',
    REVOKE: (consentId: string) => `/api/care/consents/${consentId}/revoke`,
  },
} as const;

export const TOAST_DURATION = 5000;

export const PASSWORD_REQUIREMENTS = {
  minLength: 8,
  requireUppercase: true,
  requireLowercase: true,
  requireNumber: true,
  requireSpecial: true,
} as const;
