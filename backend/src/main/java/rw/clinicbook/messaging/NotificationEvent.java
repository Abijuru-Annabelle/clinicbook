package rw.clinicbook.messaging;

/**
 * Message sent through RabbitMQ. One event per recipient.
 * type is also the routing key, e.g. "appointment.booked", "user.registered".
 */
public record NotificationEvent(String type, Long userId, String fullName, String email, String phone,
                                String subject, String message) { }
