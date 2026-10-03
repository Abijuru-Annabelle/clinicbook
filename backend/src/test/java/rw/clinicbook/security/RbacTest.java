package rw.clinicbook.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import rw.clinicbook.config.GoogleLoginSuccessHandler;
import rw.clinicbook.config.SecurityConfig;
import rw.clinicbook.service.AppointmentService;
import rw.clinicbook.service.AvailabilityService;
import rw.clinicbook.service.DepartmentService;
import rw.clinicbook.service.DoctorService;
import rw.clinicbook.web.AppointmentController;
import rw.clinicbook.web.DepartmentController;
import rw.clinicbook.web.DoctorController;
import rw.clinicbook.web.DoctorSelfController;

/**
 * Role-Based Access Control tests.
 * Only the web + security layers are loaded; services are mocked, so no database is needed.
 */
@WebMvcTest(controllers = {AppointmentController.class, DepartmentController.class,
        DoctorController.class, DoctorSelfController.class})
@Import(SecurityConfig.class)
class RbacTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean private AppointmentService appointmentService;
    @MockitoBean private AvailabilityService availabilityService;
    @MockitoBean private DepartmentService departmentService;
    @MockitoBean private DoctorService doctorService;
    @MockitoBean private GoogleLoginSuccessHandler googleLoginSuccessHandler;

    private static RequestPostProcessor as(String role) {
        return jwt().jwt(j -> j.subject("1")).authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    @DisplayName("No token -> 401 Unauthorized")
    void anonymousIsRejected() throws Exception {
        mvc.perform(get("/api/appointments/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/departments")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Any logged-in role can read departments and doctors")
    void everyRoleCanBrowse() throws Exception {
        for (String role : new String[] {"PATIENT", "DOCTOR", "ADMIN"}) {
            mvc.perform(get("/api/departments").with(as(role))).andExpect(status().isOk());
            mvc.perform(get("/api/doctors").with(as(role))).andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Only PATIENT can view and book their appointments")
    void patientEndpoints() throws Exception {
        mvc.perform(get("/api/appointments/me").with(as("PATIENT"))).andExpect(status().isOk());
        mvc.perform(get("/api/appointments/me").with(as("DOCTOR"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/appointments/me").with(as("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/appointments").with(as("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorId\":1,\"startTime\":\"2099-01-05T09:00:00\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Only DOCTOR can manage their schedule and availability")
    void doctorEndpoints() throws Exception {
        mvc.perform(get("/api/doctor/availability").with(as("DOCTOR"))).andExpect(status().isOk());
        mvc.perform(get("/api/doctor/availability").with(as("PATIENT"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/doctor/appointments?date=2099-01-05").with(as("ADMIN"))).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Only ADMIN can create departments and doctors")
    void adminEndpoints() throws Exception {
        String dept = "{\"name\":\"Cardiology\",\"description\":\"Heart care\"}";
        mvc.perform(post("/api/departments").with(as("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(dept)).andExpect(status().isCreated());
        mvc.perform(post("/api/departments").with(as("PATIENT"))
                .contentType(MediaType.APPLICATION_JSON).content(dept)).andExpect(status().isForbidden());
        mvc.perform(post("/api/departments").with(as("DOCTOR"))
                .contentType(MediaType.APPLICATION_JSON).content(dept)).andExpect(status().isForbidden());
    }
}
