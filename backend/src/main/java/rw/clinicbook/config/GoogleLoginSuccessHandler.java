package rw.clinicbook.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import rw.clinicbook.exception.ApiException;
import rw.clinicbook.service.AuthService;

/**
 * After Google confirms the user's identity:
 *  1. find or create the ClinicBook account (new Google users become PATIENTs),
 *  2. issue our own JWT,
 *  3. redirect to the React app: /oauth2/callback?token=...
 */
@Component
public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService auth;
    private final String frontendUrl;

    public GoogleLoginSuccessHandler(AuthService auth, @Value("${app.frontend-url}") String frontendUrl) {
        this.auth = auth;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User google = (OAuth2User) authentication.getPrincipal();
        String email = google.getAttribute("email");
        String name = google.getAttribute("name");
        Boolean verified = google.getAttribute("email_verified");

        // The session was only needed for the Google handshake; the app itself is stateless (JWT).
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        try {
            String token = auth.loginWithGoogle(email, name, Boolean.TRUE.equals(verified));
            response.sendRedirect(frontendUrl + "/oauth2/callback?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8));
        } catch (ApiException e) {
            response.sendRedirect(frontendUrl + "/login?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
