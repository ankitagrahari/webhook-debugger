import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  withCredentials: true, // send session cookie on every request
  headers: { 'Content-Type': 'application/json' },
})

// On 401 redirect to login — except when already on /login or /register
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status
    const url = err.config?.url || ''
    const path = window.location.pathname

    // Never redirect on /auth/me — that's how we check if logged in
    // Never redirect if already on login or register
    const isAuthCheck = url.includes('/auth/me')
    const isAuthPage = path.startsWith('/login') || path.startsWith('/register')

    if (status === 401 && !isAuthCheck && !isAuthPage) {
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api
