package rw.clinicbook.mongo;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MedicalDocumentRepository extends MongoRepository<MedicalDocument, String> {

    List<MedicalDocument> findByAppointmentIdOrderByUploadedAtDesc(Long appointmentId);
}
