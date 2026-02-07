import { useState, useMemo, useCallback } from 'react'
import DashboardLayout from '@/components/layout/DashboardLayout'
import StatsCard from '@/components/common/StatsCard'
import DataTable from '@/components/common/DataTable'
import Modal from '@/components/common/Modal'
import { httpClient } from '@/lib/http/client'
import { API_ENDPOINTS } from '@/lib/config/constants'
import { createProfile, createAssignment, getPatient, getDoctor } from '@/features/care/api/careApi'

const navItems = [
  {
    path: '/admin',
    label: 'Dashboard',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
      </svg>
    ),
  },
  {
    path: '/admin/patients',
    label: 'Patients',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
    ),
  },
  {
    path: '/admin/appointments',
    label: 'Appointments',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
  },
  {
    path: '/admin/doctors',
    label: 'Doctors',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
]

// Pre-seeded users from DB (no list API available)
const initialPatients = [
  { id: '3', fullName: 'Patient 1', email: 'patient1@genzipher.com', dateOfBirth: '', phone: '', nic: '', bloodType: '', assignedDoctor: '', lastVisit: '' },
]

const initialDoctors = [
  { id: '2', fullName: 'Doctor 1', email: 'doctor1@genzipher.com', phone: '', specialization: '', licenceNumber: '', nic: '', status: 'Available' },
  { id: '4', fullName: 'Doctor 2', email: 'doctor2@genzipher.com', phone: '', specialization: '', licenceNumber: '', nic: '', status: 'Available' },
  { id: '8', fullName: 'Doctor 3', email: 'doctor@gmail.com', phone: '', specialization: '', licenceNumber: '', nic: '', status: 'Available' },
  { id: '9', fullName: 'Doctor 4', email: 'doctor@gmail.cdsasaf', phone: '', specialization: '', licenceNumber: '', nic: '', status: 'Available' },
]

const patientColumns = [
  { key: 'fullName', label: 'Patient Name' },
  { key: 'dateOfBirth', label: 'Date of Birth' },
  { key: 'phone', label: 'Phone' },
  { key: 'bloodType', label: 'Blood Type' },
  { key: 'assignedDoctor', label: 'Assigned Doctor' },
  { key: 'lastVisit', label: 'Last Visit' },
]

const doctorColumns = [
  { key: 'fullName', label: 'Name' },
  { key: 'specialization', label: 'Specialization' },
  { key: 'phone', label: 'Phone' },
  {
    key: 'status',
    label: 'Status',
    render: (value) => (
      <span className={`px-2 py-1 text-xs font-medium rounded-full ${
        value === 'Available' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
      }`}>
        {value}
      </span>
    ),
  },
]

function calculateAge(dateOfBirth) {
  if (!dateOfBirth) return 0
  const today = new Date()
  const birth = new Date(dateOfBirth)
  let age = today.getFullYear() - birth.getFullYear()
  const monthDiff = today.getMonth() - birth.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--
  }
  return age
}

