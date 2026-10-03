package rw.clinicbook.messaging;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.clinicbook.domain.Appointment;
import rw.clinicbook.repository.AppointmentRepository;

/** Every 15 minutes: send a reminder for appointments starting in about 24 hours. */
@Component
public class ReminderScheduler {

    private static final long WINDOW_MINUTES = 15;

    private final AppointmentRepository appointments;
    private final Notifier notifier;

    public ReminderScheduler(AppointmentRepository appointments, Notifier notifier) {
        this.appointments = appointments;
        this.notifier = notifier;
    }

    @Scheduled(fixedRate = WINDOW_MINUTES, timeUnit = TimeUnit.MINUTES)
    @Transactional(readOnly = true)
    public void sendReminders() {
        LocalDateTime from = LocalDateTime.now().plusHours(24);
        LocalDateTime to = from.plusMinutes(WINDOW_MINUTES);
        for (Appointment a : appointments.findBookedStartingBetween(from, to)) {
            notifier.send("appointment.reminder", a.getPatient().getUser(), "Appointment reminder",
                    "Reminder: you have an appointment with " + a.getDoctor().getUser().getFullName()
                            + " on " + Notifier.when(a.getStartTime()) + ".");
        }
    }
}
