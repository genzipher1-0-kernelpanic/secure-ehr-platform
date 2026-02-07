import { useState } from 'react'
import DashboardLayout from '@/components/layout/DashboardLayout'
import StatsCard from '@/components/common/StatsCard'
import DataTable from '@/components/common/DataTable'
import Modal from '@/components/common/Modal'

const navItems = [
  {
    path: '/system-admin',
    label: 'Dashboard',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
      </svg>
    ),
  },
  {
    path: '/system-admin/receptionists',
    label: 'Receptionists',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
      </svg>
    ),
  },
  {
    path: '/system-admin/doctors',
    label: 'Doctors',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
  {
    path: '/system-admin/reports',
    label: 'Reports',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
    ),
  },
]

// Mock data
const mockReceptionists = [
  { id: '1', fullName: 'Emily Davis', email: 'emily.d@hospital.com', status: 'Active', shift: 'Morning', createdAt: '2025-11-20' },
  { id: '2', fullName: 'James Brown', email: 'james.b@hospital.com', status: 'Active', shift: 'Evening', createdAt: '2025-12-05' },
  { id: '3', fullName: 'Lisa Chen', email: 'lisa.c@hospital.com', status: 'Active', shift: 'Night', createdAt: '2026-01-10' },
]

const mockDoctors = [
  { id: '1', fullName: 'Dr. Robert Miller', email: 'dr.miller@hospital.com', specialty: 'Cardiology', status: 'Active' },
  { id: '2', fullName: 'Dr. Jennifer White', email: 'dr.white@hospital.com', specialty: 'Neurology', status: 'Active' },
  { id: '3', fullName: 'Dr. David Lee', email: 'dr.lee@hospital.com', specialty: 'Orthopedics', status: 'On Leave' },
]

