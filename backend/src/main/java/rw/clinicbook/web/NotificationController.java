package rw.clinicbook.web;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.clinicbook.dto.NotificationDto;
import rw.clinicbook.mongo.NotificationLogRepository;

/** The logged-in user's latest email/SMS notifications (read from MongoDB). */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationLogRepository logs;

    public NotificationController(NotificationLogRepository logs) {
        this.logs = logs;
    }

    @GetMapping("/me")
    public List<NotificationDto> mine(@AuthenticationPrincipal Jwt jwt) {
        return logs.findTop20ByUserIdOrderByCreatedAtDesc(CurrentUser.id(jwt)).stream()
                .map(n -> new NotificationDto(n.getId(), n.getChannel(), n.getType(), n.getMessage(), n.getStatus(), n.getCreatedAt()))
                .toList();
    }
}
