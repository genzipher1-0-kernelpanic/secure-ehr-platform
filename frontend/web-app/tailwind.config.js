/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#1D4ED8',
          hover: '#1E40AF',
          light: '#3B82F6',
          dark: '#1E3A8A',
        },
        secondary: {
          DEFAULT: '#0EA5A4',
          hover: '#0D9488',
          light: '#14B8A6',
          dark: '#0F766E',
        },
        background: '#F8FAFC',
        surface: '#FFFFFF',
        text: {
          primary: '#0F172A',
          secondary: '#475569',
          disabled: '#94A3B8',
        },
        border: {
          DEFAULT: '#E2E8F0',
          hover: '#CBD5E1',
        },
        success: {
          DEFAULT: '#16A34A',
          light: '#22C55E',
          bg: '#F0FDF4',
        },
        warning: {
          DEFAULT: '#F59E0B',
          light: '#FBBF24',
          bg: '#FFFBEB',
        },
        error: {
          DEFAULT: '#DC2626',
          light: '#EF4444',
          bg: '#FEF2F2',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
