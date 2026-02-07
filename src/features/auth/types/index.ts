export interface LoginCredentials {
  username: string;
  password: string;
  mfaCode?: string;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  fullName: string;
  role: 'DOCTOR' | 'NURSE' | 'PATIENT';
}

export interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  role: 'ADMIN' | 'DOCTOR' | 'NURSE' | 'PATIENT';
  mfaEnabled: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
  requireMFA?: boolean;
  tempToken?: string;
}

export interface MFASetupResponse {
  secret: string;
  qrCode: string;
}
