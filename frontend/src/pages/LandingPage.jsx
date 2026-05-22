import { useState, useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import { Zap, Check, ArrowRight, Copy, Terminal, Eye, RotateCcw, ChevronRight } from 'lucide-react'
import { cn } from '../lib/utils'

// ── Animated terminal that types out a curl command ───────────────────────────
function TerminalDemo() {
  const lines = [
    { text: '$ curl -X POST https://hookspy.in/h/rp9x4m2k \\', color: 'text-white', delay: 0 },
    { text: '    -H "Content-Type: application/json" \\', color: 'text-muted', delay: 600 },
    { text: '    -d \'{"event":"payment.captured","amount":50000}\'', color: 'text-muted', delay: 1100 },
    { text: '', color: '', delay: 1600 },
    { text: '← 200 OK  (47ms)', color: 'text-success', delay: 1900 },
    { text: '', color: '', delay: 2200 },
    { text: '✓ Request captured — check your dashboard', color: 'text-accent', delay: 2400 },
  ]

  const [visibleLines, setVisibleLines] = useState(0)

  useEffect(() => {
    lines.forEach((line, i) => {
      setTimeout(() => setVisibleLines(i + 1), line.delay + 400)
    })
  }, [])

  return (
    <div className="bg-surface-1 border border-border rounded-xl overflow-hidden font-mono text-sm shadow-2xl">
      {/* Terminal chrome */}
      <div className="flex items-center gap-2 px-4 py-3 border-b border-border bg-surface-2">
        <span className="w-3 h-3 rounded-full bg-danger/70" />
        <span className="w-3 h-3 rounded-full bg-warning/70" />
        <span className="w-3 h-3 rounded-full bg-success/70" />
        <span className="ml-3 text-xs text-muted">terminal</span>
      </div>
      <div className="p-5 space-y-1 min-h-[160px]">
        {lines.slice(0, visibleLines).map((line, i) => (
          <div key={i} className={cn('animate-fade_in', line.color)}>
            {line.text}
            {i === visibleLines - 1 && (
              <span className="inline-block w-2 h-4 bg-accent ml-0.5 animate-pulse" />
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

// ── Dashboard preview mockup ──────────────────────────────────────────────────
function DashboardPreview() {
  const requests = [
    { method: 'POST', ip: '103.21.244.0', type: 'json', size: '1.2KB', time: 'just now', highlight: true },
    { method: 'POST', ip: '103.21.244.0', type: 'json', size: '1.1KB', time: '2m ago', highlight: false },
    { method: 'GET',  ip: '192.30.252.0', type: '—',    size: '—',     time: '5m ago', highlight: false },
    { method: 'POST', ip: '103.21.244.0', type: 'json', size: '980B',  time: '12m ago', highlight: false },
  ]

  const METHOD_COLORS = {
    POST: 'bg-violet-500/15 text-violet-400 border-violet-500/30',
    GET:  'bg-emerald-500/15 text-emerald-400 border-emerald-500/30',
  }

  return (
    <div className="bg-surface-1 border border-border rounded-xl overflow-hidden shadow-2xl text-xs font-mono">
      {/* URL bar */}
      <div className="flex items-center gap-3 px-4 py-3 border-b border-border bg-surface-2">
        <span className="w-2 h-2 rounded-full bg-success animate-pulse_dot" />
        <span className="text-success text-[11px]">live</span>
        <div className="flex-1 bg-surface-3 border border-border rounded-md px-3 py-1.5 text-white text-[11px]">
          https://hookspy.in/h/rp9x4m2kqz8w
        </div>
        <span className="bg-violet-500/15 text-violet-400 border border-violet-500/30 px-2 py-0.5 rounded text-[10px]">
          PRO
        </span>
      </div>

      {/* Request list */}
      <div className="divide-y divide-border/50">
        {requests.map((req, i) => (
          <div
            key={i}
            className={cn(
              'flex items-center gap-4 px-4 py-2.5 transition-colors',
              req.highlight ? 'bg-accent/8 border-l-2 border-l-accent' : 'hover:bg-surface-2'
            )}
          >
            <span className={cn('text-[10px] font-medium px-1.5 py-0.5 rounded border uppercase', METHOD_COLORS[req.method])}>
              {req.method}
            </span>
            <span className="text-muted flex-1">{req.ip}</span>
            <span className="text-muted w-8">{req.type}</span>
            <span className="text-muted w-10 text-right">{req.size}</span>
            <span className={cn('w-16 text-right', req.highlight ? 'text-accent' : 'text-muted')}>
              {req.time}
            </span>
          </div>
        ))}
      </div>

      {/* Detail pane preview */}
      <div className="border-t border-border bg-surface-2 px-4 py-3">
        <div className="flex gap-3 mb-2">
          {['Body', 'Headers', 'Query'].map((t, i) => (
            <span key={t} className={cn('text-[11px] pb-1 border-b', i === 0 ? 'border-accent text-white' : 'border-transparent text-muted')}>
              {t}{i === 1 && <span className="ml-1 text-[10px] bg-surface-3 px-1 rounded">15</span>}
            </span>
          ))}
        </div>
        <div className="bg-surface-3 rounded-lg p-3 text-[11px] text-success/80 leading-relaxed">
          {`{`}<br />
          &nbsp;&nbsp;<span className="text-accent">"event"</span>: <span className="text-warning">"payment.captured"</span>,<br />
          &nbsp;&nbsp;<span className="text-accent">"amount"</span>: <span className="text-info">50000</span>,<br />
          &nbsp;&nbsp;<span className="text-accent">"currency"</span>: <span className="text-warning">"INR"</span><br />
          {`}`}
        </div>
      </div>
    </div>
  )
}

// ── Feature card ──────────────────────────────────────────────────────────────
function FeatureCard({ icon: Icon, title, description, accent }) {
  return (
    <div className="group relative bg-surface-2 border border-border rounded-xl p-6 hover:border-accent/40 transition-all duration-300 overflow-hidden">
      <div className="absolute inset-0 bg-gradient-to-br from-accent/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
      <div className={cn('w-10 h-10 rounded-xl flex items-center justify-center mb-4 border', accent)}>
        <Icon size={18} className="text-accent" />
      </div>
      <h3 className="text-white font-semibold text-sm mb-2">{title}</h3>
      <p className="text-muted text-sm leading-relaxed">{description}</p>
    </div>
  )
}

// ── Pricing card ──────────────────────────────────────────────────────────────
function PricingCard({ tier, price, period, features, highlight, cta }) {
  return (
    <div className={cn(
      'relative rounded-2xl border p-6 flex flex-col transition-all duration-300',
      highlight
        ? 'border-accent bg-accent/8 shadow-lg shadow-accent/10'
        : 'border-border bg-surface-2 hover:border-accent/30'
    )}>
      {highlight && (
        <div className="absolute -top-3.5 left-1/2 -translate-x-1/2 bg-accent text-white text-[11px] font-mono px-3 py-1 rounded-full">
          Most Popular
        </div>
      )}
      <div className="mb-4">
        <div className="text-sm font-mono text-muted mb-1">{tier}</div>
        <div className="text-3xl font-bold text-white font-mono">
          {price}
          {period && <span className="text-sm font-normal text-muted">{period}</span>}
        </div>
      </div>
      <ul className="space-y-2.5 flex-1 mb-6">
        {features.map((f) => (
          <li key={f} className="flex items-center gap-2.5 text-sm text-muted">
            <Check size={13} className="text-success shrink-0" />
            {f}
          </li>
        ))}
      </ul>
      <Link
        to="/register"
        className={cn(
          'text-center text-sm font-medium py-2.5 rounded-xl transition-colors',
          highlight
            ? 'bg-accent hover:bg-accent-hover text-white'
            : 'bg-surface-3 hover:bg-surface-4 text-white border border-border'
        )}
      >
        {cta}
      </Link>
    </div>
  )
}

// ── Step card for "how it works" ──────────────────────────────────────────────
function Step({ number, title, description, code }) {
  return (
    <div className="flex gap-5">
      <div className="shrink-0 w-9 h-9 rounded-xl bg-accent/15 border border-accent/30 flex items-center justify-center font-mono text-sm text-accent font-bold">
        {number}
      </div>
      <div className="flex-1 pt-1">
        <h3 className="text-white font-semibold text-sm mb-1">{title}</h3>
        <p className="text-muted text-sm leading-relaxed mb-2">{description}</p>
        {code && (
          <div className="bg-surface-2 border border-border rounded-lg px-3 py-2 font-mono text-xs text-accent">
            {code}
          </div>
        )}
      </div>
    </div>
  )
}

// ── Main landing page ─────────────────────────────────────────────────────────
export default function LandingPage() {
  return (
    <div className="min-h-screen bg-surface-0 text-white">

      {/* Nav */}
      <nav className="fixed top-0 left-0 right-0 z-50 border-b border-border/50 bg-surface-0/80 backdrop-blur-xl">
        <div className="max-w-6xl mx-auto px-6 h-14 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-accent flex items-center justify-center">
              <Zap size={13} className="text-white" fill="white" />
            </div>
            <span className="font-mono text-sm font-semibold tracking-tight">HookSpy</span>
          </div>
          <div className="flex items-center gap-6">
            <a href="#features" className="text-sm text-muted hover:text-white transition-colors hidden sm:block">
              Features
            </a>
            <a href="#pricing" className="text-sm text-muted hover:text-white transition-colors hidden sm:block">
              Pricing
            </a>
            <Link to="/login" className="text-sm text-muted hover:text-white transition-colors">
              Sign in
            </Link>
            <Link
              to="/register"
              className="bg-accent hover:bg-accent-hover text-white text-sm font-medium px-4 py-1.5 rounded-lg transition-colors"
            >
              Start free
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section className="pt-32 pb-20 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="max-w-2xl mb-16">
            {/* Badge */}
            <div className="inline-flex items-center gap-2 bg-accent/10 border border-accent/20 rounded-full px-3 py-1 text-xs font-mono text-accent mb-6">
              <span className="w-1.5 h-1.5 rounded-full bg-accent animate-pulse" />
              Free tier — no credit card required
            </div>

            <h1 className="text-5xl font-bold leading-tight mb-5 tracking-tight">
              Stop guessing what your{' '}
              <span className="text-accent">webhook sent</span>
            </h1>
            <p className="text-lg text-muted leading-relaxed mb-8">
              Get a unique URL, point any service at it, and see every request arrive in real time.
              Inspect headers, body, query params. Replay to your local server. Export as cURL.
              Debug in 5 minutes instead of 2 hours.
            </p>
            <div className="flex items-center gap-3 flex-wrap">
              <Link
                to="/register"
                className="inline-flex items-center gap-2 bg-accent hover:bg-accent-hover text-white font-medium px-6 py-3 rounded-xl transition-colors"
              >
                Get your webhook URL
                <ArrowRight size={16} />
              </Link>
              <Link
                to="/login"
                className="inline-flex items-center gap-2 text-muted hover:text-white text-sm transition-colors"
              >
                Already have an account
                <ChevronRight size={14} />
              </Link>
            </div>
          </div>

          {/* Hero visuals — terminal + dashboard side by side */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div>
              <p className="text-xs font-mono text-muted mb-3">1. Send a webhook</p>
              <TerminalDemo />
            </div>
            <div>
              <p className="text-xs font-mono text-muted mb-3">2. See it instantly in your dashboard</p>
              <DashboardPreview />
            </div>
          </div>
        </div>
      </section>

      {/* Logos / social proof */}
      <section className="py-10 px-6 border-y border-border/50">
        <div className="max-w-6xl mx-auto">
          <p className="text-center text-xs font-mono text-muted mb-6 uppercase tracking-widest">
            Works with any service that sends webhooks
          </p>
          <div className="flex items-center justify-center gap-8 flex-wrap">
            {['Stripe', 'Razorpay', 'GitHub', 'Shopify', 'Twilio', 'Any HTTP service'].map((s) => (
              <span key={s} className="text-sm font-mono text-muted/60 hover:text-muted transition-colors">
                {s}
              </span>
            ))}
          </div>
        </div>
      </section>

      {/* How it works */}
      <section className="py-20 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="mb-12">
            <p className="text-xs font-mono text-accent uppercase tracking-widest mb-3">How it works</p>
            <h2 className="text-3xl font-bold">From signup to first webhook in 3 steps</h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <Step
              number="1"
              title="Create an endpoint"
              description="Register and create a named endpoint in one click. You get a unique capture URL instantly."
              code="https://hookspy.in/h/abc123xyz"
            />
            <Step
              number="2"
              title="Point your service at it"
              description="Paste the URL into Stripe, Razorpay, GitHub, or any service that sends webhooks. No config needed."
            />
            <Step
              number="3"
              title="Inspect, replay, export"
              description="See every request in real time. Inspect headers and body. Replay to localhost. Copy as cURL."
            />
          </div>
        </div>
      </section>

      {/* Real scenario */}
      <section className="py-20 px-6 bg-surface-1 border-y border-border">
        <div className="max-w-3xl mx-auto">
          <p className="text-xs font-mono text-accent uppercase tracking-widest mb-3">Real scenario</p>
          <h2 className="text-3xl font-bold mb-4">
            Razorpay payment captured.<br />Order never marked as paid.
          </h2>
          <p className="text-muted leading-relaxed mb-8">
            Classic integration bug. The payment goes through on Razorpay's side but your handler does nothing.
            You don't know if the webhook is being sent, what payload it contains, or which header your
            HMAC verification is looking for. HookSpy solves this in under 5 minutes.
          </p>
          <div className="space-y-4">
            {[
              { step: 'Point Razorpay at your HookSpy URL', detail: 'Takes 30 seconds in Razorpay dashboard' },
              { step: 'Trigger a test payment', detail: 'Request appears in dashboard within 1 second' },
              { step: 'Open Headers tab — spot the missing X-Razorpay-Signature', detail: 'Your handler was never reading it' },
              { step: 'Check Body tab — find the correct JSON path', detail: 'payload.payment.entity.id, not payload.payment.id' },
              { step: 'Fix handler locally, replay the exact same request', detail: 'No need to trigger another payment' },
              { step: 'Export as cURL — add to your test suite', detail: 'Reproducible forever' },
            ].map((item, i) => (
              <div key={i} className="flex gap-4 items-start">
                <span className="shrink-0 w-6 h-6 rounded-full bg-success/15 border border-success/30 flex items-center justify-center text-[10px] font-mono text-success font-bold mt-0.5">
                  {i + 1}
                </span>
                <div>
                  <span className="text-white text-sm font-medium">{item.step}</span>
                  <span className="text-muted text-sm"> — {item.detail}</span>
                </div>
              </div>
            ))}
          </div>
          <div className="mt-8 p-4 bg-surface-2 border border-success/20 rounded-xl">
            <p className="text-success text-sm font-mono">
              Total debug time: under 5 minutes
            </p>
            <p className="text-muted text-sm mt-1">
              Without HookSpy: hours of adding logs, redeploying, triggering test payments repeatedly
            </p>
          </div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className="py-20 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="mb-12">
            <p className="text-xs font-mono text-accent uppercase tracking-widest mb-3">Features</p>
            <h2 className="text-3xl font-bold">Everything you need to debug webhooks</h2>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <FeatureCard
              icon={Eye}
              title="Real-time capture"
              description="Requests appear in your dashboard within 1 second via Server-Sent Events. No polling, no refresh."
              accent="bg-accent/10 border-accent/20"
            />
            <FeatureCard
              icon={Terminal}
              title="Full request inspection"
              description="Body (JSON pretty-printed), Headers, and Query params in separate tabs. Exact payload, exact headers."
              accent="bg-success/10 border-success/20"
            />
            <FeatureCard
              icon={RotateCcw}
              title="Replay to any target"
              description="Fire the exact same request — same headers, same body — to any URL. Test your local handler without re-triggering the service."
              accent="bg-info/10 border-info/20"
            />
            <FeatureCard
              icon={Copy}
              title="cURL export"
              description="Every request is instantly exportable as a curl command with all headers included. Paste into your terminal or test suite."
              accent="bg-warning/10 border-warning/20"
            />
            <FeatureCard
              icon={Zap}
              title="Accepts all HTTP methods"
              description="GET, POST, PUT, PATCH, DELETE — whatever your service sends. Different services use different methods."
              accent="bg-accent/10 border-accent/20"
            />
            <FeatureCard
              icon={Check}
              title="Works with everything"
              description="Stripe, Razorpay, GitHub, Shopify, Twilio, or any internal service. One URL, any source."
              accent="bg-success/10 border-success/20"
            />
          </div>
        </div>
      </section>

      {/* Pricing */}
      <section id="pricing" className="py-20 px-6 bg-surface-1 border-y border-border">
        <div className="max-w-6xl mx-auto">
          <div className="mb-12 text-center">
            <p className="text-xs font-mono text-accent uppercase tracking-widest mb-3">Pricing</p>
            <h2 className="text-3xl font-bold mb-3">Simple, flat pricing</h2>
            <p className="text-muted">No charges per endpoint. No charges per request. Fixed monthly cost.</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl mx-auto">
            <PricingCard
              tier="FREE"
              price="₹0"
              features={[
                '1 endpoint',
                '100 requests/day',
                '1 day retention',
                'Full inspection + replay',
              ]}
              cta="Start free"
            />
            <PricingCard
              tier="PRO"
              price="₹299"
              period="/month"
              highlight
              features={[
                '10 endpoints',
                'Unlimited requests',
                '30 day retention',
                'Slack alerts',
                'Priority support',
              ]}
              cta="Start Pro"
            />
            <PricingCard
              tier="TEAM"
              price="₹799"
              period="/month"
              features={[
                '50 endpoints',
                'Unlimited requests',
                '90 day retention',
                'Slack alerts',
                'Team features soon',
              ]}
              cta="Start Team"
            />
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-24 px-6">
        <div className="max-w-2xl mx-auto text-center">
          <div className="w-14 h-14 rounded-2xl bg-accent/10 border border-accent/20 flex items-center justify-center mx-auto mb-6">
            <Zap size={24} className="text-accent" />
          </div>
          <h2 className="text-4xl font-bold mb-4">Start debugging faster</h2>
          <p className="text-muted mb-8">
            Free tier, no credit card, no time limit.
            Get your capture URL in under 60 seconds.
          </p>
          <Link
            to="/register"
            className="inline-flex items-center gap-2 bg-accent hover:bg-accent-hover text-white font-medium px-8 py-3.5 rounded-xl transition-colors text-base"
          >
            Create free account
            <ArrowRight size={16} />
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border px-6 py-8">
        <div className="max-w-6xl mx-auto flex items-center justify-between flex-wrap gap-4">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-md bg-accent flex items-center justify-center">
              <Zap size={11} className="text-white" fill="white" />
            </div>
            <span className="font-mono text-sm text-white">HookSpy</span>
            <span className="text-muted text-sm">— Webhook Inspector</span>
          </div>
          <div className="flex items-center gap-6 text-sm text-muted">
            <a
              href="https://www.instagram.com/backendbrilliance"
              target="_blank"
              rel="noreferrer"
              className="hover:text-accent transition-colors font-mono"
            >
              @backendbrilliance
            </a>
            <a
              href="https://dynamicallyblunttech.com"
              target="_blank"
              rel="noreferrer"
              className="hover:text-white transition-colors"
            >
              Blog
            </a>
            <a href="mailto:support.hookspy@gmail.com" className="hover:text-white transition-colors">
                Support
            </a>
            <Link to="/login" className="hover:text-white transition-colors">
              Sign in
            </Link>
            <Link to="/contact" className="hover:text-white transition-colors">
              Contact
            </Link>
          </div>
        </div>
      </footer>
    </div>
  )
}
