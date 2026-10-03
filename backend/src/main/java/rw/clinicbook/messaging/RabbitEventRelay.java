package rw.clinicbook.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Producer: publishes events to the RabbitMQ topic exchange after the DB commit. */
@Component
public class RabbitEventRelay {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventRelay.class);

    private final RabbitTemplate rabbit;

    public RabbitEventRelay(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publish(NotificationEvent event) {
        try {
            rabbit.convertAndSend(RabbitConfig.EXCHANGE, event.type(), event);
            log.info("Published {} for user {}", event.type(), event.userId());
        } catch (AmqpException e) {
            // The booking itself must not fail just because the broker is down
            log.error("RabbitMQ unavailable, {} not queued: {}", event.type(), e.getMessage());
        }
    }
}
