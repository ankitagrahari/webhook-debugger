import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Zap, Check } from 'lucide-react'
import { cn } from '../lib/utils'

const TIERS = [
  {
    id: 'FREE',
    label: 'Free',
    price: '₹0',
    features: ['1 endpoint', '100 requests/day', '1 day retention'],
  },
  {
    id: 'PRO',
    label: 'Pro',
    price: '₹299/mo',
    features: ['10 endpoints', 'Unlimited requests', '30 day retention', 'Slack alerts'],
    highlight: true,
  },
  {
    id: 'TEAM',
    label: 'Team',
    price: '₹799/mo',
    features: ['50 endpoints', 'Unlimited requests', '90 day retention', 'Slack alerts'],
  },
]

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [tier, setTier] = useState('FREE')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(email, password, tier)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-surface-0 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-lg">
        {/* Logo */}
        <div className="flex items-center gap-2 mb-10 justify-center">
          <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center">
            <Zap size={16} className="text-white" fill="white" />
          </div>
          <span className="font-mono text-lg font-semibold text-white tracking-tight">HookSpy</span>
        </div>

        <div className="bg-surface-2 border border-border rounded-xl p-8">
          <h1 className="text-xl font-semibold text-white mb-1">Create account</h1>
          <p className="text-sm text-muted mb-6">Choose a plan to get started</p>

          {error && (
            <div className="mb-4 px-3 py-2 rounded-lg bg-danger/10 border border-danger/30 text-danger text-sm animate-fade_in">
              {error}
            </div>
          )}

          {/* Tier selector */}
          <div className="grid grid-cols-3 gap-3 mb-6">
            {TIERS.map((t) => (
              <button
                key={t.id}
                type="button"
                onClick={() => setTier(t.id)}
                className={cn(
                  'relative text-left p-3 rounded-lg border transition-all',
                  tier === t.id
                    ? 'border-accent bg-accent/10'
                    : 'border-border bg-surface-3 hover:border-border/80'
                )}
              >
                {t.highlight && (
                  <span className="absolute -top-2 left-1/2 -translate-x-1/2 text-[10px] font-mono bg-accent text-white px-2 py-0.5 rounded-full">
                    Popular
                  </span>
                )}
                <div className="font-medium text-sm text-white">{t.label}</div>
                <div className="font-mono text-xs text-accent mt-0.5">{t.price}</div>
                <ul className="mt-2 space-y-1">
                  {t.features.map((f) => (
                    <li key={f} className="flex items-start gap-1 text-[11px] text-muted">
                      <Check size={10} className="mt-0.5 shrink-0 text-success" />
                      {f}
                    </li>
                  ))}
                </ul>
                {tier === t.id && (
                  <div className="absolute top-2 right-2 w-4 h-4 rounded-full bg-accent flex items-center justify-center">
                    <Check size={9} className="text-white" />
                  </div>
                )}
              </button>
            ))}
          </div>

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
                minLength={6}
                placeholder="min 6 characters"
                className="w-full bg-surface-3 border border-border rounded-lg px-3 py-2.5 text-sm text-white placeholder:text-surface-4 focus:outline-none focus:border-accent/60 focus:ring-1 focus:ring-accent/30 transition-colors font-mono"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-accent hover:bg-accent-hover disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium text-sm rounded-lg py-2.5 transition-colors mt-2"
            >
              {loading ? 'Creating account…' : `Create ${tier} account`}
            </button>
          </form>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-center gap-3 mt-5">
            <p className="text-center text-sm text-muted">
            <p className="text-center text-sm text-muted mt-5">
                Already have an account?{' '}
                <Link to="/login" className="text-accent hover:text-accent-hover transition-colors">
                  Sign in
                </Link>
            </p>
            </p>
          <span className="text-muted text-xs">·</span>
          <a href="https://www.instagram.com/backendbrilliance"
            target="_blank"
            rel="noreferrer"
            className="text-xs font-mono text-accent hover:text-accent-hover transition-colors">
            @backendbrilliance
          </a>
        </div>

      </div>
    </div>
  )
}
