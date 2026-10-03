import { useEffect, useState } from 'react'
import { Alert, EmptyState, PageHeader, Spinner, StatusBadge } from '../components/ui'
import { appointmentApi, doctorSelfApi } from '../api/services'
import { DAYS, errMsg, fmtDate, fmtTime, todayStr } from '../utils/format'
import { btn, card, input, label } from '../utils/styles'

export default function DoctorDashboard() {
  const [date, setDate] = useState(todayStr())
  const [schedule, setSchedule] = useState([])
  const [loading, setLoading] = useState(true)
  const [reloadSchedule, setReloadSchedule] = useState(0)

  const [slots, setSlots] = useState([])
  const [reloadSlots, setReloadSlots] = useState(0)
  const [form, setForm] = useState({ dayOfWeek: '1', startTime: '08:00', endTime: '12:00' })

  const [error, setError] = useState('')

  useEffect(() => {
    doctorSelfApi
      .schedule(date)
      .then(setSchedule)
      .catch((e) => setError(errMsg(e)))
      .finally(() => setLoading(false))
  }, [date, reloadSchedule])

  useEffect(() => {
    doctorSelfApi.availability().then(setSlots).catch((e) => setError(errMsg(e)))
  }, [reloadSlots])

  const changeDate = (value) => {
    setDate(value)
    setLoading(true)
  }

  const setStatus = async (a, status) => {
    const notes = status === 'COMPLETED' ? window.prompt('Add a note for the patient (optional):', '') : ''
    if (notes === null) return // prompt cancelled
    setError('')
    try {
      await appointmentApi.updateStatus(a.id, status, notes)
      setReloadSchedule((r) => r + 1)
    } catch (e) {
      setError(errMsg(e))
    }
  }

  const addSlot = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await doctorSelfApi.addAvailability(form)
      setReloadSlots((r) => r + 1)
    } catch (err) {
      setError(errMsg(err))
    }
  }

  const removeSlot = async (id) => {
    try {
      await doctorSelfApi.removeAvailability(id)
      setReloadSlots((r) => r + 1)
    } catch (err) {
      setError(errMsg(err))
    }
  }

  const count = (s) => schedule.filter((a) => a.status === s).length
  const stats = [
    { label: 'Total', value: schedule.length, color: 'text-slate-900' },
    { label: 'Waiting', value: count('BOOKED'), color: 'text-sky-700' },
    { label: 'Completed', value: count('COMPLETED'), color: 'text-green-700' },
    { label: 'No-show', value: count('NO_SHOW'), color: 'text-amber-700' },
  ]

  return (
    <>
      <PageHeader title="My Schedule" subtitle={fmtDate(`${date}T00:00`)}>
        <input type="date" value={date} onChange={(e) => changeDate(e.target.value)} className={`${input} sm:w-48`} aria-label="Schedule date" />
      </PageHeader>

      <div className="mb-4"><Alert>{error}</Alert></div>

      <div className="mb-6 grid grid-cols-2 gap-3 sm:grid-cols-4">
        {stats.map((s) => (
          <div key={s.label} className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <p className="text-xs font-medium text-slate-500 uppercase">{s.label}</p>
            <p className={`mt-1 text-2xl font-bold ${s.color}`}>{s.value}</p>
          </div>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <section className="lg:col-span-2">
          <h2 className="mb-3 font-semibold text-slate-900">Appointments</h2>
          {loading ? (
            <Spinner />
          ) : schedule.length === 0 ? (
            <EmptyState title="No appointments on this day" />
          ) : (
            <ol className="space-y-3">
              {schedule.map((a) => (
                <li key={a.id} className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:flex-row sm:items-center">
                  <div className="w-24 shrink-0">
                    <p className="text-lg font-bold text-teal-700">{fmtTime(a.startTime)}</p>
                    <p className="text-xs text-slate-400">to {fmtTime(a.endTime)}</p>
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="font-semibold">{a.patientName}</p>
                      <StatusBadge status={a.status} />
                    </div>
                    <p className="text-sm text-slate-600">{a.reason || 'No reason given'}</p>
                    {a.notes && <p className="mt-1 text-xs text-slate-400">Note: {a.notes}</p>}
                  </div>
                  {a.status === 'BOOKED' && (
                    <div className="flex gap-2">
                      <button onClick={() => setStatus(a, 'COMPLETED')} className={`${btn.primary} px-3 py-1.5`}>Complete</button>
                      <button onClick={() => setStatus(a, 'NO_SHOW')} className={`${btn.secondary} px-3 py-1.5`}>No-show</button>
                    </div>
                  )}
                </li>
              ))}
            </ol>
          )}
        </section>

        <section className={card}>
          <h2 className="font-semibold text-slate-900">Weekly availability</h2>
          <p className="mb-4 text-sm text-slate-500">Patients can only book inside these hours.</p>

          <form onSubmit={addSlot} className="space-y-3">
            <div>
              <label htmlFor="day" className={label}>Day</label>
              <select id="day" value={form.dayOfWeek} onChange={(e) => setForm({ ...form, dayOfWeek: e.target.value })} className={input}>
                {DAYS.map((d, i) => <option key={d} value={i + 1}>{d}</option>)}
              </select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label htmlFor="start" className={label}>From</label>
                <input id="start" type="time" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} className={input} />
              </div>
              <div>
                <label htmlFor="end" className={label}>To</label>
                <input id="end" type="time" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} className={input} />
              </div>
            </div>
            <button type="submit" className={`${btn.primary} w-full`}>Add hours</button>
          </form>

          <ul className="mt-5 divide-y divide-slate-100">
            {slots.map((s) => (
              <li key={s.id} className="flex items-center justify-between py-2 text-sm">
                <span>
                  <span className="font-medium">{DAYS[s.dayOfWeek - 1]}</span>{' '}
                  <span className="text-slate-500">{s.startTime.slice(0, 5)}–{s.endTime.slice(0, 5)}</span>
                </span>
                <button onClick={() => removeSlot(s.id)} className="cursor-pointer text-xs font-medium text-red-600 hover:underline">Remove</button>
              </li>
            ))}
          </ul>
        </section>
      </div>
    </>
  )
}
