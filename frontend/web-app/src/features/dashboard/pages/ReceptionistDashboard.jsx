import { useState } from 'react'
import DashboardLayout from '@/components/layout/DashboardLayout'
import StatsCard from '@/components/common/StatsCard'
import DataTable from '@/components/common/DataTable'
import Modal from '@/components/common/Modal'

const navItems = [
  {
    path: '/receptionist',
    label: 'Dashboard',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
      </svg>
    ),
  },
  {
    path: '/receptionist/patients',
    label: 'Patients',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
    ),
  },
  {
    path: '/receptionist/appointments',
    label: 'Appointments',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
  },
  {
    path: '/receptionist/doctors',
    label: 'Doctors',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
]

// Mock data
const mockPatients = [
  { id: '1', fullName: 'Alice Thompson', dateOfBirth: '1985-03-15', phone: '+1 555-0101', email: 'alice.t@email.com', bloodType: 'A+', assignedDoctor: 'Dr. Robert Miller', lastVisit: '2026-02-01' },
  { id: '2', fullName: 'Bob Martinez', dateOfBirth: '1978-08-22', phone: '+1 555-0102', email: 'bob.m@email.com', bloodType: 'O-', assignedDoctor: 'Dr. Jennifer White', lastVisit: '2026-01-28' },
  { id: '3', fullName: 'Carol Anderson', dateOfBirth: '1990-11-30', phone: '+1 555-0103', email: 'carol.a@email.com', bloodType: 'B+', assignedDoctor: 'Dr. David Lee', lastVisit: '2026-02-05' },
  { id: '4', fullName: 'Daniel Kim', dateOfBirth: '1965-05-10', phone: '+1 555-0104', email: 'daniel.k@email.com', bloodType: 'AB+', assignedDoctor: 'Dr. Robert Miller', lastVisit: '2026-01-15' },
]

const mockDoctors = [
  { id: '1', fullName: 'Dr. Robert Miller', specialty: 'Cardiology' },
  { id: '2', fullName: 'Dr. Jennifer White', specialty: 'Neurology' },
  { id: '3', fullName: 'Dr. David Lee', specialty: 'Orthopedics' },
  { id: '4', fullName: 'Dr. Sarah Johnson', specialty: 'Pediatrics' },
]

const patientColumns = [
  { key: 'fullName', label: 'Patient Name' },
  { key: 'dateOfBirth', label: 'Date of Birth' },
  { key: 'phone', label: 'Phone' },
  { key: 'bloodType', label: 'Blood Type' },
  { key: 'assignedDoctor', label: 'Assigned Doctor' },
  { key: 'lastVisit', label: 'Last Visit' },
]

export default function ReceptionistDashboard() {
  const [isAddPatientModalOpen, setIsAddPatientModalOpen] = useState(false)
  const [isViewPatientModalOpen, setIsViewPatientModalOpen] = useState(false)
  const [patients, setPatients] = useState(mockPatients)
  const [selectedPatient, setSelectedPatient] = useState(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [newPatient, setNewPatient] = useState({
    fullName: '',
    dateOfBirth: '',
    phone: '',
    email: '',
    bloodType: '',
    address: '',
    emergencyContact: '',
    emergencyPhone: '',
    medicalHistory: '',
    allergies: '',
    assignedDoctor: '',
  })

  const filteredPatients = patients.filter(
    (patient) =>
      patient.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      patient.phone.includes(searchTerm) ||
      patient.email.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const handleAddPatient = (e) => {
    e.preventDefault()
    const patient = {
      id: String(patients.length + 1),
      ...newPatient,
      lastVisit: new Date().toISOString().split('T')[0],
    }
    setPatients([...patients, patient])
    setNewPatient({
      fullName: '',
      dateOfBirth: '',
      phone: '',
      email: '',
      bloodType: '',
      address: '',
      emergencyContact: '',
      emergencyPhone: '',
      medicalHistory: '',
      allergies: '',
      assignedDoctor: '',
    })
    setIsAddPatientModalOpen(false)
  }

  const handleViewPatient = (patient) => {
    setSelectedPatient(patient)
    setIsViewPatientModalOpen(true)
  }

  return (
    <DashboardLayout navItems={navItems} title="Receptionist">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Reception Dashboard</h1>
            <p className="text-slate-500 mt-1">Register and manage patient information</p>
          </div>
          <button
            onClick={() => setIsAddPatientModalOpen(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
            Register Patient
          </button>
        </div>

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
            title="Today's Registrations"
            value="12"
            color="green"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
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
            value={mockDoctors.length}
            color="orange"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            }
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
                  {mockDoctors.map((doc) => (
                    <option key={doc.id} value={doc.fullName}>
                      {doc.fullName} - {doc.specialty}
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
              <button
                type="button"
                onClick={() => setIsAddPatientModalOpen(false)}
                className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                Register Patient
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
                  <p className="text-sm text-slate-500">Phone</p>
                  <p className="font-medium text-slate-900">{selectedPatient.phone}</p>
                </div>
                <div>
                  <p className="text-sm text-slate-500">Email</p>
                  <p className="font-medium text-slate-900">{selectedPatient.email || 'N/A'}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-slate-500">Blood Type</p>
                  <p className="font-medium text-slate-900">{selectedPatient.bloodType}</p>
                </div>
                <div>
                  <p className="text-sm text-slate-500">Assigned Doctor</p>
                  <p className="font-medium text-slate-900">{selectedPatient.assignedDoctor}</p>
                </div>
              </div>
              <div>
                <p className="text-sm text-slate-500">Last Visit</p>
                <p className="font-medium text-slate-900">{selectedPatient.lastVisit}</p>
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
