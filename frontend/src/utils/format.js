export const pad = (n) => String(n).padStart(2, '0')

export const toDateStr = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`

export const todayStr = () => toDateStr(new Date())

// Parses "YYYY-MM-DDTHH:mm[:ss]" as LOCAL time (matches Java LocalDateTime)
export const parseLocal = (s) => {
  const [date, time = '00:00'] = s.split('T')
  const [y, m, d] = date.split('-').map(Number)
  const [hh, mm] = time.split(':').map(Number)
  return new Date(y, m - 1, d, hh, mm)
}

export const addMinutes = (s, minutes) => {
  const d = parseLocal(s)
  d.setMinutes(d.getMinutes() + minutes)
  return `${toDateStr(d)}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export const fmtDate = (s) =>
  parseLocal(s).toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })

export const fmtTime = (s) => (s.split('T')[1] || '').slice(0, 5)

export const errMsg = (e) => e?.response?.data?.message || e?.message || 'Something went wrong'

export const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']

export const homeFor = (role) => (role === 'ADMIN' ? '/admin' : role === 'DOCTOR' ? '/doctor' : '/doctors')

export const initials = (name = '') =>
  name.replace(/^Dr\.?\s*/i, '').split(' ').filter(Boolean).slice(0, 2).map((w) => w[0].toUpperCase()).join('')

// Patients may change an appointment only if it is BOOKED and > 2 hours away
export const canChange = (a) => a.status === 'BOOKED' && parseLocal(a.startTime) - new Date() > 2 * 60 * 60 * 1000
