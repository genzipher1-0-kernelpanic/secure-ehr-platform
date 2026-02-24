import axios, { type AxiosRequestConfig } from 'axios'
import { AUTH_TOKEN_KEY } from '../config/constants'

axios.interceptors.request.use((config: AxiosRequestConfig) => {
  const token = localStorage.getItem(AUTH_TOKEN_KEY)
  if (!token) {
    return config
  }

  const headers = config.headers ?? {}
  if (!('Authorization' in headers)) {
    headers.Authorization = `Bearer ${token}`
    config.headers = headers
  }

  return config
})
