import { Link } from 'react-router-dom'
import { useAuth } from '../context/auth'
import { homeFor } from '../utils/format'
import { btn } from '../utils/styles'

export default function Unauthorized() {
  const { user } = useAuth()
  return (
    <div className="py-16 text-center">
      <p className="text-5xl font-bold text-teal-600">403</p>
      <h1 className="mt-4 text-2xl font-bold">Access denied</h1>
      <p className="mt-2 text-slate-500">Your role ({user?.role}) is not allowed to open this page.</p>
      <Link to={homeFor(user?.role)} className={`${btn.primary} mt-6`}>Go to my dashboard</Link>
    </div>
  )
}
