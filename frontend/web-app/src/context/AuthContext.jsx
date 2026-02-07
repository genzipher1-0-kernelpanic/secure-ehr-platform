import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { AUTH_TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY, ROLES } from '@/lib/config/constants'

const AuthContext = createContext(null)

// JWT decode helper (base64url decoding)
function decodeJWT(token) {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('Failed to decode JWT:', error)
    return null
  }
}

// Get dashboard path based on role
export function getDashboardPath(role) {
  switch (role) {
    case ROLES.SUPER_ADMIN:
      return '/super-admin'
    case ROLES.SYSTEM_ADMIN:
      return '/system-admin'
    case ROLES.RECEPTIONIST:
      return '/receptionist'
    case ROLES.DOCTOR:
      return '/doctor'
    default:
      return '/login'
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  // Initialize auth state from localStorage
  useEffect(() => {
    const storedToken = localStorage.getItem(AUTH_TOKEN_KEY)
    const storedUser = localStorage.getItem(USER_KEY)
    
    if (storedToken && storedUser) {
      const decoded = decodeJWT(storedToken)
      if (decoded && decoded.exp * 1000 > Date.now()) {
        setToken(storedToken)
        setUser(JSON.parse(storedUser))
      } else {
        // Token expired, clear storage
        logout()
      }
    }
    setLoading(false)
  }, [])

  const login = useCallback(async (accessToken, refreshToken, userData) => {
    localStorage.setItem(AUTH_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(userData))
    
    setToken(accessToken)
    setUser(userData)
    
    // Navigate to appropriate dashboard
    const dashboardPath = getDashboardPath(userData.role)
    navigate(dashboardPath)
  }, [navigate])

  const logout = useCallback(() => {
    localStorage.removeItem(AUTH_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setToken(null)
    setUser(null)
    navigate('/login')
  }, [navigate])

  const isAuthenticated = useCallback(() => {
    if (!token) return false
    const decoded = decodeJWT(token)
    return decoded && decoded.exp * 1000 > Date.now()
  }, [token])

  const hasRole = useCallback((requiredRole) => {
    if (!user) return false
    if (Array.isArray(requiredRole)) {
      return requiredRole.includes(user.role)
    }
    return user.role === requiredRole
  }, [user])

  const value = {
    user,
    token,
    loading,
    login,
    logout,
    isAuthenticated,
    hasRole,
    decodeJWT,
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export default AuthContext
