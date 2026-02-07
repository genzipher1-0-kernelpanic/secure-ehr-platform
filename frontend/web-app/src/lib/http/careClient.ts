import axios, { AxiosError, AxiosInstance, AxiosRequestConfig } from 'axios';
import { config } from '../config/env';
import { AUTH_TOKEN_KEY, REFRESH_TOKEN_KEY, API_ENDPOINTS } from '../config/constants';

/**
 * Dedicated HTTP client for the Care Service (localhost:5005).
 * Shares the same Bearer-token auth logic as the main httpClient but
 * points at the care-service base URL.
 */
class CareHttpClient {
  private instance: AxiosInstance;

  constructor() {
    this.instance = axios.create({
      baseURL: config.careApiUrl,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor – attach Bearer token
    this.instance.interceptors.request.use(
      (reqConfig) => {
        const token = localStorage.getItem(AUTH_TOKEN_KEY);
        if (token) {
          reqConfig.headers.Authorization = `Bearer ${token}`;
        }
        return reqConfig;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor – handle 401 / token refresh
    this.instance.interceptors.response.use(
      (response) => response.data,
      async (error: AxiosError) => {
        const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
            if (!refreshToken) throw new Error('No refresh token available');

            // Refresh via the auth gateway (port 8080)
            const { data } = await axios.post(
              `${config.apiUrl}${API_ENDPOINTS.AUTH.REFRESH}`,
              { refreshToken }
            );

            localStorage.setItem(AUTH_TOKEN_KEY, data.accessToken);

            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
            }

            return this.instance(originalRequest);
          } catch (refreshError) {
            this.handleAuthFailure();
            return Promise.reject(refreshError);
          }
        }

        return Promise.reject(this.formatError(error));
      }
    );
  }

  private handleAuthFailure() {
    localStorage.clear();
    window.location.href = '/login';
  }

  private formatError(error: AxiosError): Error {
    const message =
      (error.response?.data as any)?.message || error.message || 'An error occurred';
    return new Error(message);
  }

  // ── HTTP methods ─────────────────────────────────────────────────────────
  async get<T = any>(url: string, cfg?: AxiosRequestConfig): Promise<T> {
    return this.instance.get(url, cfg);
  }

  async post<T = any>(url: string, data?: any, cfg?: AxiosRequestConfig): Promise<T> {
    return this.instance.post(url, data, cfg);
  }

  async put<T = any>(url: string, data?: any, cfg?: AxiosRequestConfig): Promise<T> {
    return this.instance.put(url, data, cfg);
  }

  async patch<T = any>(url: string, data?: any, cfg?: AxiosRequestConfig): Promise<T> {
    return this.instance.patch(url, data, cfg);
  }

  async delete<T = any>(url: string, cfg?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete(url, cfg);
  }
}

export const careHttpClient = new CareHttpClient();
export default careHttpClient;
