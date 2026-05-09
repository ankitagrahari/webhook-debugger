import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Copy, Check, RotateCcw, X, ChevronDown, ChevronUp } from 'lucide-react'
import { cn } from '../lib/utils'
import { formatTimestamp } from '../lib/timeUtils'
import api from '../lib/api'

const TABS = ['Body', 'Headers', 'Query']

function CopyButton({ text, size = 13 }) {
  const [copied, setCopied] = useState(false)
  const copy = () => {
    navigator.clipboard.writeText(text)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }
  return (
    <button onClick={copy} className="text-muted hover:text-white transition-colors">
      {copied ? <Check size={size} className="text-success" /> : <Copy size={size} />}
    </button>
  )
}

function JsonDisplay({ text }) {
  try {
    const parsed = JSON.parse(text)
    const pretty = JSON.stringify(parsed, null, 2)
    return (
      <pre className="text-xs font-mono text-white whitespace-pre-wrap break-all leading-relaxed">
        {pretty}
      </pre>
    )
  } catch {
    return (
      <pre className="text-xs font-mono text-white whitespace-pre-wrap break-all leading-relaxed">
        {text}
      </pre>
    )
  }
}

function buildCurl(request) {
  const url = `http://localhost:8080/h/{slug}` // placeholder — real URL from endpoint
  const method = `-X ${request.method}`
  const headers = Object.entries(request.headers || {})
    .filter(([k]) => !['host', 'content-length', 'connection'].includes(k.toLowerCase()))
    .map(([k, v]) => `-H "${k}: ${v}"`)
    .join(' \\\n  ')
  const body = request.body ? `-d '${request.body.replace(/'/g, "\\'")}'` : ''
  return `curl ${method} \\\n  ${headers}${body ? ` \\\n  ${body}` : ''} \\\n  "${url}"`
}

