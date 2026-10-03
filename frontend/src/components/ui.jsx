export function Spinner() {
  return (
    <div className="flex justify-center py-10">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-teal-200 border-t-teal-600" />
    </div>
  )
}

const ALERT_STYLES = {
  error: 'border-red-200 bg-red-50 text-red-700',
  success: 'border-green-200 bg-green-50 text-green-700',
  info: 'border-sky-200 bg-sky-50 text-sky-800',
}

export function Alert({ type = 'error', children }) {
  if (!children) return null
  return (
    <div role="alert" className={`rounded-lg border px-4 py-3 text-sm ${ALERT_STYLES[type]}`}>
      {children}
    </div>
  )
}

const STATUS_STYLES = {
  BOOKED: 'bg-sky-100 text-sky-800',
  COMPLETED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-slate-200 text-slate-700',
  NO_SHOW: 'bg-amber-100 text-amber-800',
}

export function StatusBadge({ status }) {
  return (
    <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-semibold ${STATUS_STYLES[status] || ''}`}>
      {status.replace('_', ' ')}
    </span>
  )
}

export function EmptyState({ title, text }) {
  return (
    <div className="rounded-xl border border-dashed border-slate-300 bg-white px-6 py-10 text-center">
      <p className="font-medium text-slate-700">{title}</p>
      {text && <p className="mt-1 text-sm text-slate-500">{text}</p>}
    </div>
  )
}

export function PageHeader({ title, subtitle, children }) {
  return (
    <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-slate-500">{subtitle}</p>}
      </div>
      {children}
    </div>
  )
}

export function Logo({ light = false }) {
  return (
    <span className={`flex items-center gap-2 text-lg font-bold ${light ? 'text-white' : 'text-teal-700'}`}>
      <span className={`flex h-8 w-8 items-center justify-center rounded-lg text-xl ${light ? 'bg-white text-teal-700' : 'bg-teal-600 text-white'}`}>
        +
      </span>
      ClinicBook
    </span>
  )
}
