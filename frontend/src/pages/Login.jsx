import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import AuthShell from '../components/AuthShell'
import { Alert } from '../components/ui'
import { useAuth } from '../context/auth'
import { authApi, googleLoginUrl, USE_MOCK } from '../api/services'
import { errMsg, homeFor } from '../utils/format'
import { btn, input, label } from '../utils/styles'

export default function Login() {
  const { saveSession } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [params] = useSearchParams()
  const urlError = params.get('error')
  const googleError = urlError === 'google' ? 'Google sign-in failed or was cancelled. Please try again.' : urlError

  const update = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const finish = (session) => {
    saveSession(session)
    navigate(homeFor(session.user.role))
  }

  const onSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!form.email || !form.password) return setError('Email and password are required.')
    setLoading(true)
    try {
      finish(await authApi.login(form.email, form.password))
    } catch (err) {
      setError(errMsg(err))
    } finally {
      setLoading(false)
    }
  }

  const onGoogle = async () => {
    if (USE_MOCK) return finish(await authApi.login('google.patient@gmail.com', 'google'))
    window.location.href = googleLoginUrl // Spring Security OAuth2 flow
  }

  return (
    <AuthShell title="Welcome back" subtitle="Log in to manage your appointments.">
      <div className="space-y-4">
        <Alert>{error || googleError}</Alert>

        <button type="button" onClick={onGoogle} className={btn.google}>
          <svg className="h-5 w-5" viewBox="0 0 48 48" aria-hidden="true">
            <path fill="#FFC107" d="M43.6 20.5H42V20H24v8h11.3C33.7 32.7 29.2 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.8 1.2 7.9 3.1l5.7-5.7C34 6.1 29.3 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 20-8.9 20-20c0-1.3-.1-2.4-.4-3.5z" />
            <path fill="#FF3D00" d="M6.3 14.7l6.6 4.8C14.7 15.1 19 12 24 12c3.1 0 5.8 1.2 7.9 3.1l5.7-5.7C34 6.1 29.3 4 24 4 16.3 4 9.7 8.3 6.3 14.7z" />
            <path fill="#4CAF50" d="M24 44c5.2 0 9.9-2 13.4-5.2l-6.2-5.2C29.2 35.1 26.7 36 24 36c-5.2 0-9.6-3.3-11.3-8l-6.5 5C9.5 39.6 16.2 44 24 44z" />
            <path fill="#1976D2" d="M43.6 20.5H42V20H24v8h11.3c-.8 2.2-2.2 4.2-4.1 5.6l6.2 5.2C37 39.2 44 34 44 24c0-1.3-.1-2.4-.4-3.5z" />
          </svg>
          Continue with Google
        </button>

        <div className="flex items-center gap-3 text-xs text-slate-400">
          <span className="h-px flex-1 bg-slate-200" /> OR <span className="h-px flex-1 bg-slate-200" />
        </div>

        <form onSubmit={onSubmit} className="space-y-4" noValidate>
          <div>
            <label htmlFor="email" className={label}>Email</label>
            <input id="email" name="email" type="email" autoComplete="email" value={form.email} onChange={update} className={input} placeholder="you@example.com" />
          </div>
          <div>
            <label htmlFor="password" className={label}>Password</label>
            <input id="password" name="password" type="password" autoComplete="current-password" value={form.password} onChange={update} className={input} placeholder="••••••••" />
          </div>
          <button type="submit" disabled={loading} className={`${btn.primary} w-full`}>
            {loading ? 'Logging in…' : 'Log in'}
          </button>
        </form>

        <p className="text-center text-sm text-slate-500">
          No account?{' '}
          <Link to="/register" className="font-medium text-teal-700 hover:underline">Register as a patient</Link>
        </p>

        {USE_MOCK && (
          <Alert type="info">
            <strong>Demo mode</strong> (any password works):<br />
            patient@clinicbook.rw · doctor@clinicbook.rw · admin@clinicbook.rw
          </Alert>
        )}
      </div>
    </AuthShell>
  )
}
