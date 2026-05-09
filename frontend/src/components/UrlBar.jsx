import { useState } from 'react'
import { Copy, Check, Radio } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { cn } from '../lib/utils'

export default function UrlBar({ endpoint, isLive }) {
  const { user } = useAuth()
  const [copied, setCopied] = useState(false)

  if (!endpoint) return null

  const captureUrl = endpoint.captureUrl || `${window.location.origin.replace('8082', '8080')}/h/${endpoint.slug}`

  const copy = () => {
    navigator.clipboard.writeText(captureUrl)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const tier = user?.tier?.toUpperCase()

  return (
    <div className="px-5 py-3 border-b border-border bg-surface-1 flex items-center gap-3">
      {/* Live indicator */}
      <div className="flex items-center gap-1.5 shrink-0">
        <span
          className={cn(
            'w-2 h-2 rounded-full',
            isLive ? 'bg-success animate-pulse_dot' : 'bg-surface-4'
          )}
        />
        <span className={cn('text-[11px] font-mono', isLive ? 'text-success' : 'text-muted')}>
          {isLive ? 'live' : 'idle'}
        </span>
      </div>

      <div className="w-px h-4 bg-border" />

      {/* Label */}
      <span className="text-xs text-muted font-mono shrink-0">{endpoint.label}</span>

      {/* URL */}
      <div className="flex-1 min-w-0 bg-surface-3 border border-border rounded-lg px-3 py-1.5 flex items-center gap-2 group cursor-pointer" onClick={copy}>
        <span className="text-xs font-mono text-white truncate flex-1">{captureUrl}</span>
        <button className="shrink-0 text-muted group-hover:text-white transition-colors">
          {copied ? (
            <Check size={13} className="text-success animate-fade_in" />
          ) : (
            <Copy size={13} />
          )}
        </button>
      </div>

      {/* Tier badge */}
      <span className={cn('text-[10px] font-mono px-2 py-1 rounded border shrink-0', `tier-${tier}`)}>
        {tier}
      </span>
    </div>
  )
}