export default function ReceptionistDashboard() {
  const [isAddPatientModalOpen, setIsAddPatientModalOpen] = useState(false)
  const [isAddDoctorModalOpen, setIsAddDoctorModalOpen] = useState(false)
  const [isViewPatientModalOpen, setIsViewPatientModalOpen] = useState(false)
  const [patients, setPatients] = useState(initialPatients)
  const [doctors, setDoctors] = useState(initialDoctors)
  const [selectedPatient, setSelectedPatient] = useState(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [newPatient, setNewPatient] = useState({
    fullName: '',
    dateOfBirth: '',
    phone: '',
    email: '',
    nic: '',
    sex: 'MALE',
    bloodType: '',
    address: '',
    emergencyContact: '',
    emergencyPhone: '',
    medicalHistory: '',
    allergies: '',
    assignedDoctor: '',
  })
  const [newDoctor, setNewDoctor] = useState({
    email: '',
    fullName: '',
    phone: '',
    specialization: '',
    licenceNumber: '',
    nic: '',
  })

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [successMessage, setSuccessMessage] = useState(null)

  const patientAge = useMemo(() => calculateAge(newPatient.dateOfBirth), [newPatient.dateOfBirth])
  const isNicRequired = patientAge > 16

  const availableDoctors = doctors.filter((d) => d.status === 'Available')

  const filteredPatients = patients.filter(
    (patient) =>
      patient.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      patient.phone.includes(searchTerm) ||
      patient.email.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const handleAddPatient = async (e) => {
    e.preventDefault()
    if (isNicRequired && !newPatient.nic.trim()) {
      alert('NIC is mandatory for patients over 16 years of age.')
      return
    }

    setIsSubmitting(true)
    setSubmitError('')
    setSuccessMessage(null)

    try {
      // Step 1: Register patient user via auth gateway (password auto-generated)
      const authResponse = await httpClient.post(API_ENDPOINTS.AUTH.REGISTER, {
        email: newPatient.email,
        role: 'PATIENT',
      })
      const userId = authResponse.userId || authResponse.id
      const generatedPassword = authResponse.password

      // Step 2: Create patient profile in care service
      const profileResponse = await createProfile({
        userId,
        role: 'PATIENT',
        patientProfile: {
          fullName: newPatient.fullName,
          dob: newPatient.dateOfBirth,
          sex: newPatient.sex || 'MALE',
          phone: newPatient.phone,
          address: newPatient.address || undefined,
          emergencyContact: newPatient.emergencyContact || undefined,
        },
      })

      // Step 3: Assign doctor if selected
      if (newPatient.assignedDoctor) {
        const selectedDoc = doctors.find(d => d.fullName === newPatient.assignedDoctor)
        if (selectedDoc) {
          await createAssignment({
            patientId: profileResponse.profileId,
            doctorUserId: selectedDoc.userId || selectedDoc.id,
            reason: 'Initial assignment at registration',
          })
        }
      }

      // Add to local state for immediate UI feedback
      const patient = {
        id: profileResponse.profileId,
        ...newPatient,
        lastVisit: new Date().toISOString().split('T')[0],
      }
      setPatients([...patients, patient])
      setSuccessMessage(
        `Patient registered!\nEmail: ${authResponse.email}\nTemporary Password: ${generatedPassword}\n\nPlease save this password — it won't be shown again.`
      )
      setNewPatient({
        fullName: '',
        dateOfBirth: '',
        phone: '',
        email: '',
        nic: '',
        sex: 'MALE',
        bloodType: '',
        address: '',
        emergencyContact: '',
        emergencyPhone: '',
        medicalHistory: '',
        allergies: '',
        assignedDoctor: '',
      })
      setIsAddPatientModalOpen(false)
    } catch (error) {
      console.error('Failed to register patient:', error)
      setSubmitError(error.message || 'Failed to register patient. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleAddDoctor = async (e) => {
    e.preventDefault()

    setIsSubmitting(true)
    setSubmitError('')
    setSuccessMessage(null)

    try {
      // Step 1: Register doctor user via auth gateway (password auto-generated)
      const authResponse = await httpClient.post(API_ENDPOINTS.AUTH.REGISTER, {
        email: newDoctor.email,
        role: 'DOCTOR',
      })
      const userId = authResponse.userId || authResponse.id
      const generatedPassword = authResponse.password

      // Step 2: Create doctor profile in care service
      const profileResponse = await createProfile({
        userId,
        role: 'DOCTOR',
        doctorProfile: {
          fullName: newDoctor.fullName,
          specialization: newDoctor.specialization,
          licenseNumber: newDoctor.licenceNumber,
          phone: newDoctor.phone,
        },
      })

      // Add to local state
      const doctor = {
        id: profileResponse.profileId,
        userId,
        ...newDoctor,
        status: 'Available',
      }
      setDoctors([...doctors, doctor])
      setSuccessMessage(
        `Doctor registered!\nEmail: ${authResponse.email}\nTemporary Password: ${generatedPassword}\n\nPlease save this password — it won't be shown again.`
      )
      setNewDoctor({
        email: '',
        fullName: '',
        phone: '',
        specialization: '',
        licenceNumber: '',
        nic: '',
      })
      setIsAddDoctorModalOpen(false)
    } catch (error) {
      console.error('Failed to register doctor:', error)
      setSubmitError(error.message || 'Failed to register doctor. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleViewPatient = (patient) => {
    setSelectedPatient(patient)
    setIsViewPatientModalOpen(true)
  }

  return (
    <DashboardLayout navItems={navItems} title="Admin">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Admin Dashboard</h1>
            <p className="text-slate-500 mt-1">Register and manage patients & doctors</p>
          </div>
          <div className="flex gap-3">
            <button
              onClick={() => setIsAddPatientModalOpen(true)}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              Register Patient
            </button>
            <button
              onClick={() => setIsAddDoctorModalOpen(true)}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              Register Doctor
            </button>
          </div>
        </div>

        {/* Success Banner */}
        {successMessage && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-sm font-semibold text-green-800">Registration Successful</h3>
                <pre className="mt-1 text-sm text-green-700 whitespace-pre-wrap font-mono">{successMessage}</pre>
              </div>
              <button onClick={() => setSuccessMessage(null)} className="text-green-500 hover:text-green-700">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
        )}

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <StatsCard
            title="Total Patients"
            value={patients.length}
            color="blue"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            }
          />
          <StatsCard
            title="Total Doctors"
            value={doctors.length}
            color="green"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            }
          />
          <StatsCard
            title="Appointments Today"
            value="48"
            color="purple"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            }
          />
          <StatsCard
            title="Available Doctors"
            value={availableDoctors.length}
            color="orange"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            }
          />
        </div>

        {/* Doctor Summary */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-slate-900">Doctor Summary</h2>
          </div>
          <DataTable
            columns={doctorColumns}
            data={doctors}
            emptyMessage="No doctors registered"
          />
        </div>

        {/* Search */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-4">
          <div className="flex gap-4">
            <div className="flex-1 relative">
              <svg
                className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                type="text"
                placeholder="Search patients by name, phone, or email..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>
        </div>

        {/* Patients Table */}
        <div>
          <h2 className="text-lg font-semibold text-slate-900 mb-4">Registered Patients</h2>
          <DataTable
            columns={patientColumns}
            data={filteredPatients}
            emptyMessage="No patients found"
            actions={(row) => (
              <>
                <button
                  onClick={() => handleViewPatient(row)}
                  className="px-3 py-1 text-xs font-medium rounded bg-blue-100 text-blue-700 hover:bg-blue-200"
                >
                  View
                </button>
                <button className="px-3 py-1 text-xs font-medium rounded bg-slate-100 text-slate-700 hover:bg-slate-200">
                  Edit
                </button>
              </>
            )}
          />
        </div>

        {/* Register Patient Modal */}
        <Modal
          isOpen={isAddPatientModalOpen}
          onClose={() => setIsAddPatientModalOpen(false)}
          title="Register New Patient"
          size="lg"
        >
          <form onSubmit={handleAddPatient} className="space-y-4">
            {submitError && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
                {submitError}
              </div>
            )}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Full Name *</label>
                <input
                  type="text"
                  required
                  value={newPatient.fullName}
                  onChange={(e) => setNewPatient({ ...newPatient, fullName: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Date of Birth *</label>
                <input
                  type="date"
                  required
                  value={newPatient.dateOfBirth}
                  onChange={(e) => setNewPatient({ ...newPatient, dateOfBirth: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>

            {/* Sex field (required by care API) */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Sex *</label>
                <select
                  required
                  value={newPatient.sex}
                  onChange={(e) => setNewPatient({ ...newPatient, sex: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </select>
              </div>
              <div />
            </div>

            {/* NIC field - mandatory if patient > 16 years */}
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                NIC {isNicRequired ? '*' : ''}
                {newPatient.dateOfBirth && (
                  <span className="text-xs text-slate-400 ml-2">
                    (Age: {patientAge} years{isNicRequired ? ' — NIC required' : ''})
                  </span>
                )}
              </label>
              <input
                type="text"
                required={isNicRequired}
                value={newPatient.nic}
                onChange={(e) => setNewPatient({ ...newPatient, nic: e.target.value })}
                className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-slate-900 placeholder-slate-400 ${
                  isNicRequired ? 'border-blue-400' : 'border-slate-300'
                }`}
                placeholder={isNicRequired ? 'NIC is mandatory for patients over 16' : 'Enter NIC (optional)'}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Phone *</label>
                <input
                  type="tel"
                  required
                  value={newPatient.phone}
                  onChange={(e) => setNewPatient({ ...newPatient, phone: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
                <input
                  type="email"
                  value={newPatient.email}
                  onChange={(e) => setNewPatient({ ...newPatient, email: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Blood Type</label>
                <select
                  value={newPatient.bloodType}
                  onChange={(e) => setNewPatient({ ...newPatient, bloodType: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select Blood Type</option>
                  <option value="A+">A+</option>
                  <option value="A-">A-</option>
                  <option value="B+">B+</option>
                  <option value="B-">B-</option>
                  <option value="AB+">AB+</option>
                  <option value="AB-">AB-</option>
                  <option value="O+">O+</option>
                  <option value="O-">O-</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Assign Doctor *</label>
                <select
                  required
                  value={newPatient.assignedDoctor}
                  onChange={(e) => setNewPatient({ ...newPatient, assignedDoctor: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select Doctor</option>
                  {doctors.map((doc) => (
                    <option key={doc.id} value={doc.fullName}>
                      {doc.fullName} - {doc.specialization}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Address</label>
              <input
                type="text"
                value={newPatient.address}
                onChange={(e) => setNewPatient({ ...newPatient, address: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Emergency Contact</label>
                <input
                  type="text"
                  value={newPatient.emergencyContact}
                  onChange={(e) => setNewPatient({ ...newPatient, emergencyContact: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Emergency Phone</label>
                <input
                  type="tel"
                  value={newPatient.emergencyPhone}
                  onChange={(e) => setNewPatient({ ...newPatient, emergencyPhone: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Known Allergies</label>
              <textarea
                value={newPatient.allergies}
                onChange={(e) => setNewPatient({ ...newPatient, allergies: e.target.value })}
                rows={2}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Medical History Notes</label>
              <textarea
                value={newPatient.medicalHistory}
                onChange={(e) => setNewPatient({ ...newPatient, medicalHistory: e.target.value })}
                rows={3}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="flex justify-end gap-3 mt-6">
              <button type="button" onClick={() => setIsAddPatientModalOpen(false)} className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors">Cancel</button>
              <button type="submit" disabled={isSubmitting} className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                {isSubmitting ? 'Registering...' : 'Register Patient'}
              </button>
            </div>
          </form>
        </Modal>

        {/* Register Doctor Modal */}
        <Modal
          isOpen={isAddDoctorModalOpen}
          onClose={() => setIsAddDoctorModalOpen(false)}
          title="Register New Doctor"
          size="lg"
        >
          <form onSubmit={handleAddDoctor} className="space-y-4">
            <p className="text-sm text-slate-500">Enter the doctor's details. A temporary password will be auto-generated.</p>
            {submitError && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
                {submitError}
              </div>
            )}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Email *</label>
                <input
                  type="email"
                  required
                  value={newDoctor.email}
                  onChange={(e) => setNewDoctor({ ...newDoctor, email: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Enter email address"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Full Name *</label>
                <input
                  type="text"
                  required
                  value={newDoctor.fullName}
                  onChange={(e) => setNewDoctor({ ...newDoctor, fullName: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Enter full name"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Phone *</label>
                <input
                  type="tel"
                  required
                  value={newDoctor.phone}
                  onChange={(e) => setNewDoctor({ ...newDoctor, phone: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Enter phone number"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">NIC *</label>
                <input
                  type="text"
                  required
                  value={newDoctor.nic}
                  onChange={(e) => setNewDoctor({ ...newDoctor, nic: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Enter NIC number"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Specialization *</label>
                <select
                  required
                  value={newDoctor.specialization}
                  onChange={(e) => setNewDoctor({ ...newDoctor, specialization: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="">Select Specialization</option>
                  <option value="Cardiology">Cardiology</option>
                  <option value="Neurology">Neurology</option>
                  <option value="Orthopedics">Orthopedics</option>
                  <option value="Pediatrics">Pediatrics</option>
                  <option value="Dermatology">Dermatology</option>
                  <option value="General Medicine">General Medicine</option>
                  <option value="Surgery">Surgery</option>
                  <option value="Psychiatry">Psychiatry</option>
                  <option value="Oncology">Oncology</option>
                  <option value="ENT">ENT</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Licence Number *</label>
                <input
                  type="text"
                  required
                  value={newDoctor.licenceNumber}
                  onChange={(e) => setNewDoctor({ ...newDoctor, licenceNumber: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Enter medical licence number"
                />
              </div>
            </div>

            <div className="flex justify-end gap-3 mt-6">
              <button type="button" onClick={() => setIsAddDoctorModalOpen(false)} className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors">Cancel</button>
              <button type="submit" disabled={isSubmitting} className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                {isSubmitting ? 'Registering...' : 'Register Doctor'}
              </button>
            </div>
          </form>
        </Modal>

        {/* View Patient Modal */}
        <Modal
          isOpen={isViewPatientModalOpen}
          onClose={() => setIsViewPatientModalOpen(false)}
          title="Patient Details"
          size="lg"
        >
          {selectedPatient && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-slate-500">Full Name</p>
                  <p className="font-medium text-slate-900">{selectedPatient.fullName}</p>
                </div>
                <div>
                  <p className="text-sm text-slate-500">Date of Birth</p>
                  <p className="font-medium text-slate-900">{selectedPatient.dateOfBirth}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-slate-500">NIC</p>
                  <p className="font-medium text-slate-900">{selectedPatient.nic || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-sm text-slate-500">Phone</p>
                  <p className="font-medium text-slate-900">{selectedPatient.phone}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-slate-500">Email</p>
                  <p className="font-medium text-slate-900">{selectedPatient.email || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-sm text-slate-500">Blood Type</p>
                  <p className="font-medium text-slate-900">{selectedPatient.bloodType}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-slate-500">Assigned Doctor</p>
                  <p className="font-medium text-slate-900">{selectedPatient.assignedDoctor}</p>
                </div>
                <div>
                  <p className="text-sm text-slate-500">Last Visit</p>
                  <p className="font-medium text-slate-900">{selectedPatient.lastVisit}</p>
                </div>
              </div>
              <div className="flex justify-end mt-6">
                <button
                  onClick={() => setIsViewPatientModalOpen(false)}
                  className="px-4 py-2 bg-slate-100 text-slate-700 rounded-lg hover:bg-slate-200 transition-colors"
                >
                  Close
                </button>
              </div>
            </div>
          )}
        </Modal>
      </div>
    </DashboardLayout>
  )
}
