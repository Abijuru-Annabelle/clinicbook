import { useEffect, useState } from 'react'
import { Alert, PageHeader } from '../components/ui'
import { departmentApi, doctorApi } from '../api/services'
import { errMsg } from '../utils/format'
import { btn, card, input, label } from '../utils/styles'

const EMPTY_DOCTOR = { fullName: '', email: '', phone: '', specialization: '', licenseNumber: '', departmentId: '', slotMinutes: '30' }

export default function AdminDashboard() {
  const [departments, setDepartments] = useState([])
  const [doctors, setDoctors] = useState([])
  const [reload, setReload] = useState(0)
  const [dept, setDept] = useState({ name: '', description: '' })
  const [doc, setDoc] = useState(EMPTY_DOCTOR)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    Promise.all([departmentApi.list(), doctorApi.list()])
      .then(([d, docs]) => {
        setDepartments(d)
        setDoctors(docs)
      })
      .catch((e) => setError(errMsg(e)))
  }, [reload])

  const run = async (action, success) => {
    setError('')
    setMessage('')
    try {
      await action()
      setMessage(success)
      setReload((r) => r + 1)
      return true
    } catch (e) {
      setError(errMsg(e))
      return false
    }
  }

  const addDepartment = async (e) => {
    e.preventDefault()
    if (!dept.name.trim()) return setError('Department name is required.')
    if (await run(() => departmentApi.create(dept), `Department "${dept.name}" added.`)) setDept({ name: '', description: '' })
  }

  const addDoctor = async (e) => {
    e.preventDefault()
    const required = ['fullName', 'email', 'specialization', 'licenseNumber', 'departmentId']
    if (required.some((k) => !String(doc[k]).trim())) return setError('Please fill in all doctor fields.')
    const ok = await run(
      () => doctorApi.create({ ...doc, departmentId: Number(doc.departmentId), slotMinutes: Number(doc.slotMinutes) }),
      `${doc.fullName} added. Login details were sent to ${doc.email}.`,
    )
    if (ok) setDoc(EMPTY_DOCTOR)
  }

  const removeDepartment = (d) =>
    window.confirm(`Delete department "${d.name}"?`) && run(() => departmentApi.remove(d.id), 'Department deleted.')

  const removeDoctor = (d) =>
    window.confirm(`Remove ${d.fullName}?`) && run(() => doctorApi.remove(d.id), 'Doctor removed.')

  const setD = (e) => setDoc({ ...doc, [e.target.name]: e.target.value })

  return (
    <>
      <PageHeader title="Admin Dashboard" subtitle="Manage departments and doctors." />

      <div className="mb-6 grid grid-cols-2 gap-3 sm:grid-cols-4">
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-xs font-medium text-slate-500 uppercase">Departments</p>
          <p className="mt-1 text-2xl font-bold">{departments.length}</p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-xs font-medium text-slate-500 uppercase">Doctors</p>
          <p className="mt-1 text-2xl font-bold">{doctors.length}</p>
        </div>
      </div>

      <div className="mb-4 space-y-2">
        <Alert>{error}</Alert>
        <Alert type="success">{message}</Alert>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Departments */}
        <section className={card}>
          <h2 className="mb-4 font-semibold text-slate-900">Departments</h2>
          <form onSubmit={addDepartment} className="space-y-3">
            <div>
              <label htmlFor="deptName" className={label}>Name</label>
              <input id="deptName" value={dept.name} onChange={(e) => setDept({ ...dept, name: e.target.value })} className={input} />
            </div>
            <div>
              <label htmlFor="deptDesc" className={label}>Description</label>
              <input id="deptDesc" value={dept.description} onChange={(e) => setDept({ ...dept, description: e.target.value })} className={input} />
            </div>
            <button type="submit" className={`${btn.primary} w-full`}>Add department</button>
          </form>
          <ul className="mt-5 divide-y divide-slate-100">
            {departments.map((d) => (
              <li key={d.id} className="flex items-center justify-between gap-3 py-2">
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium">{d.name}</p>
                  <p className="truncate text-xs text-slate-500">{d.description}</p>
                </div>
                <button onClick={() => removeDepartment(d)} className="cursor-pointer text-xs font-medium text-red-600 hover:underline">Delete</button>
              </li>
            ))}
          </ul>
        </section>

        {/* Doctors */}
        <section className={`${card} lg:col-span-2`}>
          <h2 className="mb-4 font-semibold text-slate-900">Doctors</h2>
          <form onSubmit={addDoctor} className="grid gap-3 sm:grid-cols-2">
            <div>
              <label htmlFor="fullName" className={label}>Full name</label>
              <input id="fullName" name="fullName" value={doc.fullName} onChange={setD} className={input} placeholder="Dr. …" />
            </div>
            <div>
              <label htmlFor="docEmail" className={label}>Email</label>
              <input id="docEmail" name="email" type="email" value={doc.email} onChange={setD} className={input} />
            </div>
            <div>
              <label htmlFor="specialization" className={label}>Specialization</label>
              <input id="specialization" name="specialization" value={doc.specialization} onChange={setD} className={input} />
            </div>
            <div>
              <label htmlFor="license" className={label}>License number</label>
              <input id="license" name="licenseNumber" value={doc.licenseNumber} onChange={setD} className={input} placeholder="RMDC-…" />
            </div>
            <div>
              <label htmlFor="docDept" className={label}>Department</label>
              <select id="docDept" name="departmentId" value={doc.departmentId} onChange={setD} className={input}>
                <option value="">Select…</option>
                {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
              </select>
            </div>
            <div>
              <label htmlFor="slot" className={label}>Visit length</label>
              <select id="slot" name="slotMinutes" value={doc.slotMinutes} onChange={setD} className={input}>
                {[15, 20, 30, 45, 60].map((m) => <option key={m} value={m}>{m} minutes</option>)}
              </select>
            </div>
            <button type="submit" className={`${btn.primary} sm:col-span-2`}>Add doctor</button>
          </form>

          <div className="mt-6 overflow-x-auto">
            <table className="w-full min-w-[540px] text-left text-sm">
              <thead className="text-xs text-slate-500 uppercase">
                <tr className="border-b border-slate-200">
                  <th className="py-2 pr-3">Doctor</th>
                  <th className="py-2 pr-3">Department</th>
                  <th className="py-2 pr-3">License</th>
                  <th className="py-2"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {doctors.map((d) => (
                  <tr key={d.id}>
                    <td className="py-2 pr-3">
                      <p className="font-medium">{d.fullName}</p>
                      <p className="text-xs text-slate-500">{d.specialization} · {d.email}</p>
                    </td>
                    <td className="py-2 pr-3">{d.departmentName}</td>
                    <td className="py-2 pr-3 text-slate-500">{d.licenseNumber}</td>
                    <td className="py-2 text-right">
                      <button onClick={() => removeDoctor(d)} className="cursor-pointer text-xs font-medium text-red-600 hover:underline">Remove</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </>
  )
}
