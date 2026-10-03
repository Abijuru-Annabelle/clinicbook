import { Logo } from './ui'

// Split-screen on desktop, single column on mobile
export default function AuthShell({ title, subtitle, children }) {
  return (
    <div className="flex min-h-screen bg-slate-50">
      <aside className="hidden w-1/2 flex-col justify-between bg-teal-700 p-12 text-white lg:flex">
        <Logo light />
        <div>
          <h2 className="text-4xl leading-tight font-bold">Book your clinic visit in under 2 minutes.</h2>
          <p className="mt-4 max-w-md text-teal-100">
            No more queues. Choose a doctor, pick a free time slot, and get a confirmation by email and SMS.
          </p>
        </div>
        <p className="text-sm text-teal-200">© 2026 ClinicBook · AUCA Web Technology Project</p>
      </aside>

      <main className="flex w-full items-center justify-center px-4 py-10 sm:px-8 lg:w-1/2">
        <div className="w-full max-w-md">
          <div className="mb-8 flex justify-center lg:hidden">
            <Logo />
          </div>
          <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
          {subtitle && <p className="mt-1 mb-6 text-sm text-slate-500">{subtitle}</p>}
          {children}
        </div>
      </main>
    </div>
  )
}