export default function RequestDetail({ request, onClose }) {
  const [tab, setTab] = useState('Body')
  const [replayTarget, setReplayTarget] = useState('')
  const [showReplay, setShowReplay] = useState(false)
  const [replayResult, setReplayResult] = useState(null)

  const replayMutation = useMutation({
    mutationFn: ({ id, targetUrl }) =>
      api.post(`/requests/${id}/replay`, { targetUrl }).then((r) => r.data),
    onSuccess: (data) => setReplayResult(data),
  })

  if (!request) {
    return (
      <div className="flex-1 flex items-center justify-center text-center px-6">
        <div>
          <p className="text-sm text-muted">Select a request to inspect</p>
        </div>
      </div>
    )
  }

  const handleReplay = () => {
    if (!replayTarget.trim()) return
    replayMutation.mutate({ id: request.id, targetUrl: replayTarget.trim() })
  }

  const curlCommand = buildCurl(request)

  return (
    <div className="flex-1 flex flex-col min-h-0 animate-fade_in">
      {/* Request meta header */}
      <div className="px-5 py-3 border-b border-border flex items-center justify-between">
        <div className="flex items-center gap-3">
          <span className={cn('text-[10px] font-mono font-medium px-1.5 py-0.5 rounded uppercase',
            `badge-${request.method}`)}>
            {request.method}
          </span>
          <span className="text-xs font-mono text-muted">{request.sourceIp}</span>
          <span className="text-xs font-mono text-muted">{formatTimestamp(request.receivedAt)}</span>
        </div>
        <div className="flex items-center gap-2">
          <CopyButton text={curlCommand} size={13} />
          <button onClick={onClose} className="text-muted hover:text-white transition-colors">
            <X size={14} />
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-border px-5 flex gap-1">
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={cn(
              'px-3 py-2.5 text-xs font-mono border-b-2 transition-colors',
              tab === t
                ? 'border-accent text-white'
                : 'border-transparent text-muted hover:text-white'
            )}
          >
            {t}
            {t === 'Headers' && (
              <span className="ml-1.5 text-[10px] bg-surface-3 px-1 rounded">
                {Object.keys(request.headers || {}).length}
              </span>
            )}
            {t === 'Query' && Object.keys(request.queryParams || {}).length > 0 && (
              <span className="ml-1.5 text-[10px] bg-surface-3 px-1 rounded">
                {Object.keys(request.queryParams).length}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Tab content */}
      <div className="flex-1 overflow-y-auto p-5">
        {tab === 'Body' && (
          <div>
            {request.body ? (
              <div className="relative">
                <div className="absolute top-2 right-2">
                  <CopyButton text={request.body} />
                </div>
                <div className="bg-surface-3 border border-border rounded-lg p-4 pr-10">
                  <JsonDisplay text={request.body} />
                </div>
              </div>
            ) : (
              <p className="text-sm text-muted">No body</p>
            )}
          </div>
        )}

        {tab === 'Headers' && (
          <div className="space-y-1">
            {Object.entries(request.headers || {}).map(([key, value]) => (
              <div key={key} className="flex gap-3 py-1.5 border-b border-border/50 last:border-0">
                <span className="text-xs font-mono text-accent w-48 shrink-0 truncate">{key}</span>
                <span className="text-xs font-mono text-white break-all">{String(value)}</span>
              </div>
            ))}
            {Object.keys(request.headers || {}).length === 0 && (
              <p className="text-sm text-muted">No headers</p>
            )}
          </div>
        )}

        {tab === 'Query' && (
          <div className="space-y-1">
            {Object.entries(request.queryParams || {}).map(([key, value]) => (
              <div key={key} className="flex gap-3 py-1.5 border-b border-border/50 last:border-0">
                <span className="text-xs font-mono text-accent w-48 shrink-0 truncate">{key}</span>
                <span className="text-xs font-mono text-white break-all">{String(value)}</span>
              </div>
            ))}
            {Object.keys(request.queryParams || {}).length === 0 && (
              <p className="text-sm text-muted">No query params</p>
            )}
          </div>
        )}
      </div>

      {/* Replay panel */}
      <div className="border-t border-border">
        <button
          onClick={() => { setShowReplay(!showReplay); setReplayResult(null) }}
          className="w-full flex items-center justify-between px-5 py-3 text-xs font-mono text-muted hover:text-white transition-colors"
        >
          <span className="flex items-center gap-2">
            <RotateCcw size={12} />
            Replay request
          </span>
          {showReplay ? <ChevronDown size={12} /> : <ChevronUp size={12} />}
        </button>

        {showReplay && (
          <div className="px-5 pb-4 space-y-3 animate-slide_in">
            <div className="flex gap-2">
              <input
                value={replayTarget}
                onChange={(e) => setReplayTarget(e.target.value)}
                placeholder="https://your-server.com/webhook"
                className="flex-1 bg-surface-3 border border-border rounded-lg px-3 py-2 text-xs font-mono text-white placeholder:text-muted focus:outline-none focus:border-accent/60 transition-colors"
              />
              <button
                onClick={handleReplay}
                disabled={replayMutation.isPending || !replayTarget.trim()}
                className="px-4 py-2 bg-accent hover:bg-accent-hover disabled:opacity-50 text-white text-xs rounded-lg transition-colors"
              >
                {replayMutation.isPending ? '…' : 'Send'}
              </button>
            </div>

            {replayResult && (
              <div className="bg-surface-3 border border-border rounded-lg p-3 space-y-2 animate-fade_in">
                <div className="flex items-center gap-3">
                  <span className={cn(
                    'text-xs font-mono font-medium px-2 py-0.5 rounded',
                    replayResult.statusCode < 300 ? 'bg-success/15 text-success' :
                    replayResult.statusCode < 400 ? 'bg-warning/15 text-warning' :
                    'bg-danger/15 text-danger'
                  )}>
                    {replayResult.statusCode}
                  </span>
                  <span className="text-xs text-muted font-mono">{replayResult.durationMs}ms</span>
                </div>
                {replayResult.body && (
                  <div className="bg-surface-0 rounded p-3">
                    <JsonDisplay text={replayResult.body} />
                  </div>
                )}
              </div>
            )}

            {replayMutation.isError && (
              <p className="text-xs text-danger font-mono">
                {replayMutation.error?.response?.data?.message || 'Replay failed'}
              </p>
            )}

            {/* cURL export */}
            <div className="border border-border rounded-lg overflow-hidden">
              <div className="flex items-center justify-between px-3 py-2 border-b border-border bg-surface-3">
                <span className="text-[10px] font-mono text-muted uppercase tracking-wider">cURL</span>
                <CopyButton text={curlCommand} size={12} />
              </div>
              <pre className="px-3 py-3 text-[11px] font-mono text-muted overflow-x-auto">
                {curlCommand}
              </pre>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
