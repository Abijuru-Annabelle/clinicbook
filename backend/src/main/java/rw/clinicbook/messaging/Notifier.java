package rw.clinicbook.messaging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import rw.clinicbook.domain.User;

/**
 * Services call this to announce something happened.
 * The event is only sent to RabbitMQ after the database transaction commits (see RabbitEventRelay),
 * so users are never notified about a booking that was rolled back.
 */
@Component
public class Notifier {

    private static final DateTimeFormatter WHEN =
            DateTimeFormatter.ofPattern("EEE d MMM yyyy 'at' HH:mm", Locale.ENGLISH);

    private final ApplicationEventPublisher events;

    public Notifier(ApplicationEventPublisher events) {
        this.events = events;
    }

    public void send(String type, User to, String subject, String message) {
        events.publishEvent(new NotificationEvent(type, to.getId(), to.getFullName(), to.getEmail(), to.getPhone(),
                subject, message));
    }

    public static String when(LocalDateTime time) {
        return time.format(WHEN);
    }
}
