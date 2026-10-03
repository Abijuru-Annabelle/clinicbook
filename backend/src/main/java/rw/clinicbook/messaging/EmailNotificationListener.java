package rw.clinicbook.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Consumer: reads notifications.email and sends the email.
 * If no SMTP server is configured, the email is simulated (logged) so the flow still works locally.
 * An exception here makes RabbitMQ retry; after 3 failures the message is dead-lettered.
 */
@Component
public class EmailNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationListener.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final NotificationLogService logs;
    private final String from;

    public EmailNotificationListener(ObjectProvider<JavaMailSender> mailSender, NotificationLogService logs,
                                     @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.logs = logs;
        this.from = from;
    }

    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void onEvent(NotificationEvent event) {
        if (event.email() == null || event.email().isBlank()) return;

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("[EMAIL simulated] to={} | {} | {}", event.email(), event.subject(), event.message());
            logs.record(event, "EMAIL", event.email(), "SIMULATED");
            return;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(event.email());
        mail.setSubject("ClinicBook - " + event.subject());
        mail.setText("Hello " + event.fullName() + ",\n\n" + event.message() + "\n\nClinicBook");
        sender.send(mail);
        log.info("[EMAIL sent] to={} | {}", event.email(), event.subject());
        logs.record(event, "EMAIL", event.email(), "SENT");
    }
}
