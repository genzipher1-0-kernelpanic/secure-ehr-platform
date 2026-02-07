import { useState } from 'react'
import DashboardLayout from '@/components/layout/DashboardLayout'
import StatsCard from '@/components/common/StatsCard'
import DataTable from '@/components/common/DataTable'
import Modal from '@/components/common/Modal'
import { httpClient } from '@/lib/http/client'
import { API_ENDPOINTS } from '@/lib/config/constants'

/* ─── role limits (must match backend AuthServiceImpl) ─── */
const ADMIN_LIMIT = 10

/* ─── sidebar nav ─── */
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
    path: '/system-admin/admins',
    label: 'Admins',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
      </svg>
    ),
  },
  {
    path: '/system-admin/system-health',
    label: 'System Health',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
  {
    path: '/system-admin/logs',
    label: 'Logs',
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
    ),
  },
  {
    label: 'Eureka Dashboard',
    href: 'http://localhost:8761',
    external: true,
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9" />
      </svg>
    ),
  },
  {
    label: 'Zipkin Tracing',
    href: 'http://localhost:9411',
    external: true,
    icon: (
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
    ),
  },
]

/* ─── pre-seeded DB users ─── */
const INITIAL_ADMINS = [
  { id: '7', email: 'recept@gmail.com', role: 'ADMIN', status: 'Active', createdAt: '2025-01-01' },
]

/* ─── service monitoring ─── */
const SERVICE_LINKS = [
  { name: 'Eureka Dashboard', desc: 'Service Discovery & Registry', url: 'http://localhost:8761', color: 'blue' },
  { name: 'Zipkin Tracing', desc: 'Distributed Tracing & Logs', url: 'http://localhost:9411', color: 'purple' },
  { name: 'API Gateway', desc: 'Gateway Health & Routing', url: 'http://localhost:8080/actuator/health', color: 'green' },
]

/* ─── table columns ─── */
const adminCols = [
  { key: 'email', label: 'Email' },
  { key: 'role', label: 'Role' },
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
  { key: 'createdAt', label: 'Created' },
]

