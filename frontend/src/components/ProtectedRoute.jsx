import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/auth'

// Frontend RBAC: hides pages from the wrong role.
// The backend enforces the same rules (the real security).
export default function ProtectedRoute({ roles, children }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (roles && !roles.includes(user.role)) return <Navigate to="/unauthorized" replace />
  return children
}
