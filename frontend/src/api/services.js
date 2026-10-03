import client, { API_URL } from './client'
import * as mock from './mock'

export const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'
export const googleLoginUrl = `${API_URL}/oauth2/authorization/google`

const data = (promise) => promise.then((res) => res.data)
const clean = (params) => Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))

export const authApi = {
  login: (email, password) => (USE_MOCK ? mock.login(email) : data(client.post('/auth/login', { email, password }))),
  register: (body) => (USE_MOCK ? mock.register(body) : data(client.post('/auth/register', body))),
  me: () => (USE_MOCK ? mock.me() : data(client.get('/auth/me'))),
}

export const departmentApi = {
  list: () => (USE_MOCK ? mock.listDepartments() : data(client.get('/departments'))),
  create: (body) => (USE_MOCK ? mock.createDepartment(body) : data(client.post('/departments', body))),
  remove: (id) => (USE_MOCK ? mock.deleteDepartment(id) : data(client.delete(`/departments/${id}`))),
}

export const doctorApi = {
  list: (params = {}) => (USE_MOCK ? mock.listDoctors(params) : data(client.get('/doctors', { params: clean(params) }))),
  get: (id) => (USE_MOCK ? mock.getDoctor(id) : data(client.get(`/doctors/${id}`))),
  slots: (id, date) => (USE_MOCK ? mock.getSlots(id, date) : data(client.get(`/doctors/${id}/slots`, { params: { date } }))),
  create: (body) => (USE_MOCK ? mock.createDoctor(body) : data(client.post('/doctors', body))),
  remove: (id) => (USE_MOCK ? mock.deleteDoctor(id) : data(client.delete(`/doctors/${id}`))),
}

export const appointmentApi = {
  book: (body) => (USE_MOCK ? mock.book(body) : data(client.post('/appointments', body))),
  mine: () => (USE_MOCK ? mock.myAppointments() : data(client.get('/appointments/me'))),
  cancel: (id) => (USE_MOCK ? mock.cancel(id) : data(client.patch(`/appointments/${id}/cancel`))),
  reschedule: (id, startTime) =>
    USE_MOCK ? mock.reschedule(id, startTime) : data(client.patch(`/appointments/${id}/reschedule`, { startTime })),
  updateStatus: (id, status, notes) =>
    USE_MOCK ? mock.updateStatus(id, status, notes) : data(client.patch(`/appointments/${id}/status`, { status, notes })),
}

export const doctorSelfApi = {
  schedule: (date) => (USE_MOCK ? mock.doctorSchedule(date) : data(client.get('/doctor/appointments', { params: { date } }))),
  availability: () => (USE_MOCK ? mock.myAvailability() : data(client.get('/doctor/availability'))),
  addAvailability: (body) => (USE_MOCK ? mock.addAvailability(body) : data(client.post('/doctor/availability', body))),
  removeAvailability: (id) =>
    USE_MOCK ? mock.deleteAvailability(id) : data(client.delete(`/doctor/availability/${id}`)),
}

export const documentApi = {
  list: (appointmentId) =>
    USE_MOCK ? mock.listDocuments(appointmentId) : data(client.get(`/appointments/${appointmentId}/documents`)),
  upload: (appointmentId, file) => {
    if (USE_MOCK) return mock.uploadDocument(appointmentId, file)
    const form = new FormData()
    form.append('file', file)
    return data(client.post(`/appointments/${appointmentId}/documents`, form))
  },
  download: (id) =>
    USE_MOCK ? mock.downloadDocument() : data(client.get(`/documents/${id}`, { responseType: 'blob', timeout: 30000 })),
  remove: (id) => (USE_MOCK ? mock.deleteDocument(id) : data(client.delete(`/documents/${id}`))),
}

export const notificationApi = {
  mine: () => (USE_MOCK ? mock.myNotifications() : data(client.get('/notifications/me'))),
}
