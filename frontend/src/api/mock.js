// Demo data so the UI works before the Spring Boot backend exists.
// Switch off with VITE_USE_MOCK=false in .env
import { addMinutes, parseLocal, toDateStr, canChange } from '../utils/format'

const wait = (value, ms = 250) => new Promise((resolve) => setTimeout(() => resolve(structuredClone(value)), ms))
const fail = (message, status = 400) =>
  new Promise((_, reject) => setTimeout(() => reject({ response: { status, data: { message } } }), 250))

const currentUser = () => JSON.parse(localStorage.getItem('user') || 'null')
const dayOffset = (n, time) => {
  const d = new Date()
  d.setDate(d.getDate() + n)
  return `${toDateStr(d)}T${time}`
}

let nextId = 1000

let departments = [
  { id: 1, name: 'General Medicine', description: 'Consultations for common illnesses' },
  { id: 2, name: 'Pediatrics', description: 'Care for children and infants' },
  { id: 3, name: 'Dentistry', description: 'Oral and dental care' },
  { id: 4, name: 'Gynecology', description: "Women's health" },
]

let doctors = [
  { id: 1, fullName: 'Dr. Eric Mugisha', email: 'doctor@clinicbook.rw', specialization: 'General Practitioner', licenseNumber: 'RMDC-1001', departmentId: 1, slotMinutes: 30 },
  { id: 2, fullName: 'Dr. Aline Uwase', email: 'aline@clinicbook.rw', specialization: 'Pediatrician', licenseNumber: 'RMDC-1002', departmentId: 2, slotMinutes: 30 },
  { id: 3, fullName: 'Dr. Jean Habimana', email: 'jean@clinicbook.rw', specialization: 'Dentist', licenseNumber: 'RMDC-1003', departmentId: 3, slotMinutes: 45 },
  { id: 4, fullName: 'Dr. Grace Mukamana', email: 'grace@clinicbook.rw', specialization: 'Gynecologist', licenseNumber: 'RMDC-1004', departmentId: 4, slotMinutes: 30 },
]

let availability = []
doctors.forEach((d) => {
  for (let day = 1; day <= 5; day++) {
    availability.push({ id: nextId++, doctorId: d.id, dayOfWeek: day, startTime: '08:00', endTime: '12:00' })
    availability.push({ id: nextId++, doctorId: d.id, dayOfWeek: day, startTime: '14:00', endTime: '17:00' })
  }
  availability.push({ id: nextId++, doctorId: d.id, dayOfWeek: 6, startTime: '09:00', endTime: '12:00' })
})

let appointments = [
  { id: 1, patientId: 1, patientName: 'Demo Patient', doctorId: 1, startTime: dayOffset(1, '09:00'), status: 'BOOKED', reason: 'Headache and fever', notes: '' },
  { id: 2, patientId: 1, patientName: 'Demo Patient', doctorId: 3, startTime: dayOffset(3, '10:30'), status: 'BOOKED', reason: 'Tooth pain', notes: '' },
  { id: 3, patientId: 1, patientName: 'Demo Patient', doctorId: 2, startTime: dayOffset(-6, '14:00'), status: 'COMPLETED', reason: 'Child vaccination', notes: 'Next dose in 4 weeks' },
  { id: 4, patientId: 1, patientName: 'Demo Patient', doctorId: 1, startTime: dayOffset(-12, '08:30'), status: 'CANCELLED', reason: 'Check-up', notes: '' },
  { id: 5, patientId: 2, patientName: 'Jean Claude N.', doctorId: 1, startTime: dayOffset(0, '08:00'), status: 'COMPLETED', reason: 'Back pain', notes: 'Prescribed rest' },
  { id: 6, patientId: 3, patientName: 'Diane U.', doctorId: 1, startTime: dayOffset(0, '10:00'), status: 'BOOKED', reason: 'Blood pressure follow-up', notes: '' },
  { id: 7, patientId: 4, patientName: 'Patrick M.', doctorId: 1, startTime: dayOffset(0, '15:30'), status: 'BOOKED', reason: 'Cough for 2 weeks', notes: '' },
]

const deptName = (id) => departments.find((x) => x.id === id)?.name || '-'
const withDept = (d) => ({ ...d, departmentName: deptName(d.departmentId) })
const enrich = (a) => {
  const doc = doctors.find((d) => d.id === a.doctorId)
  return {
    ...a,
    doctorName: doc?.fullName || '-',
    departmentName: deptName(doc?.departmentId),
    endTime: addMinutes(a.startTime, doc?.slotMinutes || 30),
  }
}
const isTaken = (doctorId, startTime, ignoreId) =>
  appointments.some((x) => x.id !== ignoreId && x.doctorId === doctorId && x.startTime === startTime && x.status !== 'CANCELLED')

// ---------- auth ----------
export function login(email) {
  const e = email.trim().toLowerCase()
  let user
  if (e.startsWith('admin')) user = { id: 99, fullName: 'Clinic Admin', email: e, role: 'ADMIN' }
  else if (e.startsWith('doctor')) user = { id: 50, fullName: 'Dr. Eric Mugisha', email: e, role: 'DOCTOR', doctorId: 1 }
  else user = { id: 1, fullName: 'Demo Patient', email: e, role: 'PATIENT', patientId: 1 }
  return wait({ token: 'mock-jwt-token', user })
}

export function register(body) {
  return wait({ token: 'mock-jwt-token', user: { id: nextId++, fullName: body.fullName, email: body.email, role: 'PATIENT', patientId: 1 } })
}

export const me = () => wait(currentUser())

// ---------- departments ----------
export const listDepartments = () => wait(departments)

