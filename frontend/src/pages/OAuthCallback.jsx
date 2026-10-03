import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { Alert, Spinner } from '../components/ui'
import { useAuth } from '../context/auth'
import { authApi } from '../api/services'
import { errMsg, homeFor } from '../utils/format'

// The backend redirects here after Google login: /oauth2/callback?token=JWT
export default function OAuthCallback() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const { saveSession } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')

  useEffect(() => {
    if (!token) return
    localStorage.setItem('token', token)
    authApi
      .me()
      .then((user) => {
        saveSession({ token, user })
        navigate(homeFor(user.role), { replace: true })
      })
      .catch((e) => setError(errMsg(e)))
  }, [token, saveSession, navigate])

  const problem = !token ? 'Google sign-in failed. Please try again.' : error
  return (
    <div className="mx-auto max-w-md p-6">
      {problem ? (
        <div className="space-y-4">
          <Alert>{problem}</Alert>
          <Link to="/login" className="text-sm font-medium text-teal-700 hover:underline">Back to login</Link>
        </div>
      ) : (
        <Spinner />
      )}
    </div>
  )
}
