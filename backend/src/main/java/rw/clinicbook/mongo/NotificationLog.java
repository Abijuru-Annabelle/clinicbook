package rw.clinicbook.mongo;

import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * One email/SMS sent (or attempted) to a user (MongoDB collection "notification_logs").
 * High-volume, flexible payload, auto-deleted after 90 days (TTL index).
 */
@Document("notification_logs")
@CompoundIndex(name = "user_created", def = "{'userId': 1, 'createdAt': -1}")
public class NotificationLog {

    @Id
    private String id;

    private Long userId;
    private String channel;    // EMAIL | SMS
    private String type;       // APPOINTMENT_BOOKED, APPOINTMENT_CANCELLED, ...
    private String recipient;
    private String message;
    private String status;     // SENT | FAILED
    private int attempts;
    private Map<String, Object> payload;

    @Indexed(expireAfter = "90d")
    private Instant createdAt;

    public String getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
