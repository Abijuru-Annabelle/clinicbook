package rw.clinicbook.web;

import org.springframework.security.oauth2.jwt.Jwt;

/** The JWT subject holds the user id. */
final class CurrentUser {

    private CurrentUser() { }

    static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
