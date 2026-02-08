import { AUTH_TOKEN_KEY } from '../../../lib/config/constants'

const EHR_BASE = 'http://localhost:5006'

function authHeaders() {
  const token = localStorage.getItem(AUTH_TOKEN_KEY)
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function createEhrRecord(patientId: string | number, category: string, data: unknown) {
  const response = await fetch(`${EHR_BASE}/api/ehr/patients/${patientId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify({ category, data }),
  })
  if (!response.ok) {
    throw new Error(await response.text())
  }
  return response.json()
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

  const response = await fetch(`${EHR_BASE}/api/ehr/patients/${patientId}/labs`, {
    method: 'POST',
    headers: { ...authHeaders() },
    body: formData,
  })
  if (!response.ok) {
    throw new Error(await response.text())
  }
  return response.json()
}
