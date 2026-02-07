import { useState } from 'react'
import { useAuth } from '@/context/AuthContext'
import { httpClient } from '@/lib/http/client'
import { API_ENDPOINTS, AUTH_TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY } from '@/lib/config/constants'

// JWT decode helper
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

function LoginPage() {
  const { login } = useAuth()
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  })
  const [passwordSetup, setPasswordSetup] = useState({
    newPassword: '',
    confirmPassword: '',
  })
  const [errors, setErrors] = useState({})
  const [isLoading, setIsLoading] = useState(false)
  const [isFirstLogin, setIsFirstLogin] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    if (isFirstLogin) {
      setPasswordSetup((prev) => ({
        ...prev,
        [name]: value,
      }))
    } else {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }))
    }
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }))
    }
  }

  const validateLoginForm = () => {
    const newErrors = {}
    
    if (!formData.email) {
      newErrors.email = 'Email is required'
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email'
    }
    
    if (!formData.password) {
      newErrors.password = 'Password is required'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const validatePasswordSetup = () => {
    const newErrors = {}
    
    if (!passwordSetup.newPassword) {
      newErrors.newPassword = 'Password is required'
    } else if (passwordSetup.newPassword.length < 8) {
      newErrors.newPassword = 'Password must be at least 8 characters'
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(passwordSetup.newPassword)) {
      newErrors.newPassword = 'Password must contain uppercase, lowercase, and a number'
    }
    
    if (!passwordSetup.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password'
    } else if (passwordSetup.newPassword !== passwordSetup.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleLoginSubmit = async (e) => {
    e.preventDefault()
    
    if (!validateLoginForm()) return
    
    setIsLoading(true)
    
    try {
      // Call the login API
      const response = await httpClient.post(API_ENDPOINTS.AUTH.LOGIN, {
        email: formData.email,
        password: formData.password,
      })
      
      // Normalise field names — backend returns snake_case
      const accessToken = response.accessToken || response.access_token
      const refreshToken = response.refreshToken || response.refresh_token
      
      // Check if it's a first login (user needs to set up password)
      if (response.isFirstLogin) {
        setIsFirstLogin(true)
        setIsLoading(false)
        return
      }
      
      // Decode JWT to get user info
      const decoded = decodeJWT(accessToken)
      
      // Extract user data from response or decode from token
      const userData = response.user || {
        id: decoded.uid || decoded.sub || decoded.userId,
        email: decoded.sub || decoded.email || formData.email,
        fullName: decoded.fullName || decoded.name || '',
        role: decoded.role || response.role,
        mfaEnabled: decoded.mfaEnabled || false,
      }
      
      // Use the auth context login to store tokens and redirect
      await login(accessToken, refreshToken, userData)
      
    } catch (error) {
      console.error('Login error:', error)
      setErrors({ submit: error.message || 'Invalid credentials. Please try again.' })
    } finally {
      setIsLoading(false)
    }
  }

  const handlePasswordSetupSubmit = async (e) => {
    e.preventDefault()
    
    if (!validatePasswordSetup()) return
    
    setIsLoading(true)
    
    try {
      // Call API to set up password for first-time login
      const response = await httpClient.post('/auth/setup-password', {
        email: formData.email,
        currentPassword: formData.password,
        newPassword: passwordSetup.newPassword,
      })
      
      // Decode JWT to get user info
      const decoded = decodeJWT(response.accessToken)
      
      const userData = response.user || {
        id: decoded.sub || decoded.userId,
        email: decoded.email || formData.email,
        fullName: decoded.fullName || decoded.name,
        role: decoded.role,
        mfaEnabled: decoded.mfaEnabled || false,
      }
      
      // Use the auth context login to store tokens and redirect
      await login(response.accessToken, response.refreshToken, userData)
      
    } catch (error) {
      console.error('Password setup error:', error)
      setErrors({ submit: error.message || 'Failed to set password. Please try again.' })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo and Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-xl mb-4 shadow-lg">
            <svg 
              className="w-8 h-8 text-white" 
              fill="none" 
              stroke="currentColor" 
              viewBox="0 0 24 24"
            >
              <path 
                strokeLinecap="round" 
                strokeLinejoin="round" 
                strokeWidth={2} 
                d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" 
              />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-slate-900">Secure EHR Platform</h1>
          <p className="text-slate-500 mt-2">
            {isFirstLogin 
              ? 'Set up your password to continue' 
              : 'Sign in to access your healthcare records'}
          </p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-xl shadow-lg p-8">
          {!isFirstLogin ? (
            /* Login Form */
            <form onSubmit={handleLoginSubmit} className="space-y-6">
              {/* Email Field */}
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-slate-700 mb-2">
                  Email Address
                </label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  className={`w-full px-4 py-3 bg-slate-50 border rounded-lg text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all ${
                    errors.email ? 'border-red-500 focus:ring-red-500' : 'border-slate-200'
                  }`}
                  placeholder="you@example.com"
                  autoComplete="email"
                />
                {errors.email && (
                  <p className="mt-1.5 text-sm text-red-600">{errors.email}</p>
                )}
              </div>

              {/* Password Field */}
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-slate-700 mb-2">
                  Password
                </label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className={`w-full px-4 py-3 bg-slate-50 border rounded-lg text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all ${
                    errors.password ? 'border-red-500 focus:ring-red-500' : 'border-slate-200'
                  }`}
                  placeholder="••••••••"
                  autoComplete="current-password"
                />
                {errors.password && (
                  <p className="mt-1.5 text-sm text-red-600">{errors.password}</p>
                )}
              </div>

              {/* Remember Me & Forgot Password */}
              <div className="flex items-center justify-between">
                <label className="flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    className="w-4 h-4 text-blue-600 border-slate-300 rounded focus:ring-blue-500"
                  />
                  <span className="ml-2 text-sm text-slate-600">Remember me</span>
                </label>
                <a href="/forgot-password" className="text-sm text-blue-600 hover:text-blue-700 font-medium">
                  Forgot password?
                </a>
              </div>

              {/* Submit Error */}
              {errors.submit && (
                <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                  <p className="text-sm text-red-700">{errors.submit}</p>
                </div>
              )}

              {/* Submit Button */}
              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-3 px-4 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg shadow-md hover:shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <span className="flex items-center justify-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Signing in...
                  </span>
                ) : (
                  'Sign In'
                )}
              </button>
            </form>
          ) : (
            /* Password Setup Form (First Login) */
            <form onSubmit={handlePasswordSetupSubmit} className="space-y-6">
              <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg mb-2">
                <p className="text-sm text-blue-700">
                  <strong>Welcome!</strong> This is your first login. Please set up a secure password to continue.
                </p>
              </div>

              {/* New Password Field */}
              <div>
                <label htmlFor="newPassword" className="block text-sm font-medium text-slate-700 mb-2">
                  New Password
                </label>
                <input
                  type="password"
                  id="newPassword"
                  name="newPassword"
                  value={passwordSetup.newPassword}
                  onChange={handleChange}
                  className={`w-full px-4 py-3 bg-slate-50 border rounded-lg text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all ${
                    errors.newPassword ? 'border-red-500 focus:ring-red-500' : 'border-slate-200'
                  }`}
                  placeholder="Enter new password"
                  autoComplete="new-password"
                />
                {errors.newPassword && (
                  <p className="mt-1.5 text-sm text-red-600">{errors.newPassword}</p>
                )}
                <p className="mt-1.5 text-xs text-slate-500">
                  Must be at least 8 characters with uppercase, lowercase, and a number
                </p>
              </div>

              {/* Confirm Password Field */}
              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-slate-700 mb-2">
                  Confirm Password
                </label>
                <input
                  type="password"
                  id="confirmPassword"
                  name="confirmPassword"
                  value={passwordSetup.confirmPassword}
                  onChange={handleChange}
                  className={`w-full px-4 py-3 bg-slate-50 border rounded-lg text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all ${
                    errors.confirmPassword ? 'border-red-500 focus:ring-red-500' : 'border-slate-200'
                  }`}
                  placeholder="Confirm your password"
                  autoComplete="new-password"
                />
                {errors.confirmPassword && (
                  <p className="mt-1.5 text-sm text-red-600">{errors.confirmPassword}</p>
                )}
              </div>

              {/* Submit Error */}
              {errors.submit && (
                <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                  <p className="text-sm text-red-700">{errors.submit}</p>
                </div>
              )}

              {/* Submit Button */}
              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-3 px-4 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg shadow-md hover:shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <span className="flex items-center justify-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Setting up...
                  </span>
                ) : (
                  'Set Password & Continue'
                )}
              </button>

              {/* Back to Login */}
              <button
                type="button"
                onClick={() => setIsFirstLogin(false)}
                className="w-full text-sm text-slate-500 hover:text-slate-700"
              >
                ← Back to login
              </button>
            </form>
          )}
        </div>

        {/* Footer */}
        <p className="text-center text-xs text-slate-400 mt-8">
          Protected by industry-standard encryption.
          <br />
          Your health data is secure with us.
        </p>
      </div>
    </div>
  )
}

export default LoginPage
