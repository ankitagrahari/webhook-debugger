import { useState } from 'react'
import { Copy, Check } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../context/AuthContext'
import { cn } from '../lib/utils'
import api from '../lib/api'

export default function UrlBar({ endpoint, isLive }) {
  const { user } = useAuth()
  const [copied, setCopied] = useState(false)
  const tier = user?.tier?.toUpperCase()

  // Same queryKey as RequestList — reads from cache, no extra fetch
  const { data: requests = [] } = useQuery({
    queryKey: ['requests', endpoint?.slug],
    queryFn: () => api.get(`/endpoints/${endpoint.slug}/requests`).then((r) => r.data),
    enabled: !!endpoint?.slug,
  })

  if (!endpoint) return null

  const captureUrl = endpoint.captureUrl ||
    `${window.location.protocol}//${window.location.hostname}:8080/h/${endpoint.slug}`

  const copy = () => {
    navigator.clipboard.writeText(captureUrl)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="px-5 py-3 border-b border-border bg-surface-1 flex items-center gap-3">
      <div className="flex items-center gap-1.5 shrink-0">
        <span className={cn('w-2 h-2 rounded-full',
          isLive ? 'bg-success animate-pulse_dot' : 'bg-surface-4')} />
        <span className={cn('text-[11px] font-mono',
          isLive ? 'text-success' : 'text-muted')}>
          {isLive ? 'live' : 'idle'}
        </span>
      </div>

      <div className="w-px h-4 bg-border" />
      <span className="text-xs text-muted font-mono shrink-0">{endpoint.label}</span>

      <div onClick={copy}
        className="flex-1 min-w-0 bg-surface-3 border border-border rounded-lg px-3 py-1.5 flex items-center gap-2 group cursor-pointer hover:border-accent/40 transition-colors">
        <span className="text-xs font-mono text-white truncate flex-1">{captureUrl}</span>
        <button className="shrink-0 text-muted group-hover:text-white transition-colors">
          {copied ? <Check size={13} className="text-success" /> : <Copy size={13} />}
        </button>
      </div>

      {requests.length > 0 && (
        <div className="shrink-0 flex items-center gap-1.5 bg-surface-3 border border-border rounded-lg px-2.5 py-1">
          <span className="text-[11px] font-mono text-white">{requests.length}</span>
          <span className="text-[10px] text-muted font-mono">reqs</span>
        </div>
      )}

      <span className={cn('text-[10px] font-mono px-2 py-1 rounded border shrink-0', `tier-${tier}`)}>
        {tier}
      </span>
    </div>
  )
}