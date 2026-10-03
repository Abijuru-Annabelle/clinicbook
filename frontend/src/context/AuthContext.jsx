import { useCallback, useMemo, useState } from 'react'
import { AuthContext } from './auth'

const readUser = () => {
  try {
    return JSON.parse(localStorage.getItem('user'))
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readUser)

  const saveSession = useCallback(({ token, user }) => {
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(user))
    setUser(user)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }, [])

  const value = useMemo(() => ({ user, saveSession, logout }), [user, saveSession, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
