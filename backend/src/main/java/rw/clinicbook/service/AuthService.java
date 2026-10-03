package rw.clinicbook.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.clinicbook.domain.AuthProvider;
import rw.clinicbook.domain.Doctor;
import rw.clinicbook.domain.Patient;
import rw.clinicbook.domain.Role;
import rw.clinicbook.domain.User;
import rw.clinicbook.dto.AuthResponse;
import rw.clinicbook.dto.LoginRequest;
import rw.clinicbook.dto.RegisterRequest;
import rw.clinicbook.dto.UserDto;
import rw.clinicbook.exception.ApiException;
import rw.clinicbook.repository.DoctorRepository;
import rw.clinicbook.repository.PatientRepository;
import rw.clinicbook.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, PatientRepository patients, DoctorRepository doctors,
                       PasswordEncoder encoder, TokenService tokens) {
        this.users = users;
        this.patients = patients;
        this.doctors = doctors;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    /** Self-registration always creates a PATIENT. Doctors/admins are created by an admin. */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw ApiException.conflict("An account with this email already exists");

        User user = new User();
        user.setFullName(req.fullName().trim());
        user.setEmail(email);
        user.setPhone(req.phone() == null || req.phone().isBlank() ? null : req.phone().trim());
        user.setPasswordHash(encoder.encode(req.password()));
        user.setProvider(AuthProvider.LOCAL);
        user.setRole(Role.PATIENT);
        users.save(user);
        patients.save(new Patient(user));

        return new AuthResponse(tokens.issue(user), toDto(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> u.getPasswordHash() != null && encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!user.isEnabled()) throw ApiException.forbidden("This account is disabled");
        return new AuthResponse(tokens.issue(user), toDto(user));
    }

    /** Called after Google login. Existing accounts sign in; new emails get a PATIENT account. */
    @Transactional
    public String loginWithGoogle(String email, String name, boolean emailVerified) {
        if (email == null || !emailVerified) throw ApiException.unauthorized("Your Google email is not verified");
        User user = users.findByEmailIgnoreCase(email).orElseGet(() -> {
            User u = new User();
            u.setFullName(name == null || name.isBlank() ? email : name);
            u.setEmail(email.toLowerCase());
            u.setProvider(AuthProvider.GOOGLE);
            u.setRole(Role.PATIENT);
            users.save(u);
            patients.save(new Patient(u));
            return u;
        });
        if (!user.isEnabled()) throw ApiException.forbidden("This account is disabled");
        return tokens.issue(user);
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        return toDto(users.findById(userId).orElseThrow(() -> ApiException.unauthorized("User not found")));
    }

    UserDto toDto(User u) {
        Long patientId = patients.findByUserId(u.getId()).map(Patient::getId).orElse(null);
        Long doctorId = doctors.findByUserId(u.getId()).map(Doctor::getId).orElse(null);
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(), patientId, doctorId);
    }
}
