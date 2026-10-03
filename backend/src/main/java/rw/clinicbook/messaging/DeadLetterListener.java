package rw.clinicbook.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/** Messages that failed 3 times end up here; we record them as FAILED in MongoDB. */
@Component
public class DeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterListener.class);

    private final NotificationLogService logs;

    public DeadLetterListener(NotificationLogService logs) {
        this.logs = logs;
    }

    @RabbitListener(queues = RabbitConfig.DEAD_LETTER_QUEUE)
    public void onDeadLetter(NotificationEvent event,
                             @Header(name = "x-first-death-queue", required = false) String fromQueue) {
        String channel = RabbitConfig.SMS_QUEUE.equals(fromQueue) ? "SMS" : "EMAIL";
        String recipient = "SMS".equals(channel) ? event.phone() : event.email();
        log.warn("[DEAD LETTER] {} {} to {} failed after retries", channel, event.type(), recipient);
        logs.record(event, channel, recipient, "FAILED");
    }
}
