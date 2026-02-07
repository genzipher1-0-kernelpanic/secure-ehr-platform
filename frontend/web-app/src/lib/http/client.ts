import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { config } from '../config/env';
import { AUTH_TOKEN_KEY, REFRESH_TOKEN_KEY, API_ENDPOINTS } from '../config/constants';

class HttpClient {
  private instance: AxiosInstance;

  constructor() {
    this.instance = axios.create({
      baseURL: config.apiUrl,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor - add auth token
    this.instance.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem(AUTH_TOKEN_KEY);
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor - handle errors & token refresh
    this.instance.interceptors.response.use(
      (response) => response.data,
      async (error: AxiosError) => {
        const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

        // If 401 and not already retried, try to refresh token
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
            
            if (!refreshToken) {
              throw new Error('No refresh token available');
            }

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
            // Refresh failed, logout user
            this.handleAuthFailure();
            return Promise.reject(refreshError);
          }
        }

        // Handle other errors
        return Promise.reject(this.formatError(error));
      }
    );
  }

  private handleAuthFailure() {
    localStorage.clear();
    window.location.href = '/login';
  }

  private formatError(error: AxiosError): Error {
    const data = error.response?.data as any;
    const serverMessage = data?.message || data?.error;

    if (serverMessage) {
      return new Error(serverMessage);
    }

    // Provide meaningful messages for common HTTP status codes when backend body is empty
    const status = error.response?.status;
    if (status === 403) {
      return new Error('Access denied — you may have reached the account creation limit for this role, or you lack the required permissions.');
    }
    if (status === 409) {
      return new Error('A user with this email already exists.');
    }

    return new Error(error.message || 'An error occurred');
  }

  // HTTP methods
  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.get(url, config);
  }

  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.post(url, data, config);
  }

  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.put(url, data, config);
  }

  async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.patch(url, data, config);
  }

  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete(url, config);
  }
}

export const httpClient = new HttpClient();
export default httpClient;