export default function SystemAdminDashboard() {
  const [isAddReceptionistModalOpen, setIsAddReceptionistModalOpen] = useState(false)
  const [isAddDoctorModalOpen, setIsAddDoctorModalOpen] = useState(false)
  const [receptionists, setReceptionists] = useState(mockReceptionists)
  const [doctors, setDoctors] = useState(mockDoctors)
  const [newReceptionist, setNewReceptionist] = useState({ fullName: '', email: '', shift: 'Morning', password: '' })
  const [newDoctor, setNewDoctor] = useState({ fullName: '', email: '', specialty: '', password: '' })

  const receptionistColumns = [
    { key: 'fullName', label: 'Name' },
    { key: 'email', label: 'Email' },
    { key: 'shift', label: 'Shift' },
    {
      key: 'status',
      label: 'Status',
      render: (value) => (
        <span className={`px-2 py-1 text-xs font-medium rounded-full ${
          value === 'Active' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
        }`}>
          {value}
        </span>
      ),
    },
  ]

  const doctorColumns = [
    { key: 'fullName', label: 'Name' },
    { key: 'email', label: 'Email' },
    { key: 'specialty', label: 'Specialty' },
    {
      key: 'status',
      label: 'Status',
      render: (value) => (
        <span className={`px-2 py-1 text-xs font-medium rounded-full ${
          value === 'Active' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
        }`}>
          {value}
        </span>
      ),
    },
  ]

  const handleAddReceptionist = (e) => {
    e.preventDefault()
    const receptionist = {
      id: String(receptionists.length + 1),
      ...newReceptionist,
      status: 'Active',
      createdAt: new Date().toISOString().split('T')[0],
    }
    setReceptionists([...receptionists, receptionist])
    setNewReceptionist({ fullName: '', email: '', shift: 'Morning', password: '' })
    setIsAddReceptionistModalOpen(false)
  }

  const handleAddDoctor = (e) => {
    e.preventDefault()
    const doctor = {
      id: String(doctors.length + 1),
      ...newDoctor,
      status: 'Active',
    }
    setDoctors([...doctors, doctor])
    setNewDoctor({ fullName: '', email: '', specialty: '', password: '' })
    setIsAddDoctorModalOpen(false)
  }

  return (
    <DashboardLayout navItems={navItems} title="System Admin">
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold text-slate-900">System Admin Dashboard</h1>
          <p className="text-slate-500 mt-1">Manage receptionists and doctors</p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <StatsCard
            title="Active Receptionists"
            value={receptionists.filter((r) => r.status === 'Active').length}
            color="blue"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
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
            title="Registered Patients"
            value="1,234"
            color="purple"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            }
          />
          <StatsCard
            title="Today's Appointments"
            value="48"
            color="orange"
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            }
          />
        </div>

        {/* Receptionists Section */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-slate-900">Receptionists</h2>
            <button
              onClick={() => setIsAddReceptionistModalOpen(true)}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              Add Receptionist
            </button>
          </div>
          <DataTable
            columns={receptionistColumns}
            data={receptionists}
            emptyMessage="No receptionists found"
            actions={(row) => (
              <>
                <button className="px-3 py-1 text-xs font-medium rounded bg-slate-100 text-slate-700 hover:bg-slate-200">
                  Edit
                </button>
                <button className="px-3 py-1 text-xs font-medium rounded bg-red-100 text-red-700 hover:bg-red-200">
                  Remove
                </button>
              </>
            )}
          />
        </div>

        {/* Doctors Section */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-slate-900">Doctors</h2>
            <button
              onClick={() => setIsAddDoctorModalOpen(true)}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              Add Doctor
            </button>
          </div>
          <DataTable
            columns={doctorColumns}
            data={doctors}
            emptyMessage="No doctors found"
            actions={(row) => (
              <>
                <button className="px-3 py-1 text-xs font-medium rounded bg-slate-100 text-slate-700 hover:bg-slate-200">
                  Edit
                </button>
                <button className="px-3 py-1 text-xs font-medium rounded bg-red-100 text-red-700 hover:bg-red-200">
                  Remove
                </button>
              </>
            )}
          />
        </div>

        {/* Add Receptionist Modal */}
        <Modal isOpen={isAddReceptionistModalOpen} onClose={() => setIsAddReceptionistModalOpen(false)} title="Add Receptionist">
          <form onSubmit={handleAddReceptionist} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Full Name</label>
              <input
                type="text"
                required
                value={newReceptionist.fullName}
                onChange={(e) => setNewReceptionist({ ...newReceptionist, fullName: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
              <input
                type="email"
                required
                value={newReceptionist.email}
                onChange={(e) => setNewReceptionist({ ...newReceptionist, email: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Shift</label>
              <select
                value={newReceptionist.shift}
                onChange={(e) => setNewReceptionist({ ...newReceptionist, shift: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="Morning">Morning</option>
                <option value="Evening">Evening</option>
                <option value="Night">Night</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Temporary Password</label>
              <input
                type="password"
                required
                value={newReceptionist.password}
                onChange={(e) => setNewReceptionist({ ...newReceptionist, password: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <button
                type="button"
                onClick={() => setIsAddReceptionistModalOpen(false)}
                className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                Add Receptionist
              </button>
            </div>
          </form>
        </Modal>

        {/* Add Doctor Modal */}
        <Modal isOpen={isAddDoctorModalOpen} onClose={() => setIsAddDoctorModalOpen(false)} title="Add Doctor">
          <form onSubmit={handleAddDoctor} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Full Name</label>
              <input
                type="text"
                required
                value={newDoctor.fullName}
                onChange={(e) => setNewDoctor({ ...newDoctor, fullName: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
              <input
                type="email"
                required
                value={newDoctor.email}
                onChange={(e) => setNewDoctor({ ...newDoctor, email: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Specialty</label>
              <select
                required
                value={newDoctor.specialty}
                onChange={(e) => setNewDoctor({ ...newDoctor, specialty: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Select Specialty</option>
                <option value="Cardiology">Cardiology</option>
                <option value="Neurology">Neurology</option>
                <option value="Orthopedics">Orthopedics</option>
                <option value="Pediatrics">Pediatrics</option>
                <option value="Dermatology">Dermatology</option>
                <option value="General Medicine">General Medicine</option>
                <option value="Surgery">Surgery</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Temporary Password</label>
              <input
                type="password"
                required
                value={newDoctor.password}
                onChange={(e) => setNewDoctor({ ...newDoctor, password: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <button
                type="button"
                onClick={() => setIsAddDoctorModalOpen(false)}
                className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
              >
                Add Doctor
              </button>
            </div>
          </form>
        </Modal>
      </div>
    </DashboardLayout>
  )
}
