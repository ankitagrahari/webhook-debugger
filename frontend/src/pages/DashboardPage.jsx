import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import Sidebar from '../components/Sidebar'
import UrlBar from '../components/UrlBar'
import RequestList from '../components/RequestList'
import RequestDetail from '../components/RequestDetail'
import api from '../lib/api'

export default function DashboardPage() {
  const { slug } = useParams()
  const [selectedRequest, setSelectedRequest] = useState(null)
  const [isLive, setIsLive] = useState(false)

  const { data: endpoints = [] } = useQuery({
    queryKey: ['endpoints'],
    queryFn: () => api.get('/endpoints').then((r) => r.data),
  })

  const endpoint = endpoints.find((e) => e.slug === slug)

  const handleLiveUpdate = () => {
    setIsLive(true)
    clearTimeout(window._liveTimeout)
    window._liveTimeout = setTimeout(() => setIsLive(false), 5000)
  }

  const handleSelect = (req) => {
    setSelectedRequest((prev) => (prev?.id === req.id ? null : req))
  }

  return (
    <div className="h-screen flex bg-surface-0 overflow-hidden">
      <Sidebar />

      <div className="flex-1 flex flex-col min-w-0">
        <UrlBar endpoint={endpoint} isLive={isLive} />

        <div className="flex-1 flex min-h-0">
          {/* Request list */}
          <div className={`flex flex-col border-r border-border ${selectedRequest ? 'w-[380px] shrink-0' : 'flex-1'} transition-all duration-150`}>
            <RequestList
              slug={slug}
              selectedId={selectedRequest?.id}
              onSelect={handleSelect}
              onLiveUpdate={handleLiveUpdate}
            />
          </div>

          {/* Detail panel — pass endpoint for replay URL + curl */}
          {selectedRequest && (
            <div className="flex-1 flex flex-col min-w-0 min-h-0">
              <RequestDetail
                request={selectedRequest}
                endpoint={endpoint}
                onClose={() => setSelectedRequest(null)}
              />
            </div>
          )}

          {!selectedRequest && (
            <div className="hidden lg:flex flex-1 items-center justify-center">
              <p className="text-xs text-surface-4 font-mono">← select a request</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}