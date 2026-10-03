import axios from 'axios'

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const client = axios.create({ baseURL: `${API_URL}/api`, timeout: 10000 })

// Attach the JWT to every request
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// If the token is expired/invalid, log out and go to login
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthCall = error.config?.url?.startsWith('/auth/')
    if (error.response?.status === 401 && !isAuthCall) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default client
