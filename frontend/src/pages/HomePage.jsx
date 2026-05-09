import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Zap, Plus } from 'lucide-react'
import api from '../lib/api'
import { useAuth } from '../context/AuthContext'

export default function HomePage() {
  const navigate = useNavigate()
  const { user } = useAuth()

  const { data: endpoints, isLoading } = useQuery({
    queryKey: ['endpoints'],
    queryFn: () => api.get('/endpoints').then((r) => r.data),
  })

  useEffect(() => {
    if (endpoints && endpoints.length > 0) {
      navigate(`/dashboard/${endpoints[0].slug}`, { replace: true })
    }
  }, [endpoints, navigate])

  const createEndpoint = async () => {
    const label = prompt('Endpoint label (e.g. stripe-test):')
    if (!label) return
    const res = await api.post('/endpoints', { label })
    navigate(`/dashboard/${res.data.slug}`)
  }

  if (isLoading) {
    return (
      <div className="h-full flex items-center justify-center bg-surface-0">
        <div className="w-5 h-5 border-2 border-accent border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  // No endpoints — empty state
  return (
    <div className="min-h-screen bg-surface-0 flex items-center justify-center">
      <div className="text-center max-w-sm">
        <div className="w-14 h-14 rounded-2xl bg-accent/10 border border-accent/20 flex items-center justify-center mx-auto mb-5">
          <Zap size={24} className="text-accent" />
        </div>
        <h2 className="text-xl font-semibold text-white mb-2">No endpoints yet</h2>
        <p className="text-sm text-muted mb-6">
          Create your first endpoint to start capturing webhooks from Stripe, Razorpay, GitHub, or any service.
        </p>
        <button
          onClick={createEndpoint}
          className="inline-flex items-center gap-2 bg-accent hover:bg-accent-hover text-white text-sm font-medium px-4 py-2.5 rounded-lg transition-colors"
        >
          <Plus size={15} />
          Create endpoint
        </button>
        <p className="text-xs text-muted mt-4">
          Signed in as <span className="text-white font-mono">{user?.email}</span>
          {' · '}
          <span className={`tier-${user?.tier} px-1.5 py-0.5 rounded text-[10px] font-mono border`}>
            {user?.tier}
          </span>
        </p>
      </div>
    </div>
  )
}
