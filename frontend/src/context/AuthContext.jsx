import { createContext, useContext, useState, useEffect } from 'react'
import api from '../lib/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(undefined) // undefined = loading, null = not authed

  useEffect(() => {
    api.get('/auth/me')
      .then((res) => setUser(res.data))
      .catch(() => setUser(null))
  }, [])

  const login = async (email, password) => {
    const res = await api.post('/auth/login', { email, password })
    setUser(res.data)
    return res.data
  }

  const logout = async () => {
    await api.post('/auth/logout')
    setUser(null)
    window.location.href = '/login'
  }

  const register = async (email, password, tier) => {
    const res = await api.post('/auth/register', { email, password, tier })
    setUser(res.data)
    return res.data
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