export default function SystemAdminDashboard() {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState(null)
  const [admins, setAdmins] = useState(INITIAL_ADMINS)
  const [newAdmin, setNewAdmin] = useState({ email: '' })
  const [isLoading, setIsLoading] = useState(false)
  const [successMessage, setSuccessMessage] = useState(null)
  const [errorMessage, setErrorMessage] = useState(null)

  const isLimitReached = admins.length >= ADMIN_LIMIT

  const handleAddAdmin = async (e) => {
    e.preventDefault()
    setIsLoading(true)
    setErrorMessage(null)
    setSuccessMessage(null)

    try {
      const response = await httpClient.post(API_ENDPOINTS.AUTH.REGISTER, {
        email: newAdmin.email,
        role: 'ADMIN',
      })

      const registeredAdmin = {
        id: String(response.id),
        email: response.email,
        generatedPassword: response.password,
        role: response.role,
        status: 'Active',
        createdAt: new Date().toISOString().split('T')[0],
      }

      setAdmins([...admins, registeredAdmin])
      setSuccessMessage(
        `Admin created!\nEmail: ${response.email}\nTemporary Password: ${response.password}\n\nPlease save this password — it won't be shown again.`
      )
      setNewAdmin({ email: '' })
      setIsModalOpen(false)
    } catch (error) {
      setErrorMessage(error.message || 'Failed to register admin')
    } finally {
      setIsLoading(false)
    }
  }

  const handleDeleteAdmin = async (admin) => {
    setIsLoading(true)
    setErrorMessage(null)
    try {
      await httpClient.delete(`/auth/delete/${admin.id}`)
      setAdmins(admins.filter((a) => a.id !== admin.id))
      setSuccessMessage(`Admin ${admin.email} has been deleted.`)
      setConfirmDelete(null)
    } catch (error) {
      setErrorMessage(error.message || 'Failed to delete admin')
      setConfirmDelete(null)
    } finally {
      setIsLoading(false)
    }
  }

  const handleResetPassword = async (admin) => {
    setIsLoading(true)
    setErrorMessage(null)
    try {
      await httpClient.post(API_ENDPOINTS.AUTH.FORGOT_PASSWORD, { email: admin.email })
      setSuccessMessage(`Password reset email sent to ${admin.email}.`)
    } catch (error) {
      setErrorMessage(error.message || 'Failed to send password reset')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <DashboardLayout navItems={navItems} title="System Admin">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">System Admin Dashboard</h1>
            <p className="text-slate-500 mt-1">Manage hospital admins and system health</p>
          </div>
          <button
            onClick={() => setIsModalOpen(true)}
            disabled={isLimitReached}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            title={isLimitReached ? `Admin limit reached (${ADMIN_LIMIT})` : 'Add Admin'}
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
            Add Admin
          </button>
        </div>

        {/* Limit Warning */}
        {isLimitReached && (
          <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 flex items-center gap-3">
            <svg className="w-5 h-5 text-amber-600 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
            <p className="text-sm text-amber-800">
              <strong>Admin limit reached.</strong> Maximum {ADMIN_LIMIT} Admins allowed.
            </p>
          </div>
        )}

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

        {/* Error Banner */}
        {errorMessage && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <div className="flex items-start justify-between">
              <p className="text-sm text-red-700">{errorMessage}</p>
              <button onClick={() => setErrorMessage(null)} className="text-red-500 hover:text-red-700">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
        )}

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-1 gap-4 max-w-sm">
          <StatsCard
            title="Admins"
            value={`${admins.length} / ${ADMIN_LIMIT}`}
            color={isLimitReached ? 'orange' : 'blue'}
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            }
          />
        </div>

        {/* Service Monitoring — Quick Access */}
        <div>
          <h2 className="text-lg font-semibold text-slate-900 mb-3">Service Monitoring</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {SERVICE_LINKS.map((svc) => (
              <a
                key={svc.name}
                href={svc.url}
                target="_blank"
                rel="noopener noreferrer"
                className={`block p-5 rounded-xl border transition-all hover:shadow-md ${
                  svc.color === 'blue'   ? 'bg-blue-50 border-blue-200 hover:border-blue-400' :
                  svc.color === 'purple' ? 'bg-purple-50 border-purple-200 hover:border-purple-400' :
                                           'bg-green-50 border-green-200 hover:border-green-400'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <h3 className={`font-semibold ${
                    svc.color === 'blue' ? 'text-blue-800' : svc.color === 'purple' ? 'text-purple-800' : 'text-green-800'
                  }`}>{svc.name}</h3>
                  <svg className="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                  </svg>
                </div>
                <p className="text-sm text-slate-600">{svc.desc}</p>
              </a>
            ))}
          </div>
        </div>

        {/* Admins Table */}
        <div>
          <h2 className="text-lg font-semibold text-slate-900 mb-4">Admins</h2>
          <DataTable
            columns={adminCols}
            data={admins}
            emptyMessage="No admins found"
            actions={(row) => (
              <div className="flex gap-2">
                <button
                  onClick={() => handleResetPassword(row)}
                  disabled={isLoading}
                  className="px-3 py-1 text-xs font-medium rounded bg-blue-100 text-blue-700 hover:bg-blue-200 disabled:opacity-50"
                >
                  Reset Password
                </button>
                <button
                  onClick={() => setConfirmDelete(row)}
                  disabled={isLoading}
                  className="px-3 py-1 text-xs font-medium rounded bg-red-100 text-red-700 hover:bg-red-200 disabled:opacity-50"
                >
                  Delete
                </button>
              </div>
            )}
          />
        </div>

        {/* Delete Confirmation Modal */}
        <Modal isOpen={!!confirmDelete} onClose={() => setConfirmDelete(null)} title="Confirm Delete">
          <div className="space-y-4">
            <p className="text-sm text-slate-600">
              Are you sure you want to permanently delete <strong>{confirmDelete?.email}</strong>? This action cannot be undone.
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setConfirmDelete(null)}
                className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                disabled={isLoading}
              >
                Cancel
              </button>
              <button
                onClick={() => handleDeleteAdmin(confirmDelete)}
                disabled={isLoading}
                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 flex items-center gap-2"
              >
                {isLoading && (
                  <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                )}
                {isLoading ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </Modal>

        {/* Add Admin Modal */}
        <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Add Admin">
          <form onSubmit={handleAddAdmin} className="space-y-4">
            <p className="text-sm text-slate-500">
              Enter the email address for the new Admin. A temporary password will be auto-generated.
            </p>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
              <input
                type="email"
                required
                value={newAdmin.email}
                onChange={(e) => setNewAdmin({ ...newAdmin, email: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-slate-900 placeholder-slate-400"
                placeholder="Enter email address"
                disabled={isLoading}
              />
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <button
                type="button"
                onClick={() => setIsModalOpen(false)}
                className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                disabled={isLoading}
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isLoading}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                {isLoading && (
                  <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                )}
                {isLoading ? 'Registering...' : 'Add Admin'}
              </button>
            </div>
          </form>
        </Modal>
      </div>
    </DashboardLayout>
  )
}
