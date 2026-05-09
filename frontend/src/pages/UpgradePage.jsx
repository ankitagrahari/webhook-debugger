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

  const tier = user?.tier?.toUpperCase()

  const handleCheckout = async () => {
    if (!window.Razorpay) {
      setError('Payment SDK not loaded. Please disable ad blockers and refresh.')
      return
    }

    setError('')
    setLoading(true)

    try {
      const plan = PLANS.find((p) => p.id === selected)

      const orderRes = await api.post('/payment/order', {
        tier: selected,
        amount: plan.price * 100,
      })

      // Defensive extraction — handles both { orderId } and raw { id } shapes
      const orderId = orderRes.data.orderId || orderRes.data.id
      const keyId = orderRes.data.keyId

      if (!orderId || !keyId) {
        setError('Invalid response from payment server. Please try again.')
        setLoading(false)
        return
      }

      const options = {
        key: keyId,
        amount: plan.price * 100,
        currency: 'INR',
        name: 'HookSpy',
        description: `${plan.label} Plan — Webhook Inspector`,
        order_id: orderId,
        handler: async (response) => {
          try {
            await api.post('/payment/confirm', {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
              tier: selected,
            })
            // Hard redirect to refresh user session with new tier
            window.location.href = '/'
          } catch (confirmErr) {
            setError(
              confirmErr.response?.data?.message ||
              'Payment verified but upgrade failed. Contact support.'
            )
            setLoading(false)
          }
        },
        prefill: {
          email: user?.email,
        },
        theme: {
          color: '#7c6aff',
        },
        modal: {
          // Reset loading state if user closes the modal without paying
          ondismiss: () => setLoading(false),
        },
      }

      const rzp = new window.Razorpay(options)

      // Show payment failure errors on the UI instead of silently failing
      rzp.on('payment.failed', (response) => {
        setError(
          `Payment failed: ${response.error.description} (${response.error.code})`
        )
        setLoading(false)
      })

      rzp.open()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not initiate payment')
      setLoading(false)
    }
  }

  const currentPlan = PLANS.find((p) => p.id === selected)

  return (
    <div className="min-h-screen bg-surface-0 px-4 py-10">
      <div className="max-w-2xl mx-auto">

        {/* Back button */}
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-muted hover:text-white text-sm transition-colors mb-8"
        >
          <ArrowLeft size={14} />
          Back
        </button>

        {/* Header */}
        <div className="flex items-center gap-3 mb-2">
          <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center">
            <Zap size={15} className="text-white" fill="white" />
          </div>
          <h1 className="text-2xl font-semibold text-white">Upgrade HookSpy</h1>
        </div>
        <p className="text-muted text-sm mb-8 ml-11">
          You&apos;re on{' '}
          <span className={cn(
            'font-mono text-[11px] px-1.5 py-0.5 rounded border',
            tier === 'FREE' && 'bg-surface-3 text-muted border-muted/40',
            tier === 'PRO'  && 'bg-violet-500/15 text-violet-400 border-violet-500/40',
            tier === 'TEAM' && 'bg-amber-500/15 text-amber-400 border-amber-500/40',
          )}>
            {tier}
          </span>
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
                <span className="absolute -top-3 left-1/2 -translate-x-1/2 text-[11px] font-mono bg-accent text-white px-3 py-0.5 rounded-full whitespace-nowrap">
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
                  <div className="w-5 h-5 rounded-full bg-accent flex items-center justify-center shrink-0">
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

        {/* Error message */}
        {error && (
          <div className="mb-4 px-4 py-3 rounded-lg bg-danger/10 border border-danger/30 text-danger text-sm animate-fade_in">
            {error}
          </div>
        )}

        {/* CTA button */}
        <button
          onClick={handleCheckout}
          disabled={loading || tier === selected}
          className="w-full bg-accent hover:bg-accent-hover disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium py-3 rounded-xl transition-colors"
        >
          {loading
            ? 'Opening checkout…'
            : tier === selected
            ? `Already on ${selected}`
            : `Upgrade to ${currentPlan?.label} — ${currentPlan?.priceLabel}/mo`}
        </button>

        {/* Footer */}
        <div className="flex items-center justify-center gap-3 mt-5">
          <p className="text-xs text-muted">Powered by Razorpay · Secure payment</p>
          <span className="text-muted text-xs">·</span>
          <a
            href="https://www.instagram.com/backendbrilliance"
            target="_blank"
            rel="noreferrer"
            className="text-xs font-mono text-accent hover:text-accent-hover transition-colors"
          >
            @backendbrilliance
          </a>
        </div>

      </div>
    </div>
  )
}
