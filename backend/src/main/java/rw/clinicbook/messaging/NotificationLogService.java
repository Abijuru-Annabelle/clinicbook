package rw.clinicbook.messaging;

import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;
import rw.clinicbook.mongo.NotificationLog;
import rw.clinicbook.mongo.NotificationLogRepository;

/** Writes every delivery attempt to MongoDB (collection notification_logs). */
@Service
public class NotificationLogService {

    private final NotificationLogRepository logs;

    public NotificationLogService(NotificationLogRepository logs) {
        this.logs = logs;
    }

    public void record(NotificationEvent e, String channel, String recipient, String status) {
        NotificationLog log = new NotificationLog();
        log.setUserId(e.userId());
        log.setChannel(channel);
        log.setType(e.type());
        log.setRecipient(recipient);
        log.setMessage(e.message());
        log.setStatus(status);
        log.setAttempts(1);
        log.setPayload(Map.of("subject", e.subject() == null ? "" : e.subject()));
        log.setCreatedAt(Instant.now());
        logs.save(log);
    }
}
