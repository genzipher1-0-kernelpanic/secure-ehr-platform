import axios from 'axios'
import { AUTH_TOKEN_KEY } from '../../../lib/config/constants'

// Care-service internal endpoints go through gateway but NOT under /api prefix
const GATEWAY_BASE = 'http://localhost:8080'

function authHeaders() {
  const token = localStorage.getItem(AUTH_TOKEN_KEY)
  return token ? { Authorization: `Bearer ${token}` } : {}
}

// ── Profile management (internal endpoint) ────────────────────────────

interface PatientProfile {
  fullName: string
  dob: string
  sex: string
  phone: string
  address?: string
  emergencyContact?: string
}

interface DoctorProfile {
  fullName: string
  specialization: string
  licenseNumber: string
  phone: string
}

interface CreateProfileRequest {
  userId: string | number
  role: 'PATIENT' | 'DOCTOR'
  patientProfile?: PatientProfile
  doctorProfile?: DoctorProfile
}

interface CreateAssignmentRequest {
  patientId: string | number
  doctorUserId: string | number
  reason?: string
}

export async function createProfile(data: CreateProfileRequest) {
  const response = await axios.post(`${GATEWAY_BASE}/internal/profiles`, data, {
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
  })
  return response.data
}

// ── Assignment management ─────────────────────────────────────────────

export async function createAssignment(data: CreateAssignmentRequest) {
  const response = await axios.post(`${GATEWAY_BASE}/api/care/assignments`, data, {
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
  })
  return response.data
}

// ── Lookup helpers ────────────────────────────────────────────────────

export async function getPatient(patientId: string | number) {
  const response = await axios.get(`${GATEWAY_BASE}/api/care/patients/${patientId}`, {
    headers: authHeaders(),
  })
  return response.data
}

export async function getDoctor(doctorId: string | number) {
  const response = await axios.get(`${GATEWAY_BASE}/api/care/doctors/${doctorId}`, {
    headers: authHeaders(),
  })
  return response.data
}
