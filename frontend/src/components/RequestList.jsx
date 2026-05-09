import { useState, useEffect, useCallback } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { formatDistanceToNow } from '../lib/timeUtils'
import { cn } from '../lib/utils'
import api from '../lib/api'

const METHOD_CLASSES = {
  GET: 'badge-GET',
  POST: 'badge-POST',
  PUT: 'badge-PUT',
  PATCH: 'badge-PATCH',
  DELETE: 'badge-DELETE',
  HEAD: 'badge-HEAD',
}

function MethodBadge({ method }) {
  return (
    <span className={cn('inline-block text-[10px] font-mono font-medium px-1.5 py-0.5 rounded uppercase', METHOD_CLASSES[method] || 'badge-HEAD')}>
      {method}
    </span>
  )
}

function formatBytes(bytes) {
  if (!bytes) return '—'
  if (bytes < 1024) return `${bytes}B`
  return `${(bytes / 1024).toFixed(1)}KB`
}

export default function RequestList({ slug, onSelect, selectedId }) {
  const qc = useQueryClient()
  const [liveCount, setLiveCount] = useState(0)

  const { data: requests = [], isLoading } = useQuery({
    queryKey: ['requests', slug],
    queryFn: () => api.get(`/endpoints/${slug}/requests`).then((r) => r.data),
    enabled: !!slug,
  })

  // SSE — live updates
  useEffect(() => {
    if (!slug) return
    const source = new EventSource(`/api/endpoints/${slug}/requests/stream`, {
      withCredentials: true,
    })

    source.onmessage = (e) => {
      const newReq = JSON.parse(e.data)
      qc.setQueryData(['requests', slug], (old = []) => [newReq, ...old])
      setLiveCount((n) => n + 1)
      // Reset live flash after 2s
      setTimeout(() => setLiveCount((n) => Math.max(0, n - 1)), 2000)
    }

    source.onerror = () => {
      // SSE reconnects automatically — nothing to do
    }

    return () => source.close()
  }, [slug, qc])

  if (!slug) return null

  if (isLoading) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="w-4 h-4 border-2 border-accent border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  if (requests.length === 0) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center text-center px-6">
        <div className="w-10 h-10 rounded-xl bg-surface-3 border border-border flex items-center justify-center mb-3">
          <span className="text-lg">🪝</span>
        </div>
        <p className="text-sm text-white font-medium mb-1">Waiting for webhooks</p>
        <p className="text-xs text-muted">
          Send a request to your endpoint URL above
        </p>
      </div>
    )
  }

  return (
    <div className="flex-1 overflow-y-auto">
      {/* Header row */}
      <div className="sticky top-0 z-10 bg-surface-1 border-b border-border px-4 py-2 grid grid-cols-[70px_1fr_90px_60px_80px] gap-3 text-[10px] font-mono text-muted uppercase tracking-widest">
        <span>Method</span>
        <span>Source IP</span>
        <span>Type</span>
        <span className="text-right">Size</span>
        <span className="text-right">Time</span>
      </div>

      <ul>
        {requests.map((req, i) => (
          <li
            key={req.id}
            onClick={() => onSelect(req)}
            className={cn(
              'px-4 py-2.5 grid grid-cols-[70px_1fr_90px_60px_80px] gap-3 items-center cursor-pointer border-b border-border/50 transition-colors',
              selectedId === req.id
                ? 'bg-accent/10'
                : 'hover:bg-surface-3',
              i === 0 && liveCount > 0 ? 'animate-slide_in' : ''
            )}
          >
            <MethodBadge method={req.method} />
            <span className="text-xs font-mono text-white truncate">{req.sourceIp || '—'}</span>
            <span className="text-[11px] font-mono text-muted truncate">
              {req.contentType ? req.contentType.split(';')[0].split('/')[1] : '—'}
            </span>
            <span className="text-[11px] font-mono text-muted text-right">{formatBytes(req.bodySize)}</span>
            <span className="text-[11px] font-mono text-muted text-right whitespace-nowrap">
              {formatDistanceToNow(req.receivedAt)}
            </span>
          </li>
        ))}
      </ul>
    </div>
  )
}
