import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Trash2, Zap, LogOut, ChevronRight, CreditCard, AlertCircle, HelpCircle } from 'lucide-react'
import { cn } from '../lib/utils'
import { useAuth } from '../context/AuthContext'
import api from '../lib/api'

export default function Sidebar() {
  const { slug } = useParams()
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const [newLabel, setNewLabel] = useState('')
  const [confirmDelete, setConfirmDelete] = useState(null)
  const [createError, setCreateError] = useState('')

  const tier = user?.tier?.toUpperCase()

  const { data: endpoints = [] } = useQuery({
    queryKey: ['endpoints'],
    queryFn: () => api.get('/endpoints').then((r) => r.data),
  })

  const createMutation = useMutation({
    mutationFn: (label) => api.post('/endpoints', { label }),
    onSuccess: (res) => {
      qc.invalidateQueries({ queryKey: ['endpoints'] })
      navigate(`/dashboard/${res.data.slug}`)
      setCreating(false)
      setNewLabel('')
      setCreateError('')
    },
    onError: (err) => {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to create endpoint'
      setCreateError(msg)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => api.delete(`/endpoints/${id}`),
    onSuccess: (_, id) => {
      qc.invalidateQueries({ queryKey: ['endpoints'] })
      const remaining = endpoints.filter((e) => e.id !== id)
      if (remaining.length > 0) {
        navigate(`/dashboard/${remaining[0].slug}`)
      } else {
        navigate('/')
      }
      setConfirmDelete(null)
    },
  })

  const handleCreate = (e) => {
    e.preventDefault()
    if (!newLabel.trim()) return
    setCreateError('')
    createMutation.mutate(newLabel.trim())
  }

  const cancelCreate = () => {
    setCreating(false)
    setNewLabel('')
    setCreateError('')
  }

  return (
    <aside className="w-56 shrink-0 h-full bg-surface-1 border-r border-border flex flex-col">
      <div className="px-4 py-4 border-b border-border flex items-center gap-2">
        <div className="w-7 h-7 rounded-lg bg-accent flex items-center justify-center">
          <Zap size={13} className="text-white" fill="white" />
        </div>
        <span className="font-mono text-sm font-semibold text-white tracking-tight">HookSpy</span>
      </div>

      <div className="flex-1 overflow-y-auto py-3 px-2">
        <div className="flex items-center justify-between px-2 mb-2">
          <span className="text-[10px] font-mono text-muted uppercase tracking-widest">Endpoints</span>
          <button
            onClick={() => { setCreating(true); setCreateError('') }}
            className="text-muted hover:text-white transition-colors"
            title="New endpoint"
          >
            <Plus size={13} />
          </button>
        </div>

        {creating && (
          <form onSubmit={handleCreate} className="mb-2 px-1 animate-slide_in">
            <input
              autoFocus
              value={newLabel}
              onChange={(e) => setNewLabel(e.target.value)}
              onKeyDown={(e) => e.key === 'Escape' && cancelCreate()}
              placeholder="stripe-test"
              className="w-full bg-surface-3 border border-accent/50 rounded-md px-2 py-1.5 text-xs text-white placeholder:text-muted focus:outline-none font-mono"
            />

            {createError && (
              <div className="flex items-start gap-1.5 mt-1.5 px-1 animate-fade_in">
                <AlertCircle size={11} className="text-danger shrink-0 mt-0.5" />
                <span className="text-[11px] text-danger leading-tight">{createError}</span>
              </div>
            )}

            <div className="flex gap-1 mt-1.5">
              <button
                type="submit"
                disabled={createMutation.isPending}
                className="flex-1 bg-accent/20 hover:bg-accent/30 text-accent text-[11px] rounded px-2 py-1 transition-colors disabled:opacity-50"
              >
                {createMutation.isPending ? '…' : 'Create'}
              </button>
              <button
                type="button"
                onClick={cancelCreate}
                className="flex-1 bg-surface-3 hover:bg-surface-4 text-muted text-[11px] rounded px-2 py-1 transition-colors"
              >
                Cancel
              </button>
            </div>
          </form>
        )}

        <ul className="space-y-0.5">
          {endpoints.map((ep) => (
            <li key={ep.id}>
              {confirmDelete === ep.id ? (
                <div className="px-2 py-2 rounded-lg bg-danger/10 border border-danger/30 animate-slide_in">
                  <p className="text-[11px] text-danger mb-2">Delete "{ep.label}"?</p>
                  <div className="flex gap-1">
                    <button
                      onClick={() => deleteMutation.mutate(ep.id)}
                      disabled={deleteMutation.isPending}
                      className="flex-1 bg-danger/20 hover:bg-danger/30 text-danger text-[11px] rounded px-2 py-1 transition-colors"
                    >
                      {deleteMutation.isPending ? '…' : 'Delete'}
                    </button>
                    <button
                      onClick={() => setConfirmDelete(null)}
                      className="flex-1 bg-surface-3 text-muted text-[11px] rounded px-2 py-1"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              ) : (
                <div
                  onClick={() => navigate(`/dashboard/${ep.slug}`)}
                  className={cn(
                    'group flex items-center justify-between px-2 py-2 rounded-lg cursor-pointer transition-colors',
                    ep.slug === slug
                      ? 'bg-accent/15 text-white'
                      : 'text-muted hover:bg-surface-3 hover:text-white'
                  )}
                >
                  <div className="flex items-center gap-2 min-w-0">
                    <ChevronRight
                      size={11}
                      className={cn(
                        'shrink-0',
                        ep.slug === slug ? 'text-accent' : 'opacity-0 group-hover:opacity-100'
                      )}
                    />
                    <span className="text-xs font-mono truncate">{ep.label}</span>
                  </div>
                  <button
                    onClick={(e) => { e.stopPropagation(); setConfirmDelete(ep.id) }}
                    className="opacity-0 group-hover:opacity-100 text-muted hover:text-danger transition-all"
                  >
                    <Trash2 size={11} />
                  </button>
                </div>
              )}
            </li>
          ))}

          {endpoints.length === 0 && !creating && (
            <li className="px-2 py-4 text-center text-xs text-muted">
              No endpoints yet
            </li>
          )}
        </ul>
      </div>

      <div className="border-t border-border px-3 py-3 space-y-1">
        {tier !== 'TEAM' && (
          <button
            onClick={() => navigate('/upgrade')}
            className="w-full flex items-center gap-2 px-2 py-1.5 rounded-lg text-xs text-muted hover:text-warning hover:bg-warning/10 transition-colors"
          >
            <CreditCard size={12} />
            Upgrade plan
          </button>
        )}
        <div className="flex items-center justify-between px-2 py-1">
          <div className="min-w-0">
            <p className="text-[11px] text-white font-mono truncate">{user?.email}</p>
            <span className={cn('text-[10px] font-mono px-1.5 py-0.5 rounded border inline-block mt-0.5', `tier-${tier}`)}>
              {tier}
            </span>
          </div>

          <a href="mailto:support@hookspy.in"
            className="w-full flex items-center gap-2 px-2 py-1.5 rounded-lg text-xs text-muted hover:text-white hover:bg-surface-3 transition-colors">
            <HelpCircle size={12} />
            support@hookspy.in
          </a>

          <button
            onClick={logout}
            className="text-muted hover:text-white transition-colors shrink-0 ml-2"
            title="Sign out">
            <LogOut size={13} />
          </button>
        </div>
      </div>
    </aside>
  )
}