export function createDepartment({ name, description }) {
  if (departments.some((d) => d.name.toLowerCase() === name.trim().toLowerCase())) return fail('Department already exists', 409)
  const dept = { id: nextId++, name: name.trim(), description }
  departments.push(dept)
  return wait(dept)
}

export function deleteDepartment(id) {
  if (doctors.some((d) => d.departmentId === id)) return fail('Cannot delete a department that still has doctors', 409)
  departments = departments.filter((d) => d.id !== id)
  return wait(null)
}

// ---------- doctors ----------
export function listDoctors({ search = '', departmentId = '' } = {}) {
  const q = search.trim().toLowerCase()
  return wait(
    doctors
      .filter((d) => !departmentId || d.departmentId === Number(departmentId))
      .filter((d) => !q || d.fullName.toLowerCase().includes(q) || d.specialization.toLowerCase().includes(q))
      .map(withDept),
  )
}

export function getDoctor(id) {
  const d = doctors.find((x) => x.id === Number(id))
  return d ? wait(withDept(d)) : fail('Doctor not found', 404)
}

export function createDoctor(body) {
  if (doctors.some((d) => d.licenseNumber === body.licenseNumber)) return fail('License number already registered', 409)
  if (doctors.some((d) => d.email === body.email)) return fail('Email already in use', 409)
  const doc = { ...body, id: nextId++, departmentId: Number(body.departmentId), slotMinutes: Number(body.slotMinutes) || 30 }
  doctors.push(doc)
  return wait(withDept(doc))
}

export function deleteDoctor(id) {
  if (appointments.some((a) => a.doctorId === id && a.status === 'BOOKED')) return fail('Doctor has upcoming appointments', 409)
  doctors = doctors.filter((d) => d.id !== id)
  return wait(null)
}

export function getSlots(doctorId, date) {
  const doc = doctors.find((d) => d.id === Number(doctorId))
  if (!doc) return fail('Doctor not found', 404)
  const [y, m, dd] = date.split('-').map(Number)
  const js = new Date(y, m - 1, dd).getDay()
  const dow = js === 0 ? 7 : js
  const now = new Date()
  const slots = []
  availability
    .filter((a) => a.doctorId === doc.id && a.dayOfWeek === dow)
    .sort((a, b) => a.startTime.localeCompare(b.startTime))
    .forEach((a) => {
      let t = `${date}T${a.startTime}`
      const end = `${date}T${a.endTime}`
      while (addMinutes(t, doc.slotMinutes) <= end) {
        slots.push({ startTime: t, available: !isTaken(doc.id, t) && parseLocal(t) > now })
        t = addMinutes(t, doc.slotMinutes)
      }
    })
  return wait(slots)
}

// ---------- appointments ----------
export function book({ doctorId, startTime, reason }) {
  const doc = doctors.find((d) => d.id === Number(doctorId))
  if (!doc) return fail('Doctor not found', 404)
  if (isTaken(doc.id, startTime)) return fail('This slot was just taken. Please pick another one.', 409)
  const user = currentUser()
  const appt = { id: nextId++, patientId: user?.patientId ?? 1, patientName: user?.fullName, doctorId: doc.id, startTime, status: 'BOOKED', reason, notes: '' }
  appointments.push(appt)
  return wait(enrich(appt))
}

export function myAppointments() {
  const pid = currentUser()?.patientId ?? 1
  return wait(appointments.filter((a) => a.patientId === pid).map(enrich).sort((a, b) => b.startTime.localeCompare(a.startTime)))
}

export function cancel(id) {
  const a = appointments.find((x) => x.id === Number(id))
  if (!a) return fail('Appointment not found', 404)
  if (!canChange(a)) return fail('Appointments can only be cancelled more than 2 hours before they start.')
  a.status = 'CANCELLED'
  return wait(enrich(a))
}

export function reschedule(id, startTime) {
  const a = appointments.find((x) => x.id === Number(id))
  if (!a) return fail('Appointment not found', 404)
  if (!canChange(a)) return fail('Appointments can only be rescheduled more than 2 hours before they start.')
  if (isTaken(a.doctorId, startTime, a.id)) return fail('This slot was just taken. Please pick another one.', 409)
  a.startTime = startTime
  return wait(enrich(a))
}

export function updateStatus(id, status, notes) {
  const a = appointments.find((x) => x.id === Number(id))
  if (!a) return fail('Appointment not found', 404)
  a.status = status
  if (notes) a.notes = notes
  return wait(enrich(a))
}

// ---------- doctor self-service ----------
export function doctorSchedule(date) {
  const did = currentUser()?.doctorId ?? 1
  return wait(
    appointments
      .filter((a) => a.doctorId === did && a.startTime.startsWith(date))
      .map(enrich)
      .sort((a, b) => a.startTime.localeCompare(b.startTime)),
  )
}

export function myAvailability() {
  const did = currentUser()?.doctorId ?? 1
  return wait(
    availability
      .filter((a) => a.doctorId === did)
      .sort((a, b) => a.dayOfWeek - b.dayOfWeek || a.startTime.localeCompare(b.startTime)),
  )
}

export function addAvailability({ dayOfWeek, startTime, endTime }) {
  const did = currentUser()?.doctorId ?? 1
  const day = Number(dayOfWeek)
  if (endTime <= startTime) return fail('End time must be after start time')
  const overlaps = availability.some(
    (a) => a.doctorId === did && a.dayOfWeek === day && startTime < a.endTime && endTime > a.startTime,
  )
  if (overlaps) return fail('This time overlaps with existing availability', 409)
  const row = { id: nextId++, doctorId: did, dayOfWeek: day, startTime, endTime }
  availability.push(row)
  return wait(row)
}

export function deleteAvailability(id) {
  availability = availability.filter((a) => a.id !== id)
  return wait(null)
}
