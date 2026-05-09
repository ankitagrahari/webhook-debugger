/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        mono: ['"JetBrains Mono"', '"Fira Code"', 'monospace'],
        sans: ['"DM Sans"', 'system-ui', 'sans-serif'],
      },
      colors: {
        surface: {
          0: '#0a0a0f',
          1: '#0f0f17',
          2: '#16161f',
          3: '#1e1e2a',
          4: '#26263a',
        },
        accent: {
          DEFAULT: '#7c6aff',
          hover: '#9b8dff',
          muted: '#7c6aff33',
        },
        border: '#2a2a3d',
        muted: '#6b6b8a',
        success: '#22c55e',
        warning: '#f59e0b',
        danger: '#ef4444',
        info: '#38bdf8',
      },
      keyframes: {
        pulse_dot: {
          '0%, 100%': { opacity: 1 },
          '50%': { opacity: 0.3 },
        },
        slide_in: {
          '0%': { opacity: 0, transform: 'translateY(-6px)' },
          '100%': { opacity: 1, transform: 'translateY(0)' },
        },
        fade_in: {
          '0%': { opacity: 0 },
          '100%': { opacity: 1 },
        },
      },
      animation: {
        pulse_dot: 'pulse_dot 1.4s ease-in-out infinite',
        slide_in: 'slide_in 0.15s ease-out',
        fade_in: 'fade_in 0.2s ease-out',
      },
    },
  },
  plugins: [],
}
