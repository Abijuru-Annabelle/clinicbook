import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/auth'
import { btn } from '../utils/styles'
import NotificationBell from './NotificationBell'
import { Logo } from './ui'

const LINKS = {
  PATIENT: [
    { to: '/doctors', label: 'Find a Doctor' },
    { to: '/appointments', label: 'My Appointments' },
  ],
  DOCTOR: [{ to: '/doctor', label: 'My Schedule' }],
  ADMIN: [{ to: '/admin', label: 'Dashboard' }],
}

export default function Navbar() {
  const { user, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const navigate = useNavigate()
  const links = LINKS[user?.role] || []

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const linkClass = ({ isActive }) =>
    `block rounded-lg px-3 py-2 text-sm font-medium ${isActive ? 'bg-teal-50 text-teal-700' : 'text-slate-600 hover:bg-slate-100'}`

  return (
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-white">
      <nav className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link to="/">
          <Logo />
        </Link>

        {/* Desktop menu */}
        <div className="hidden items-center gap-1 md:flex">
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} className={linkClass}>
              {l.label}
            </NavLink>
          ))}
          <div className="ml-2">
            <NotificationBell />
          </div>
          <span className="ml-2 text-sm text-slate-500">
            {user?.fullName} · <span className="font-semibold text-slate-700">{user?.role}</span>
          </span>
          <button onClick={handleLogout} className={`${btn.secondary} ml-3`}>
            Log out
          </button>
        </div>

        {/* Mobile: bell + hamburger */}
        <div className="flex items-center gap-1 md:hidden">
          <NotificationBell />
        <button
          onClick={() => setOpen((o) => !o)}
          className="cursor-pointer rounded-lg p-2 text-slate-600 hover:bg-slate-100"
          aria-label="Toggle menu"
        >
          <svg className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            {open ? (
              <path strokeLinecap="round" d="M6 6l12 12M18 6L6 18" />
            ) : (
              <path strokeLinecap="round" d="M4 6h16M4 12h16M4 18h16" />
            )}
          </svg>
        </button>
        </div>
      </nav>

      {/* Mobile menu */}
      {open && (
        <div className="border-t border-slate-200 px-4 py-3 md:hidden">
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} className={linkClass} onClick={() => setOpen(false)}>
              {l.label}
            </NavLink>
          ))}
          <div className="mt-3 border-t border-slate-200 pt-3 text-sm text-slate-500">
            {user?.fullName} · <span className="font-semibold text-slate-700">{user?.role}</span>
          </div>
          <button onClick={handleLogout} className={`${btn.secondary} mt-3 w-full`}>
            Log out
          </button>
        </div>
      )}
    </header>
  )
}
