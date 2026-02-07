import { useEffect, useState } from 'react'
import DashboardLayout from '@/components/layout/DashboardLayout'
import StatsCard from '@/components/common/StatsCard'
import DataTable from '@/components/common/DataTable'
import Modal from '@/components/common/Modal'
import { getDoctorMe, getDoctorPatients } from '@/features/care/api/careApi'

const navItems = [
  {
    path: '/doctor',
    label: 'Dashboard',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
      </svg>
    ),
  },
  {
    path: '/doctor/patients',
    label: 'My Patients',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
    ),
  },
  {
    path: '/doctor/appointments',
    label: 'Appointments',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
  },
  {
    path: '/doctor/referrals',
    label: 'Referrals',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
      </svg>
    ),
  },
]

// Mock other specialists for referral
const mockSpecialists = [
  { id: '1', fullName: 'Dr. Jennifer White', specialty: 'Neurology' },
  { id: '2', fullName: 'Dr. David Lee', specialty: 'Orthopedics' },
  { id: '3', fullName: 'Dr. Sarah Johnson', specialty: 'Pediatrics' },
  { id: '4', fullName: 'Dr. Michael Chen', specialty: 'Gastroenterology' },
  { id: '5', fullName: 'Dr. Amanda Brown', specialty: 'Dermatology' },
  { id: '6', fullName: 'Dr. William Davis', specialty: 'Oncology' },
]

const patientColumns = [
  { key: 'fullName', label: 'Patient Name' },
  { key: 'age', label: 'Age' },
  { key: 'condition', label: 'Primary Condition' },
  { key: 'lastVisit', label: 'Last Visit' },
  { key: 'nextAppointment', label: 'Next Appointment' },
]

