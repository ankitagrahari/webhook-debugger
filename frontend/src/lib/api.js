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
    if (
      err.response?.status === 401 &&
      !window.location.pathname.startsWith('/login') &&
      !window.location.pathname.startsWith('/register')
    ) {
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api
