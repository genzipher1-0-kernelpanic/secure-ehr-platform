import { useEffect, useMemo, useState } from 'react'
import DashboardLayout from '@/components/layout/DashboardLayout'
import StatsCard from '@/components/common/StatsCard'
import DataTable from '@/components/common/DataTable'
import Modal from '@/components/common/Modal'
import { httpClient } from '@/lib/http/client'
import { API_ENDPOINTS } from '@/lib/config/constants'
import { createProfile, createAssignment, getDoctors, getPatients } from '@/features/care/api/careApi'
import { createEhrRecord, uploadLabReport } from '@/features/ehr/api/ehrApi'

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

const initialPatients = []
const initialDoctors = []

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
  const [isAddRecordModalOpen, setIsAddRecordModalOpen] = useState(false)
  const [isViewPatientModalOpen, setIsViewPatientModalOpen] = useState(false)
  const [patients, setPatients] = useState(initialPatients)
  const [doctors, setDoctors] = useState(initialDoctors)
  const [isLoadingDirectory, setIsLoadingDirectory] = useState(true)
  const [directoryError, setDirectoryError] = useState('')
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
  const [recordForm, setRecordForm] = useState({
    patientId: '',
    recordType: 'CLINICAL',
    conditions: '',
    allergies: '',
    clinicalNotes: '',
    vitalsBp: '',
    vitalsHr: '',
    vitalsTemp: '',
    medications: '',
    procedures: '',
    carePlans: '',
    labReportType: 'PDF',
    labTitle: '',
    labStudyDate: '',
    labRelatedCategory: '',
    labRelatedVersion: '',
    labFile: null,
  })

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [successMessage, setSuccessMessage] = useState(null)
  const [recordSubmitError, setRecordSubmitError] = useState('')
  const [recordSuccessMessage, setRecordSuccessMessage] = useState(null)

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

  useEffect(() => {
    let active = true

    const loadDirectory = async () => {
      setIsLoadingDirectory(true)
      setDirectoryError('')
      try {
        const [patientList, doctorList] = await Promise.all([getPatients(), getDoctors()])
        if (!active) return

        const mappedPatients = patientList.map((patient) => ({
          id: String(patient.id),
          fullName: patient.fullName,
          email: patient.email || '',
          dateOfBirth: patient.dob || '',
          phone: patient.phone || '',
          nic: '',
          bloodType: '',
          assignedDoctor: '',
          lastVisit: '',
        }))

        const mappedDoctors = doctorList.map((doctor) => ({
          id: String(doctor.id),
          userId: doctor.userId,
          fullName: doctor.fullName,
          email: doctor.email || '',
          phone: doctor.phone || '',
          specialization: doctor.specialization || '',
          licenceNumber: doctor.licenseNumber || '',
          nic: '',
          status: 'Available',
        }))

        setPatients(mappedPatients)
        setDoctors(mappedDoctors)
      } catch (error) {
        if (!active) return
        console.error('Failed to load patients/doctors:', error)
        setDirectoryError('Failed to load patients and doctors from care service.')
      } finally {
        if (active) {
          setIsLoadingDirectory(false)
        }
      }
    }

    loadDirectory()
    return () => {
      active = false
    }
  }, [])

  const parseList = (value) =>
    value
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item)

  const handleAddRecord = async (e) => {
    e.preventDefault()
    setRecordSubmitError('')
    setRecordSuccessMessage(null)

    if (!recordForm.patientId) {
      setRecordSubmitError('Please select a patient.')
      return
    }

    try {
      if (recordForm.recordType === 'LAB') {
        if (!recordForm.labFile) {
          setRecordSubmitError('Please attach a lab report file.')
          return
        }
        const allowedTypes = ['application/pdf', 'image/png']
        if (!allowedTypes.includes(recordForm.labFile.type)) {
          setRecordSubmitError('Only PDF or PNG files are supported.')
          return
        }
        if (!recordForm.labTitle.trim()) {
          setRecordSubmitError('Lab report title is required.')
          return
        }

        const meta = {
          reportType: recordForm.labReportType,
          title: recordForm.labTitle,
          studyDate: recordForm.labStudyDate || undefined,
          relatedCategory: recordForm.labRelatedCategory || undefined,
          relatedVersion: recordForm.labRelatedVersion
            ? parseInt(recordForm.labRelatedVersion)
            : undefined,
        }
        await uploadLabReport(recordForm.patientId, recordForm.labFile, meta)
      } else {
        const data = {}
        if (recordForm.recordType === 'CLINICAL') {
          if (recordForm.conditions.trim()) data.conditions = parseList(recordForm.conditions)
          if (recordForm.allergies.trim()) data.allergies = parseList(recordForm.allergies)
          if (recordForm.clinicalNotes.trim()) data.clinicalNotes = recordForm.clinicalNotes
          const vitals = {}
          if (recordForm.vitalsBp.trim()) vitals.bp = recordForm.vitalsBp
          if (recordForm.vitalsHr.trim()) vitals.hr = Number(recordForm.vitalsHr)
          if (recordForm.vitalsTemp.trim()) vitals.tempC = Number(recordForm.vitalsTemp)
          if (Object.keys(vitals).length > 0) data.vitals = vitals
        } else if (recordForm.recordType === 'TREATMENTS') {
          if (recordForm.medications.trim()) data.medications = parseList(recordForm.medications)
          if (recordForm.procedures.trim()) data.procedures = parseList(recordForm.procedures)
          if (recordForm.carePlans.trim()) data.carePlans = parseList(recordForm.carePlans)
        }

        if (Object.keys(data).length === 0) {
          setRecordSubmitError('Please add at least one field before submitting.')
          return
        }
        await createEhrRecord(recordForm.patientId, recordForm.recordType, data)
      }

      setRecordSuccessMessage('Record added successfully.')
      setRecordForm({
        patientId: '',
        recordType: 'CLINICAL',
        conditions: '',
        allergies: '',
        clinicalNotes: '',
        vitalsBp: '',
        vitalsHr: '',
        vitalsTemp: '',
        medications: '',
        procedures: '',
        carePlans: '',
        labReportType: 'PDF',
        labTitle: '',
        labStudyDate: '',
        labRelatedCategory: '',
        labRelatedVersion: '',
        labFile: null,
      })
    } catch (error) {
      console.error('Failed to add EHR record:', error)
      setRecordSubmitError(error.message || 'Failed to add EHR record.')
    }
  }

  const openRecordModal = () => {
    setRecordSubmitError('')
    setRecordSuccessMessage(null)
    setIsAddRecordModalOpen(true)
  }

  const closeRecordModal = () => {
    setIsAddRecordModalOpen(false)
    setRecordSubmitError('')
    setRecordSuccessMessage(null)
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
              onClick={openRecordModal}
              className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5l5 5v11a2 2 0 01-2 2z" />
              </svg>
              Add Patient Record
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
        {directoryError && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-sm text-red-700">
            {directoryError}
          </div>
        )}
        {isLoadingDirectory && (
          <div className="bg-slate-50 border border-slate-200 rounded-lg p-4 text-sm text-slate-600">
            Loading patients and doctors...
          </div>
        )}

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

        {/* Add Patient Record Modal */}
        <Modal
          isOpen={isAddRecordModalOpen}
          onClose={closeRecordModal}
          title="Add Patient Record"
          size="lg"
        >
          <form onSubmit={handleAddRecord} className="space-y-4">
            {recordSubmitError && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
                {recordSubmitError}
              </div>
            )}
            {recordSuccessMessage && (
              <div className="p-3 bg-green-50 border border-green-200 rounded-lg text-sm text-green-700">
                {recordSuccessMessage}
              </div>
            )}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Patient *</label>
                <select
                  required
                  value={recordForm.patientId}
                  onChange={(e) => setRecordForm({ ...recordForm, patientId: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                >
                  <option value="">Select Patient</option>
                  {patients.map((patient) => (
                    <option key={patient.id} value={patient.id}>
                      {patient.fullName} (ID: {patient.id})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Record Type *</label>
                <select
                  value={recordForm.recordType}
                  onChange={(e) => setRecordForm({ ...recordForm, recordType: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                >
                  <option value="CLINICAL">Clinical</option>
                  <option value="TREATMENTS">Treatments</option>
                  <option value="LAB">Lab Report</option>
                </select>
              </div>
            </div>

            {recordForm.recordType === 'CLINICAL' && (
              <>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Conditions</label>
                  <input
                    type="text"
                    value={recordForm.conditions}
                    onChange={(e) => setRecordForm({ ...recordForm, conditions: e.target.value })}
                    placeholder="e.g., Hypertension, Diabetes"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Allergies</label>
                  <input
                    type="text"
                    value={recordForm.allergies}
                    onChange={(e) => setRecordForm({ ...recordForm, allergies: e.target.value })}
                    placeholder="e.g., Penicillin"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  />
                </div>
                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">BP</label>
                    <input
                      type="text"
                      value={recordForm.vitalsBp}
                      onChange={(e) => setRecordForm({ ...recordForm, vitalsBp: e.target.value })}
                      placeholder="120/80"
                      className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">HR</label>
                    <input
                      type="number"
                      value={recordForm.vitalsHr}
                      onChange={(e) => setRecordForm({ ...recordForm, vitalsHr: e.target.value })}
                      placeholder="72"
                      className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Temp (C)</label>
                    <input
                      type="number"
                      step="0.1"
                      value={recordForm.vitalsTemp}
                      onChange={(e) => setRecordForm({ ...recordForm, vitalsTemp: e.target.value })}
                      placeholder="36.6"
                      className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Clinical Notes</label>
                  <textarea
                    value={recordForm.clinicalNotes}
                    onChange={(e) => setRecordForm({ ...recordForm, clinicalNotes: e.target.value })}
                    rows={3}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  />
                </div>
              </>
            )}

            {recordForm.recordType === 'TREATMENTS' && (
              <>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Medications</label>
                  <input
                    type="text"
                    value={recordForm.medications}
                    onChange={(e) => setRecordForm({ ...recordForm, medications: e.target.value })}
                    placeholder="e.g., Lisinopril 10mg, Aspirin 81mg"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Procedures</label>
                  <input
                    type="text"
                    value={recordForm.procedures}
                    onChange={(e) => setRecordForm({ ...recordForm, procedures: e.target.value })}
                    placeholder="e.g., ECG, Blood test"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Care Plans</label>
                  <input
                    type="text"
                    value={recordForm.carePlans}
                    onChange={(e) => setRecordForm({ ...recordForm, carePlans: e.target.value })}
                    placeholder="e.g., Weekly blood pressure checks"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  />
                </div>
              </>
            )}

            {recordForm.recordType === 'LAB' && (
              <>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Report Type *</label>
                    <select
                      value={recordForm.labReportType}
                      onChange={(e) => setRecordForm({ ...recordForm, labReportType: e.target.value })}
                      className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                    >
                      <option value="PDF">PDF</option>
                      <option value="XRAY">X-Ray</option>
                      <option value="BLOOD">Blood</option>
                      <option value="MRI">MRI</option>
                      <option value="ULTRASOUND">Ultrasound</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Title *</label>
                    <input
                      type="text"
                      value={recordForm.labTitle}
                      onChange={(e) => setRecordForm({ ...recordForm, labTitle: e.target.value })}
                      placeholder="e.g., Chest X-Ray"
                      className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                    />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Study Date</label>
                    <input
                      type="date"
                      value={recordForm.labStudyDate}
                      onChange={(e) => setRecordForm({ ...recordForm, labStudyDate: e.target.value })}
                      className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Related Category</label>
                    <select
                      value={recordForm.labRelatedCategory}
                      onChange={(e) => setRecordForm({ ...recordForm, labRelatedCategory: e.target.value })}
                      className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                    >
                      <option value="">None</option>
                      <option value="CLINICAL">Clinical</option>
                      <option value="TREATMENTS">Treatments</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Related Version</label>
                  <input
                    type="number"
                    value={recordForm.labRelatedVersion}
                    onChange={(e) => setRecordForm({ ...recordForm, labRelatedVersion: e.target.value })}
                    placeholder="Version number"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Lab Report File (PDF/PNG) *</label>
                  <input
                    type="file"
                    accept=".pdf,.png"
                    onChange={(e) => setRecordForm({ ...recordForm, labFile: e.target.files?.[0] || null })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  />
                </div>
              </>
            )}

            <div className="flex justify-end gap-3 mt-6">
              <button
                type="button"
                onClick={closeRecordModal}
                className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
              >
                Save Record
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