export default function DoctorDashboard() {
  const [patients, setPatients] = useState([])
  const [isLoadingPatients, setIsLoadingPatients] = useState(true)
  const [patientsError, setPatientsError] = useState('')
  const [isViewPatientModalOpen, setIsViewPatientModalOpen] = useState(false)
  const [isReferPatientModalOpen, setIsReferPatientModalOpen] = useState(false)
  const [isAddNotesModalOpen, setIsAddNotesModalOpen] = useState(false)
  const [isEditPatientModalOpen, setIsEditPatientModalOpen] = useState(false)
  const [selectedPatient, setSelectedPatient] = useState(null)
  const [editPatientData, setEditPatientData] = useState(null)
  const [referralData, setReferralData] = useState({
    specialist: '',
    reason: '',
    urgency: 'Normal',
    notes: '',
  })
  const [newNotes, setNewNotes] = useState('')

  useEffect(() => {
    let isActive = true

    const loadPatients = async () => {
      setIsLoadingPatients(true)
      setPatientsError('')
      try {
        const doctor = await getDoctorMe()
        const doctorUserId = doctor?.userId ?? doctor?.id
        if (!doctorUserId) {
          throw new Error('Doctor profile missing userId')
        }
        const assignedPatients = await getDoctorPatients(doctorUserId)
        if (!isActive) return

        const mapped = assignedPatients.map((patient) => {
          const age = patient.dob
            ? Math.floor((Date.now() - new Date(patient.dob).getTime()) / 31557600000)
            : ''
          const gender = patient.sex ? patient.sex.charAt(0) + patient.sex.slice(1).toLowerCase() : 'N/A'
          return {
            id: String(patient.id),
            fullName: patient.fullName,
            age,
            gender,
            phone: patient.phone || 'N/A',
            email: patient.email || 'N/A',
            bloodType: 'N/A',
            condition: 'N/A',
            lastVisit: 'N/A',
            nextAppointment: 'N/A',
            allergies: 'N/A',
            medicalHistory: 'N/A',
            currentMedications: [],
          }
        })
        setPatients(mapped)
      } catch (error) {
        if (!isActive) return
        console.error('Failed to load doctor patients:', error)
        setPatientsError('Failed to load patients from care service.')
        setPatients([])
      } finally {
        if (isActive) {
          setIsLoadingPatients(false)
        }
      }
    }

    loadPatients()
    return () => {
      isActive = false
    }
  }, [])

  const handleViewPatient = (patient) => {
    setSelectedPatient(patient)
    setIsViewPatientModalOpen(true)
  }

  const handleReferPatient = (patient) => {
    setSelectedPatient(patient)
    setReferralData({ specialist: '', reason: '', urgency: 'Normal', notes: '' })
    setIsReferPatientModalOpen(true)
  }

  const handleAddNotes = (patient) => {
    setSelectedPatient(patient)
    setNewNotes('')
    setIsAddNotesModalOpen(true)
  }

  const handleEditPatient = (patient) => {
    setSelectedPatient(patient)
    setEditPatientData({
      fullName: patient.fullName,
      age: patient.age,
      gender: patient.gender,
      phone: patient.phone,
      email: patient.email,
      bloodType: patient.bloodType,
      condition: patient.condition,
      allergies: patient.allergies,
      medicalHistory: patient.medicalHistory,
      currentMedications: patient.currentMedications.join(', '),
    })
    setIsEditPatientModalOpen(true)
  }

  const submitEditPatient = (e) => {
    e.preventDefault()
    const updatedPatients = patients.map((p) =>
      p.id === selectedPatient.id
        ? {
            ...p,
            ...editPatientData,
            currentMedications: editPatientData.currentMedications
              .split(',')
              .map((med) => med.trim())
              .filter((med) => med),
          }
        : p
    )
    setPatients(updatedPatients)
    alert('Patient details updated successfully')
    setIsEditPatientModalOpen(false)
  }

  const submitReferral = (e) => {
    e.preventDefault()
    console.log('Referral submitted:', { patient: selectedPatient.fullName, ...referralData })
    alert(`Referral submitted for ${selectedPatient.fullName} to ${referralData.specialist}`)
    setIsReferPatientModalOpen(false)
  }

  const submitNotes = (e) => {
    e.preventDefault()
    console.log('Notes added:', { patient: selectedPatient.fullName, notes: newNotes })
    alert('Medical notes saved successfully')
    setIsAddNotesModalOpen(false)
  }

  return (
    <DashboardLayout navItems={navItems} title="Doctor">
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Doctor Dashboard</h1>
          <p className="text-slate-500 mt-1">Welcome back! Here are your patients and appointments.</p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <StatsCard
            title="My Patients"
            value={patients.length}
            color="blue"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            }
          />
          <StatsCard
            title="Today's Appointments"
            value="8"
            color="green"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            }
          />
          <StatsCard
            title="Pending Referrals"
            value="2"
            color="orange"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
              </svg>
            }
          />
          <StatsCard
            title="Completed This Week"
            value="24"
            color="purple"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            }
          />
        </div>

        {/* Today's Schedule */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
          <h2 className="text-lg font-semibold text-slate-900 mb-4">Today's Schedule</h2>
          <div className="space-y-3">
            {[
              { time: '09:00 AM', patient: 'Alice Thompson', type: 'Follow-up' },
              { time: '10:00 AM', patient: 'Daniel Kim', type: 'Check-up' },
              { time: '11:30 AM', patient: 'Emma Wilson', type: 'Consultation' },
              { time: '02:00 PM', patient: 'New Patient', type: 'Initial Visit' },
            ].map((appointment, idx) => (
              <div
                key={idx}
                className="flex items-center justify-between p-3 bg-slate-50 rounded-lg hover:bg-slate-100 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <span className="text-sm font-medium text-blue-600 w-20">{appointment.time}</span>
                  <span className="font-medium text-slate-900">{appointment.patient}</span>
                </div>
                <span className="text-sm text-slate-500">{appointment.type}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Patients Table */}
        <div>
          <h2 className="text-lg font-semibold text-slate-900 mb-4">My Patients</h2>
          {patientsError && (
            <p className="text-sm text-red-600 mb-3">{patientsError}</p>
          )}
          {isLoadingPatients && (
            <p className="text-sm text-slate-500 mb-3">Loading patients...</p>
          )}
          <DataTable
            columns={patientColumns}
            data={patients}
            emptyMessage="No patients assigned"
            actions={(row) => (
              <>
                <button
                  onClick={() => handleViewPatient(row)}
                  className="px-3 py-1 text-xs font-medium rounded bg-blue-100 text-blue-700 hover:bg-blue-200"
                >
                  View Details
                </button>
                <button
                  onClick={() => handleEditPatient(row)}
                  className="px-3 py-1 text-xs font-medium rounded bg-slate-100 text-slate-700 hover:bg-slate-200"
                >
                  Edit
                </button>
                <button
                  onClick={() => handleAddNotes(row)}
                  className="px-3 py-1 text-xs font-medium rounded bg-green-100 text-green-700 hover:bg-green-200"
                >
                  Add Notes
                </button>
                <button
                  onClick={() => handleReferPatient(row)}
                  className="px-3 py-1 text-xs font-medium rounded bg-purple-100 text-purple-700 hover:bg-purple-200"
                >
                  Refer
                </button>
              </>
            )}
          />
        </div>

        {/* View Patient Modal */}
        <Modal
          isOpen={isViewPatientModalOpen}
          onClose={() => setIsViewPatientModalOpen(false)}
          title="Patient Details"
          size="lg"
        >
          {selectedPatient && (
            <div className="space-y-6">
              {/* Patient Header */}
              <div className="flex items-center gap-4 pb-4 border-b border-slate-200">
                <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center">
                  <span className="text-2xl font-bold text-blue-600">
                    {selectedPatient.fullName.charAt(0)}
                  </span>
                </div>
                <div>
                  <h3 className="text-xl font-semibold text-slate-900">{selectedPatient.fullName}</h3>
                  <p className="text-slate-500">
                    {selectedPatient.age ? `${selectedPatient.age} years old` : 'Age unknown'}, {selectedPatient.gender}
                  </p>
                </div>
              </div>

              {/* Basic Info */}
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <p className="text-sm text-slate-500">Blood Type</p>
                  <p className="font-medium text-slate-900">{selectedPatient.bloodType}</p>
                </div>
                <div>
                  <p className="text-sm text-slate-500">Phone</p>
                  <p className="font-medium text-slate-900">{selectedPatient.phone}</p>
                </div>
                <div>
                  <p className="text-sm text-slate-500">Email</p>
                  <p className="font-medium text-slate-900">{selectedPatient.email}</p>
                </div>
              </div>

              {/* Allergies */}
              <div>
                <p className="text-sm text-slate-500 mb-1">Known Allergies</p>
                <div className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${
                  selectedPatient.allergies === 'None'
                    ? 'bg-green-100 text-green-700'
                    : 'bg-red-100 text-red-700'
                }`}>
                  {selectedPatient.allergies}
                </div>
              </div>

              {/* Primary Condition */}
              <div>
                <p className="text-sm text-slate-500 mb-1">Primary Condition</p>
                <p className="font-medium text-slate-900">{selectedPatient.condition}</p>
              </div>

              {/* Medical History */}
              <div>
                <p className="text-sm text-slate-500 mb-1">Medical History</p>
                <p className="text-slate-700 bg-slate-50 p-3 rounded-lg">{selectedPatient.medicalHistory}</p>
              </div>

              {/* Current Medications */}
              <div>
                <p className="text-sm text-slate-500 mb-2">Current Medications</p>
                <div className="flex flex-wrap gap-2">
                  {selectedPatient.currentMedications.length === 0 && (
                    <span className="text-sm text-slate-500">No medications listed</span>
                  )}
                  {selectedPatient.currentMedications.map((med, idx) => (
                    <span
                      key={idx}
                      className="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm"
                    >
                      {med}
                    </span>
                  ))}
                </div>
              </div>

              {/* Actions */}
              <div className="flex justify-end gap-3 pt-4 border-t border-slate-200">
                <button
                  onClick={() => {
                    setIsViewPatientModalOpen(false)
                    handleReferPatient(selectedPatient)
                  }}
                  className="px-4 py-2 bg-purple-100 text-purple-700 rounded-lg hover:bg-purple-200 transition-colors"
                >
                  Refer to Specialist
                </button>
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

        {/* Refer Patient Modal */}
        <Modal
          isOpen={isReferPatientModalOpen}
          onClose={() => setIsReferPatientModalOpen(false)}
          title={`Refer Patient: ${selectedPatient?.fullName || ''}`}
          size="md"
        >
          <form onSubmit={submitReferral} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Select Specialist *</label>
              <select
                required
                value={referralData.specialist}
                onChange={(e) => setReferralData({ ...referralData, specialist: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Choose a specialist...</option>
                {mockSpecialists.map((spec) => (
                  <option key={spec.id} value={spec.fullName}>
                    {spec.fullName} - {spec.specialty}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Reason for Referral *</label>
              <input
                type="text"
                required
                value={referralData.reason}
                onChange={(e) => setReferralData({ ...referralData, reason: e.target.value })}
                placeholder="e.g., Further evaluation of symptoms"
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Urgency</label>
              <select
                value={referralData.urgency}
                onChange={(e) => setReferralData({ ...referralData, urgency: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="Normal">Normal</option>
                <option value="Urgent">Urgent</option>
                <option value="Emergency">Emergency</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Additional Notes</label>
              <textarea
                value={referralData.notes}
                onChange={(e) => setReferralData({ ...referralData, notes: e.target.value })}
                rows={3}
                placeholder="Any additional information for the specialist..."
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="flex justify-end gap-3 mt-6">
              <button
                type="button"
                onClick={() => setIsReferPatientModalOpen(false)}
                className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
              >
                Submit Referral
              </button>
            </div>
          </form>
        </Modal>

        {/* Add Notes Modal */}
        <Modal
          isOpen={isAddNotesModalOpen}
          onClose={() => setIsAddNotesModalOpen(false)}
          title={`Add Medical Notes: ${selectedPatient?.fullName || ''}`}
          size="md"
        >
          <form onSubmit={submitNotes} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Medical Notes *</label>
              <textarea
                required
                value={newNotes}
                onChange={(e) => setNewNotes(e.target.value)}
                rows={6}
                placeholder="Enter medical notes, observations, or treatment updates..."
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="flex justify-end gap-3 mt-6">
              <button
                type="button"
                onClick={() => setIsAddNotesModalOpen(false)}
                className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
              >
                Save Notes
              </button>
            </div>
          </form>
        </Modal>

        {/* Edit Patient Modal */}
        <Modal
          isOpen={isEditPatientModalOpen}
          onClose={() => setIsEditPatientModalOpen(false)}
          title={`Edit Patient: ${selectedPatient?.fullName || ''}`}
          size="lg"
        >
          {editPatientData && (
            <form onSubmit={submitEditPatient} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Full Name *</label>
                  <input
                    type="text"
                    required
                    value={editPatientData.fullName}
                    onChange={(e) => setEditPatientData({ ...editPatientData, fullName: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Age *</label>
                  <input
                    type="number"
                    required
                    value={editPatientData.age}
                    onChange={(e) => {
                      const value = e.target.value
                      setEditPatientData({
                        ...editPatientData,
                        age: value === '' ? '' : parseInt(value),
                      })
                    }}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Gender</label>
                  <select
                    value={editPatientData.gender}
                    onChange={(e) => setEditPatientData({ ...editPatientData, gender: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Blood Type</label>
                  <select
                    value={editPatientData.bloodType}
                    onChange={(e) => setEditPatientData({ ...editPatientData, bloodType: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
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
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Phone</label>
                  <input
                    type="tel"
                    value={editPatientData.phone}
                    onChange={(e) => setEditPatientData({ ...editPatientData, phone: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
                  <input
                    type="email"
                    value={editPatientData.email}
                    onChange={(e) => setEditPatientData({ ...editPatientData, email: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Primary Condition *</label>
                <input
                  type="text"
                  required
                  value={editPatientData.condition}
                  onChange={(e) => setEditPatientData({ ...editPatientData, condition: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Known Allergies</label>
                <input
                  type="text"
                  value={editPatientData.allergies}
                  onChange={(e) => setEditPatientData({ ...editPatientData, allergies: e.target.value })}
                  placeholder="e.g., Penicillin, Sulfa drugs"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Current Medications</label>
                <input
                  type="text"
                  value={editPatientData.currentMedications}
                  onChange={(e) => setEditPatientData({ ...editPatientData, currentMedications: e.target.value })}
                  placeholder="Comma-separated list (e.g., Lisinopril 10mg, Aspirin 81mg)"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Medical History</label>
                <textarea
                  value={editPatientData.medicalHistory}
                  onChange={(e) => setEditPatientData({ ...editPatientData, medicalHistory: e.target.value })}
                  rows={3}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div className="flex justify-end gap-3 mt-6">
                <button
                  type="button"
                  onClick={() => setIsEditPatientModalOpen(false)}
                  className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Save Changes
                </button>
              </div>
            </form>
          )}
        </Modal>
      </div>
    </DashboardLayout>
  )
}
