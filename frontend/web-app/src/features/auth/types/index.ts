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
  role: 'SUPER_ADMIN' | 'SYSTEM_ADMIN' | 'ADMIN' | 'DOCTOR';
}

export interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  role: 'SUPER_ADMIN' | 'SYSTEM_ADMIN' | 'ADMIN' | 'DOCTOR';
  mfaEnabled: boolean;
  isFirstLogin?: boolean;
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
