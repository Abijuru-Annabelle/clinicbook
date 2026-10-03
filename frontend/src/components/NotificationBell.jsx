import { useEffect, useRef, useState } from 'react'
import { notificationApi } from '../api/services'
import { errMsg } from '../utils/format'

const CHANNEL_STYLES = {
  EMAIL: 'bg-sky-100 text-sky-800',
  SMS: 'bg-violet-100 text-violet-800',
}

const STATUS_STYLES = {
  SENT: 'text-green-700',
  SIMULATED: 'text-slate-500',
  FAILED: 'text-red-600',
}

/** Latest email/SMS notifications (sent through RabbitMQ, logged in MongoDB). */
export default function NotificationBell() {
  const [open, setOpen] = useState(false)
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const boxRef = useRef(null)

  // Close when clicking outside
  useEffect(() => {
    if (!open) return
    const onClick = (e) => {
      if (boxRef.current && !boxRef.current.contains(e.target)) setOpen(false)
    }
    document.addEventListener('mousedown', onClick)
    return () => document.removeEventListener('mousedown', onClick)
  }, [open])

  const toggle = async () => {
    const next = !open
    setOpen(next)
    if (!next) return
    setLoading(true)
    setError('')
    try {
      setItems(await notificationApi.mine())
    } catch (e) {
      setError(errMsg(e))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="relative" ref={boxRef}>
      <button
        onClick={toggle}
        className="cursor-pointer rounded-lg p-2 text-slate-600 hover:bg-slate-100"
        aria-label="Notifications"
      >
        <svg className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.4-1.4A2 2 0 0118 14.2V11a6 6 0 10-12 0v3.2c0 .5-.2 1-.6 1.4L4 17h5m6 0a3 3 0 11-6 0" />
        </svg>
      </button>

      {open && (
        <div className="absolute right-0 z-30 mt-2 w-80 max-w-[calc(100vw-2rem)] rounded-xl border border-slate-200 bg-white shadow-lg">
          <div className="border-b border-slate-100 px-4 py-3">
            <p className="font-semibold text-slate-900">Notifications</p>
            <p className="text-xs text-slate-500">Email & SMS sent to you</p>
          </div>
          <div className="max-h-96 overflow-y-auto">
            {loading ? (
              <p className="px-4 py-6 text-center text-sm text-slate-500">Loading…</p>
            ) : error ? (
              <p className="px-4 py-6 text-center text-sm text-red-600">{error}</p>
            ) : items.length === 0 ? (
              <p className="px-4 py-6 text-center text-sm text-slate-500">No notifications yet.</p>
            ) : (
              <ul className="divide-y divide-slate-100">
                {items.map((n) => (
                  <li key={n.id} className="px-4 py-3">
                    <div className="mb-1 flex items-center gap-2 text-xs">
                      <span className={`rounded px-1.5 py-0.5 font-semibold ${CHANNEL_STYLES[n.channel] || ''}`}>{n.channel}</span>
                      <span className="text-slate-400">{n.type}</span>
                      <span className={`ml-auto ${STATUS_STYLES[n.status] || ''}`}>{n.status}</span>
                    </div>
                    <p className="text-sm text-slate-700">{n.message}</p>
                    <p className="mt-1 text-xs text-slate-400">{new Date(n.createdAt).toLocaleString('en-GB')}</p>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
