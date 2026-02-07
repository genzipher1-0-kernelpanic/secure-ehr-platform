import { z } from 'zod';
import { PASSWORD_REQUIREMENTS } from '@/lib/config/constants';

export const loginSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(PASSWORD_REQUIREMENTS.minLength, 
    `Password must be at least ${PASSWORD_REQUIREMENTS.minLength} characters`),
  mfaCode: z.string().optional(),
});

export const registerSchema = z.object({
  username: z.string()
    .min(3, 'Username must be at least 3 characters')
    .max(50, 'Username must be less than 50 characters')
    .regex(/^[a-zA-Z0-9_]+$/, 'Username can only contain letters, numbers, and underscores'),
  
  email: z.string()
    .email('Invalid email address')
    .min(1, 'Email is required'),
  
  fullName: z.string()
    .min(2, 'Full name must be at least 2 characters')
    .max(255, 'Full name must be less than 255 characters'),
  
  password: z.string()
    .min(PASSWORD_REQUIREMENTS.minLength, `Password must be at least ${PASSWORD_REQUIREMENTS.minLength} characters`)
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
    .regex(/[0-9]/, 'Password must contain at least one number')
    .regex(/[^A-Za-z0-9]/, 'Password must contain at least one special character'),
  
  confirmPassword: z.string(),
  
  role: z.enum(['DOCTOR', 'NURSE', 'PATIENT']),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
