import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Check, ArrowLeft, Zap } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { cn } from '../lib/utils'
import api from '../lib/api'

const PLANS = [
  {
    id: 'PRO',
    label: 'Pro',
    price: 299,
    priceLabel: '₹299',
    period: '/month',
    features: [
      '10 endpoints',
      'Unlimited requests',
      '30 day retention',
      'Slack alerts on inactivity',
      'Priority support',
    ],
  },
  {
    id: 'TEAM',
    label: 'Team',
    price: 799,
    priceLabel: '₹799',
    period: '/month',
    features: [
      '50 endpoints',
      'Unlimited requests',
      '90 day retention',
      'Slack alerts on inactivity',
      'Team collaboration (coming soon)',
    ],
    highlight: true,
  },
]

export default function UpgradePage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [selected, setSelected] = useState('PRO')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleCheckout = async () => {
    setError('')
    setLoading(true)
    try {
      const plan = PLANS.find((p) => p.id === selected)
      // Create Razorpay order
      const orderRes = await api.post('/payment/order', {
        tier: selected,
        amount: plan.price * 100, // paise
      })
      const { orderId, keyId } = orderRes.data

      const options = {
        key: keyId,
        amount: plan.price * 100,
        currency: 'INR',
        name: 'HookSpy',
        description: `${plan.label} Plan`,
        order_id: orderId,
        handler: async (response) => {
          try {
            await api.post('/payment/confirm', {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
              tier: selected,
            })
            // Reload page to refresh user tier
            window.location.href = '/'
          } catch {
            setError('Payment verification failed. Contact support.')
          }
        },
        prefill: { email: user?.email },
        theme: { color: '#7c6aff' },
      }

      const rzp = new window.Razorpay(options)
      rzp.open()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not initiate payment')
    } finally {
      setLoading(false)
    }
  }

  const currentPlan = PLANS.find((p) => p.id === selected)

  return (
    <>
      {/* Razorpay SDK — loaded lazily */}
      <script src="https://checkout.razorpay.com/v1/checkout.js" />

      <div className="min-h-screen bg-surface-0 px-4 py-10">
        <div className="max-w-2xl mx-auto">
          <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 text-muted hover:text-white text-sm transition-colors mb-8"
          >
            <ArrowLeft size={14} />
            Back
          </button>

          <div className="flex items-center gap-3 mb-2">
            <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center">
              <Zap size={15} className="text-white" fill="white" />
            </div>
            <h1 className="text-2xl font-semibold text-white">Upgrade HookSpy</h1>
          </div>
          <p className="text-muted text-sm mb-8 ml-11">
            You're on <span className={cn('font-mono text-[11px] px-1.5 py-0.5 rounded border', `tier-${user?.tier}`)}>{user?.tier}</span>
            . Unlock more endpoints and longer retention.
          </p>

          {/* Plan cards */}
          <div className="grid grid-cols-2 gap-4 mb-8">
            {PLANS.map((plan) => (
              <button
                key={plan.id}
                onClick={() => setSelected(plan.id)}
                className={cn(
                  'relative text-left p-5 rounded-xl border transition-all',
                  selected === plan.id
                    ? 'border-accent bg-accent/10'
                    : 'border-border bg-surface-2 hover:border-border/80'
                )}
              >
                {plan.highlight && (
                  <span className="absolute -top-3 left-1/2 -translate-x-1/2 text-[11px] font-mono bg-accent text-white px-3 py-0.5 rounded-full">
                    Best value
                  </span>
                )}
                <div className="flex items-start justify-between mb-3">
                  <div>
                    <div className="font-semibold text-white">{plan.label}</div>
                    <div className="text-2xl font-mono font-bold text-white mt-1">
                      {plan.priceLabel}
                      <span className="text-sm font-normal text-muted">{plan.period}</span>
                    </div>
                  </div>
                  {selected === plan.id && (
                    <div className="w-5 h-5 rounded-full bg-accent flex items-center justify-center">
                      <Check size={11} className="text-white" />
                    </div>
                  )}
                </div>
                <ul className="space-y-1.5">
                  {plan.features.map((f) => (
                    <li key={f} className="flex items-center gap-2 text-xs text-muted">
                      <Check size={11} className="text-success shrink-0" />
                      {f}
                    </li>
                  ))}
                </ul>
              </button>
            ))}
          </div>

          {error && (
            <div className="mb-4 px-4 py-3 rounded-lg bg-danger/10 border border-danger/30 text-danger text-sm animate-fade_in">
              {error}
            </div>
          )}

          <button
            onClick={handleCheckout}
            disabled={loading || user?.tier === selected}
            className="w-full bg-accent hover:bg-accent-hover disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium py-3 rounded-xl transition-colors"
          >
            {loading
              ? 'Opening checkout…'
              : user?.tier === selected
              ? `Already on ${selected}`
              : `Upgrade to ${currentPlan?.label} — ${currentPlan?.priceLabel}/mo`}
          </button>

          <p className="text-center text-xs text-muted mt-4">
            Powered by Razorpay · Secure payment · Cancel anytime
          </p>
        </div>
      </div>
    </>
  )
}
