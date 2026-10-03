const base =
  'inline-flex cursor-pointer items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50'

export const btn = {
  primary: `${base} bg-teal-600 text-white hover:bg-teal-700`,
  secondary: `${base} border border-slate-300 bg-white text-slate-700 hover:bg-slate-50`,
  danger: `${base} border border-red-200 bg-red-50 text-red-700 hover:bg-red-100`,
  google: `${base} w-full border border-slate-300 bg-white text-slate-700 hover:bg-slate-50`,
}

export const input =
  'w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:border-teal-500 focus:ring-2 focus:ring-teal-200 focus:outline-none'

export const label = 'mb-1 block text-sm font-medium text-slate-700'

export const card = 'rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:p-6'
