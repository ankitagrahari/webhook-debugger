import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Zap } from 'lucide-react'
import LegalFooter from "../components/LegalFooter";

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-surface-0 flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        {/* Logo */}
        <div className="flex items-center gap-2 mb-10 justify-center">
          <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center">
            <Zap size={16} className="text-white" fill="white" />
          </div>
          <span className="font-mono text-lg font-semibold text-white tracking-tight">HookSpy</span>
        </div>

        <div className="bg-surface-2 border border-border rounded-xl p-8">
          <h1 className="text-xl font-semibold text-white mb-1">Sign in</h1>
          <p className="text-sm text-muted mb-6">Welcome back</p>

          {error && (
            <div className="mb-4 px-3 py-2 rounded-lg bg-danger/10 border border-danger/30 text-danger text-sm animate-fade_in">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm text-muted mb-1.5">Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoFocus
                placeholder="you@example.com"
                className="w-full bg-surface-3 border border-border rounded-lg px-3 py-2.5 text-sm text-white placeholder:text-surface-4 focus:outline-none focus:border-accent/60 focus:ring-1 focus:ring-accent/30 transition-colors font-mono"
              />
            </div>
            <div>
              <label className="block text-sm text-muted mb-1.5">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                placeholder="••••••••"
                className="w-full bg-surface-3 border border-border rounded-lg px-3 py-2.5 text-sm text-white placeholder:text-surface-4 focus:outline-none focus:border-accent/60 focus:ring-1 focus:ring-accent/30 transition-colors font-mono"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-accent hover:bg-accent-hover disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium text-sm rounded-lg py-2.5 transition-colors mt-2"
            >
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-center gap-3 mt-5">
            <p className="text-center text-sm text-muted">
            No account?{' '}
            <Link to="/register" className="text-accent hover:text-accent-hover transition-colors">
              Create one
            </Link>
          </p>
          <span className="text-muted text-xs">·</span>
          <a href="https://www.instagram.com/backendbrilliance"
            target="_blank"
            rel="noreferrer"
            className="text-xs font-mono text-accent hover:text-accent-hover transition-colors">
            @backendbrilliance
          </a>
        </div>
        <div className="flex items-center justify-center gap-3 mt-5"><LegalFooter/></div>
      </div>
    </div>
  )
}
