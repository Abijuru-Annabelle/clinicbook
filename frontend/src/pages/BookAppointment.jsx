import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Alert, Spinner } from '../components/ui'
import { appointmentApi, doctorApi } from '../api/services'
import { errMsg, fmtDate, fmtTime, initials, toDateStr, todayStr } from '../utils/format'
import { btn, card, input, label } from '../utils/styles'

const nextDays = (n) =>
  Array.from({ length: n }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() + i)
    return d
  })

export default function BookAppointment() {
  const { doctorId } = useParams()
  const [params] = useSearchParams()
  const rescheduleId = params.get('reschedule')
  const navigate = useNavigate()

  const [doctor, setDoctor] = useState(null)
  const [date, setDate] = useState(todayStr())
  const [slots, setSlots] = useState([])
  const [selected, setSelected] = useState(null)
  const [reason, setReason] = useState('')
  const [loadingSlots, setLoadingSlots] = useState(true)
  const [reload, setReload] = useState(0)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const days = useMemo(() => nextDays(14), [])

  useEffect(() => {
    doctorApi.get(doctorId).then(setDoctor).catch((e) => setError(errMsg(e)))
  }, [doctorId])

  useEffect(() => {
    let active = true
    doctorApi
      .slots(doctorId, date)
      .then((s) => active && setSlots(s))
      .catch((e) => active && setError(errMsg(e)))
      .finally(() => active && setLoadingSlots(false))
    return () => {
      active = false
    }
  }, [doctorId, date, reload])

  const pickDate = (value) => {
    setDate(value)
    setSelected(null)
    setLoadingSlots(true)
  }

  const submit = async () => {
    if (!selected) return
    setSubmitting(true)
    setError('')
    try {
      if (rescheduleId) await appointmentApi.reschedule(rescheduleId, selected)
      else await appointmentApi.book({ doctorId: Number(doctorId), startTime: selected, reason })
      setSuccess(
        rescheduleId
          ? 'Appointment rescheduled. A confirmation is on its way to your email and phone.'
          : 'Appointment booked! A confirmation is on its way to your email and phone.',
      )
      setTimeout(() => navigate('/appointments'), 1800)
    } catch (e) {
      setError(errMsg(e))
      setSelected(null)
      setLoadingSlots(true)
      setReload((r) => r + 1) // refresh slots, someone may have taken it
    } finally {
      setSubmitting(false)
    }
  }

  if (!doctor && !error) return <Spinner />

  return (
    <>
      <Link to={rescheduleId ? '/appointments' : '/doctors'} className="mb-4 inline-block text-sm font-medium text-teal-700 hover:underline">
        ← Back
      </Link>
      <h1 className="mb-6 text-2xl font-bold text-slate-900">{rescheduleId ? 'Reschedule appointment' : 'Book an appointment'}</h1>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Doctor + summary: top on mobile, right sidebar on desktop */}
        <aside className="space-y-4 lg:order-2">
          {doctor && (
            <div className={card}>
              <div className="flex items-center gap-3">
                <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-teal-100 text-lg font-bold text-teal-700">
                  {initials(doctor.fullName)}
                </div>
                <div>
                  <p className="font-semibold text-slate-900">{doctor.fullName}</p>
                  <p className="text-sm text-slate-500">{doctor.specialization}</p>
                  <p className="text-xs text-slate-400">{doctor.departmentName} · {doctor.slotMinutes} min</p>
                </div>
              </div>
            </div>
          )}
          <div className={`${card} lg:sticky lg:top-20`}>
            <h2 className="font-semibold text-slate-900">Summary</h2>
            <dl className="mt-3 space-y-2 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-slate-500">Date</dt>
                <dd className="text-right font-medium">{fmtDate(`${date}T00:00`)}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-slate-500">Time</dt>
                <dd className="font-medium">{selected ? fmtTime(selected) : '—'}</dd>
              </div>
            </dl>
            <button onClick={submit} disabled={!selected || submitting || !!success} className={`${btn.primary} mt-5 w-full`}>
              {submitting ? 'Saving…' : rescheduleId ? 'Confirm new time' : 'Confirm booking'}
            </button>
          </div>
        </aside>

        <section className="space-y-6 lg:order-1 lg:col-span-2">
          <Alert>{error}</Alert>
          <Alert type="success">{success}</Alert>

          <div className={card}>
            <h2 className="mb-3 font-semibold text-slate-900">1. Choose a day</h2>
            {/* Horizontal scroll on small screens */}
            <div className="-mx-1 flex gap-2 overflow-x-auto px-1 pb-2">
              {days.map((d) => {
                const value = toDateStr(d)
                const active = value === date
                return (
                  <button
                    key={value}
                    onClick={() => pickDate(value)}
                    className={`flex w-16 shrink-0 cursor-pointer flex-col items-center rounded-lg border py-2 text-sm transition ${
                      active ? 'border-teal-600 bg-teal-600 text-white' : 'border-slate-200 bg-white hover:border-teal-400'
                    }`}
                  >
                    <span className="text-xs uppercase">{d.toLocaleDateString('en-GB', { weekday: 'short' })}</span>
                    <span className="text-lg font-bold">{d.getDate()}</span>
                    <span className="text-xs">{d.toLocaleDateString('en-GB', { month: 'short' })}</span>
                  </button>
                )
              })}
            </div>
          </div>

          <div className={card}>
            <h2 className="mb-3 font-semibold text-slate-900">2. Choose a time</h2>
            {loadingSlots ? (
              <Spinner />
            ) : slots.length === 0 ? (
              <p className="text-sm text-slate-500">The doctor is not available on this day. Please pick another day.</p>
            ) : (
              <div className="grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-6">
                {slots.map((s) => {
                  const active = s.startTime === selected
                  return (
                    <button
                      key={s.startTime}
                      disabled={!s.available}
                      onClick={() => setSelected(s.startTime)}
                      className={`cursor-pointer rounded-lg border py-2 text-sm font-medium transition disabled:cursor-not-allowed disabled:border-slate-100 disabled:bg-slate-100 disabled:text-slate-400 disabled:line-through ${
                        active ? 'border-teal-600 bg-teal-600 text-white' : 'border-slate-200 bg-white hover:border-teal-400'
                      }`}
                    >
                      {fmtTime(s.startTime)}
                    </button>
                  )
                })}
              </div>
            )}
          </div>

          {!rescheduleId && (
            <div className={card}>
              <label htmlFor="reason" className={`${label} font-semibold`}>3. Reason for visit (optional)</label>
              <textarea id="reason" rows={3} maxLength={255} value={reason} onChange={(e) => setReason(e.target.value)} className={input} placeholder="e.g. Fever for 3 days" />
            </div>
          )}
        </section>
      </div>
    </>
  )
}
