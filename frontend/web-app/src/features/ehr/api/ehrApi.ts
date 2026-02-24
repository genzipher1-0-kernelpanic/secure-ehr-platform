import axios from 'axios'
import { AUTH_TOKEN_KEY } from '../../../lib/config/constants'

const EHR_BASE = 'http://localhost:5006'

function authHeaders() {
  const token = localStorage.getItem(AUTH_TOKEN_KEY)
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function createEhrRecord(patientId: string | number, category: string, data: unknown) {
  const response = await axios.post(
    `${EHR_BASE}/api/ehr/patients/${patientId}`,
    { category, data },
    { headers: { 'Content-Type': 'application/json', ...authHeaders() } }
  )
  return response.data
}

export async function uploadLabReport(
  patientId: string | number,
  file: File,
  meta: {
    reportType: string
    title: string
    studyDate?: string
    relatedCategory?: string
    relatedVersion?: number
  }
) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('meta', JSON.stringify(meta))

  const response = await axios.post(`${EHR_BASE}/api/ehr/patients/${patientId}/labs`, formData, {
    headers: { ...authHeaders() },
  })
  return response.data
}

export async function getEhrRecord(patientId: string | number, category: string) {
  const response = await axios.get(`${EHR_BASE}/api/ehr/patients/${patientId}`, {
    params: { category },
    headers: { ...authHeaders() },
  })
  return response.data
}

export async function getLabReports(patientId: string | number) {
  const response = await axios.get(`${EHR_BASE}/api/ehr/patients/${patientId}/labs`, {
    headers: { ...authHeaders() },
  })
  return response.data
}
