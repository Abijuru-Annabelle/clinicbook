import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Alert, EmptyState, PageHeader, Spinner } from '../components/ui'
import { departmentApi, doctorApi } from '../api/services'
import { errMsg, initials } from '../utils/format'
import { btn, input } from '../utils/styles'

export default function FindDoctor() {
  const [departments, setDepartments] = useState([])
  const [doctors, setDoctors] = useState([])
  const [search, setSearch] = useState('')
  const [departmentId, setDepartmentId] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    departmentApi.list().then(setDepartments).catch((e) => setError(errMsg(e)))
  }, [])

  // Debounced search: waits 300 ms after typing stops
  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true)
      doctorApi
        .list({ search, departmentId })
        .then(setDoctors)
        .catch((e) => setError(errMsg(e)))
        .finally(() => setLoading(false))
    }, 300)
    return () => clearTimeout(timer)
  }, [search, departmentId])

  return (
    <>
      <PageHeader title="Find a Doctor" subtitle="Search by name or specialty, then pick a time that suits you." />

      <div className="mb-6 flex flex-col gap-3 sm:flex-row">
        <input
          type="search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search doctor or specialty…"
          className={`${input} sm:flex-1`}
          aria-label="Search doctors"
        />
        <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)} className={`${input} sm:w-60`} aria-label="Department">
          <option value="">All departments</option>
          {departments.map((d) => (
            <option key={d.id} value={d.id}>{d.name}</option>
          ))}
        </select>
      </div>

      <Alert>{error}</Alert>

      {loading ? (
        <Spinner />
      ) : doctors.length === 0 ? (
        <EmptyState title="No doctors found" text="Try another name or department." />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {doctors.map((d) => (
            <article key={d.id} className="flex flex-col rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md">
              <div className="flex items-center gap-3">
                <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-teal-100 font-bold text-teal-700">
                  {initials(d.fullName)}
                </div>
                <div className="min-w-0">
                  <h2 className="truncate font-semibold text-slate-900">{d.fullName}</h2>
                  <p className="truncate text-sm text-slate-500">{d.specialization}</p>
                </div>
              </div>
              <div className="mt-4 flex flex-wrap gap-2 text-xs">
                <span className="rounded-full bg-slate-100 px-2.5 py-1 text-slate-600">{d.departmentName}</span>
                <span className="rounded-full bg-slate-100 px-2.5 py-1 text-slate-600">{d.slotMinutes} min visits</span>
              </div>
              <Link to={`/book/${d.id}`} className={`${btn.primary} mt-5 w-full`}>
                Book appointment
              </Link>
            </article>
          ))}
        </div>
      )}
    </>
  )
}
