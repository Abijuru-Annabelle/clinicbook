import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { useAuth } from './context/auth'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import OAuthCallback from './pages/OAuthCallback'
import FindDoctor from './pages/FindDoctor'
import BookAppointment from './pages/BookAppointment'
import MyAppointments from './pages/MyAppointments'
import DoctorDashboard from './pages/DoctorDashboard'
import AdminDashboard from './pages/AdminDashboard'
import Unauthorized from './pages/Unauthorized'
import { homeFor } from './utils/format'

function AppLayout() {
  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main className="mx-auto max-w-6xl px-4 py-6 sm:py-8">
        <Outlet />
      </main>
    </div>
  )
}

function HomeRedirect() {
  const { user } = useAuth()
  return <Navigate to={user ? homeFor(user.role) : '/login'} replace />
}

const only = (roles, page) => <ProtectedRoute roles={roles}>{page}</ProtectedRoute>

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/oauth2/callback" element={<OAuthCallback />} />

          <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
            <Route path="/doctors" element={only(['PATIENT'], <FindDoctor />)} />
            <Route path="/book/:doctorId" element={only(['PATIENT'], <BookAppointment />)} />
            <Route path="/appointments" element={only(['PATIENT'], <MyAppointments />)} />
            <Route path="/doctor" element={only(['DOCTOR'], <DoctorDashboard />)} />
            <Route path="/admin" element={only(['ADMIN'], <AdminDashboard />)} />
            <Route path="/unauthorized" element={<Unauthorized />} />
          </Route>

          <Route path="*" element={<HomeRedirect />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
