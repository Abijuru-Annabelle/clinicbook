import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Alert, EmptyState, PageHeader, Spinner, StatusBadge } from '../components/ui'
import { appointmentApi } from '../api/services'
import { canChange, errMsg, fmtDate, fmtTime, parseLocal } from '../utils/format'
import { btn } from '../utils/styles'

export default function MyAppointments() {
  const [items, setItems] = useState([])
  const [tab, setTab] = useState('upcoming')
  const [loading, setLoading] = useState(true)
  const [reload, setReload] = useState(0)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [now] = useState(() => new Date())

  useEffect(() => {
    appointmentApi
      .mine()
      .then(setItems)
      .catch((e) => setError(errMsg(e)))
      .finally(() => setLoading(false))
  }, [reload])

  const upcoming = items
    .filter((a) => a.status === 'BOOKED' && parseLocal(a.startTime) >= now)
    .sort((a, b) => a.startTime.localeCompare(b.startTime))
  const past = items.filter((a) => !upcoming.includes(a))
  const list = tab === 'upcoming' ? upcoming : past

  const onCancel = async (a) => {
    if (!window.confirm(`Cancel your appointment with ${a.doctorName} on ${fmtDate(a.startTime)} at ${fmtTime(a.startTime)}?`)) return
    setError('')
    try {
      await appointmentApi.cancel(a.id)
      setMessage('Appointment cancelled. The slot is now free for others.')
      setReload((r) => r + 1)
    } catch (e) {
      setError(errMsg(e))
    }
  }

  const renderActions = (a) =>
    canChange(a) ? (
      <div className="flex gap-2">
        <Link to={`/book/${a.doctorId}?reschedule=${a.id}`} className={`${btn.secondary} px-3 py-1.5`}>Reschedule</Link>
        <button onClick={() => onCancel(a)} className={`${btn.danger} px-3 py-1.5`}>Cancel</button>
      </div>
    ) : null

  const tabClass = (t) =>
    `cursor-pointer rounded-md px-4 py-1.5 text-sm font-medium ${tab === t ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`

  return (
    <>
      <PageHeader title="My Appointments" subtitle="You can cancel or reschedule up to 2 hours before the visit.">
        <Link to="/doctors" className={btn.primary}>+ New appointment</Link>
      </PageHeader>

      <div className="mb-4 inline-flex rounded-lg bg-slate-200 p-1">
        <button className={tabClass('upcoming')} onClick={() => setTab('upcoming')}>Upcoming ({upcoming.length})</button>
        <button className={tabClass('past')} onClick={() => setTab('past')}>History ({past.length})</button>
      </div>

      <div className="mb-4 space-y-2">
        <Alert>{error}</Alert>
        <Alert type="success">{message}</Alert>
      </div>

      {loading ? (
        <Spinner />
      ) : list.length === 0 ? (
        <EmptyState title={tab === 'upcoming' ? 'No upcoming appointments' : 'No past appointments'} text={tab === 'upcoming' ? 'Book one from "Find a Doctor".' : undefined} />
      ) : (
        <>
          {/* Desktop / tablet: table */}
          <div className="hidden overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm md:block">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
                <tr>
                  <th className="px-4 py-3">Date & time</th>
                  <th className="px-4 py-3">Doctor</th>
                  <th className="px-4 py-3">Reason</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {list.map((a) => (
                  <tr key={a.id}>
                    <td className="px-4 py-3">
                      <p className="font-medium">{fmtDate(a.startTime)}</p>
                      <p className="text-slate-500">{fmtTime(a.startTime)} – {fmtTime(a.endTime)}</p>
                    </td>
                    <td className="px-4 py-3">
                      <p className="font-medium">{a.doctorName}</p>
                      <p className="text-slate-500">{a.departmentName}</p>
                    </td>
                    <td className="max-w-xs px-4 py-3 text-slate-600">
                      {a.reason || '—'}
                      {a.notes && <p className="mt-1 text-xs text-slate-400">Doctor's note: {a.notes}</p>}
                    </td>
                    <td className="px-4 py-3"><StatusBadge status={a.status} /></td>
                    <td className="px-4 py-3 text-right">{renderActions(a)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Mobile: cards */}
          <div className="space-y-3 md:hidden">
            {list.map((a) => (
              <article key={a.id} className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-semibold">{fmtDate(a.startTime)}</p>
                    <p className="text-sm text-slate-500">{fmtTime(a.startTime)} – {fmtTime(a.endTime)}</p>
                  </div>
                  <StatusBadge status={a.status} />
                </div>
                <p className="mt-3 text-sm font-medium">{a.doctorName}</p>
                <p className="text-sm text-slate-500">{a.departmentName}</p>
                {a.reason && <p className="mt-2 text-sm text-slate-600">{a.reason}</p>}
                {a.notes && <p className="mt-1 text-xs text-slate-400">Doctor's note: {a.notes}</p>}
                <div className="mt-3">{renderActions(a)}</div>
              </article>
            ))}
          </div>
        </>
      )}
    </>
  )
}
