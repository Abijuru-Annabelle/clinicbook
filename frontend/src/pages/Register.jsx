import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthShell from '../components/AuthShell'
import { Alert } from '../components/ui'
import { useAuth } from '../context/auth'
import { authApi } from '../api/services'
import { errMsg, homeFor } from '../utils/format'
import { btn, input, label } from '../utils/styles'

const validate = (f) => {
  if (!f.fullName.trim()) return 'Full name is required.'
  if (!/^\S+@\S+\.\S+$/.test(f.email)) return 'Enter a valid email address.'
  if (f.phone && !/^\+?\d{9,15}$/.test(f.phone)) return 'Phone must be 9–15 digits, e.g. +250788123456.'
  if (f.password.length < 8) return 'Password must be at least 8 characters.'
  if (f.password !== f.confirm) return 'Passwords do not match.'
  return ''
}

export default function Register() {
  const { saveSession } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '', confirm: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const update = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const onSubmit = async (e) => {
    e.preventDefault()
    const problem = validate(form)
    if (problem) return setError(problem)
    setError('')
    setLoading(true)
    try {
      const session = await authApi.register({
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        phone: form.phone.trim(),
        password: form.password,
      })
      saveSession(session)
      navigate(homeFor(session.user.role))
    } catch (err) {
      setError(errMsg(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthShell title="Create your account" subtitle="Register as a patient to start booking.">
      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        <Alert>{error}</Alert>
        <div>
          <label htmlFor="fullName" className={label}>Full name</label>
          <input id="fullName" name="fullName" value={form.fullName} onChange={update} className={input} />
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="email" className={label}>Email</label>
            <input id="email" name="email" type="email" value={form.email} onChange={update} className={input} />
          </div>
          <div>
            <label htmlFor="phone" className={label}>Phone (for SMS)</label>
            <input id="phone" name="phone" type="tel" value={form.phone} onChange={update} className={input} placeholder="+250788123456" />
          </div>
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="password" className={label}>Password</label>
            <input id="password" name="password" type="password" value={form.password} onChange={update} className={input} />
          </div>
          <div>
            <label htmlFor="confirm" className={label}>Confirm password</label>
            <input id="confirm" name="confirm" type="password" value={form.confirm} onChange={update} className={input} />
          </div>
        </div>
        <button type="submit" disabled={loading} className={`${btn.primary} w-full`}>
          {loading ? 'Creating account…' : 'Create account'}
        </button>
        <p className="text-center text-sm text-slate-500">
          Already registered? <Link to="/login" className="font-medium text-teal-700 hover:underline">Log in</Link>
        </p>
      </form>
    </AuthShell>
  )
}
