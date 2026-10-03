package rw.clinicbook.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer: reads notifications.sms (appointment events only).
 * SMS gateways (e.g. Africa's Talking, Twilio) need a paid account, so the SMS is simulated:
 * it is logged and saved to MongoDB. A real gateway call would replace the log line.
 */
@Component
public class SmsNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationListener.class);
    private static final int SMS_LENGTH = 160;

    private final NotificationLogService logs;

    public SmsNotificationListener(NotificationLogService logs) {
        this.logs = logs;
    }

    @RabbitListener(queues = RabbitConfig.SMS_QUEUE)
    public void onEvent(NotificationEvent event) {
        if (event.phone() == null || event.phone().isBlank()) return;
        String text = event.message().length() > SMS_LENGTH ? event.message().substring(0, SMS_LENGTH - 3) + "..." : event.message();
        log.info("[SMS simulated] to={} | {}", event.phone(), text);
        logs.record(event, "SMS", event.phone(), "SIMULATED");
    }
}
