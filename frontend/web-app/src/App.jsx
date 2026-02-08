import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from '@/context/AuthContext'
import { ProtectedRoute, PublicRoute } from '@/components/auth/ProtectedRoute'
import { ROLES } from '@/lib/config/constants'
import LoginPage from './features/auth/pages/LoginPage'
import {
  SuperAdminDashboard,
  SystemAdminDashboard,
  ReceptionistDashboard,
  DoctorDashboard,
} from './features/dashboard'

function AppRoutes() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route
        path="/login"
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        }
      />
      
      {/* Super Admin Routes */}
      <Route
        path="/super-admin/*"
        element={
          <ProtectedRoute allowedRoles={[ROLES.SUPER_ADMIN]}>
            <SuperAdminDashboard />
          </ProtectedRoute>
        }
      />
      
      {/* System Admin Routes */}
      <Route
        path="/system-admin/*"
        element={
          <ProtectedRoute allowedRoles={[ROLES.SYSTEM_ADMIN]}>
            <SystemAdminDashboard />
          </ProtectedRoute>
        }
      />
      
      {/* Admin Routes */}
      <Route
        path="/admin/*"
        element={
          <ProtectedRoute allowedRoles={[ROLES.ADMIN]}>
            <ReceptionistDashboard />
          </ProtectedRoute>
        }
      />
      
      {/* Doctor Routes */}
      <Route
        path="/doctor/*"
        element={
          // <ProtectedRoute allowedRoles={[ROLES.DOCTOR]}>
            <DoctorDashboard />
          // </ProtectedRoute>
        }
      />
      
      {/* Redirect root to login */}
      <Route path="/" element={<Navigate to="/login" replace />} />
      
      {/* Catch all - redirect to login */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}

export default App
