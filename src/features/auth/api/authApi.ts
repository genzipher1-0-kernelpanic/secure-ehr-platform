import { httpClient } from '@/lib/http/client';
import { API_ENDPOINTS, AUTH_TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY } from '@/lib/config/constants';
import type { LoginCredentials, RegisterData, AuthResponse, MFASetupResponse, User } from '../types';

export const authApi = {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await httpClient.post<AuthResponse>(
      API_ENDPOINTS.AUTH.LOGIN,
      credentials
    );

    if (response.accessToken && !response.requireMFA) {
      localStorage.setItem(AUTH_TOKEN_KEY, response.accessToken);
      localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
      localStorage.setItem(USER_KEY, JSON.stringify(response.user));
    }

    return response;
  },

  async register(data: RegisterData): Promise<User> {
    return httpClient.post<User>(API_ENDPOINTS.AUTH.REGISTER, data);
  },

  async logout(): Promise<void> {
    await httpClient.post(API_ENDPOINTS.AUTH.LOGOUT);
    localStorage.clear();
  },

  async setupMFA(): Promise<MFASetupResponse> {
    return httpClient.post<MFASetupResponse>(API_ENDPOINTS.AUTH.MFA_SETUP);
  },

  async enableMFA(mfaCode: string): Promise<{ success: boolean }> {
    return httpClient.post(API_ENDPOINTS.AUTH.MFA_ENABLE, { mfaCode });
  },

  async verifyMFA(mfaCode: string, tempToken: string): Promise<AuthResponse> {
    const response = await httpClient.post<AuthResponse>(
      API_ENDPOINTS.AUTH.MFA_VERIFY,
      { mfaCode },
      { headers: { Authorization: `Bearer ${tempToken}` } }
    );

    localStorage.setItem(AUTH_TOKEN_KEY, response.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(response.user));

    return response;
  },

  getCurrentUser(): User | null {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem(AUTH_TOKEN_KEY);
  },
};
