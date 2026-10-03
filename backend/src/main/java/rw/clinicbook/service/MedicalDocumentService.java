package rw.clinicbook.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rw.clinicbook.domain.User;
import rw.clinicbook.dto.MedicalDocumentDto;
import rw.clinicbook.exception.ApiException;
import rw.clinicbook.mongo.MedicalDocument;
import rw.clinicbook.mongo.MedicalDocumentRepository;
import rw.clinicbook.repository.AppointmentRepository;
import rw.clinicbook.repository.UserRepository;

/**
 * Medical files for an appointment. Metadata -> MongoDB collection, bytes -> GridFS.
 * Only the appointment's patient or doctor can upload, list or download.
 */
@Service
public class MedicalDocumentService {

    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final MedicalDocumentRepository documents;
    private final GridFsTemplate gridFs;
    private final AppointmentRepository appointments;
    private final UserRepository users;

    public MedicalDocumentService(MedicalDocumentRepository documents, GridFsTemplate gridFs,
                                  AppointmentRepository appointments, UserRepository users) {
        this.documents = documents;
        this.gridFs = gridFs;
        this.appointments = appointments;
        this.users = users;
    }

    public List<MedicalDocumentDto> list(Long userId, Long appointmentId) {
        requireParticipant(userId, appointmentId);
        return documents.findByAppointmentIdOrderByUploadedAtDesc(appointmentId).stream().map(this::toDto).toList();
    }

    public MedicalDocumentDto upload(Long userId, Long appointmentId, MultipartFile file) {
        requireParticipant(userId, appointmentId);
        if (file == null || file.isEmpty()) throw ApiException.badRequest("Please choose a file");
        if (file.getSize() > MAX_BYTES) throw ApiException.badRequest("File is larger than 5 MB");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw ApiException.badRequest("Only PDF, JPG and PNG files are allowed");

        User uploader = users.findById(userId).orElseThrow(() -> ApiException.unauthorized("User not found"));
        String fileName = cleanName(file.getOriginalFilename());

        ObjectId gridFsId;
        try (InputStream in = file.getInputStream()) {
            gridFsId = gridFs.store(in, fileName, file.getContentType());
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the file");
        }

        MedicalDocument doc = new MedicalDocument();
        doc.setAppointmentId(appointmentId);
        doc.setPatientId(appointments.findPatientId(appointmentId).orElse(null));
        doc.setUploadedBy(userId);
        doc.setUploaderName(uploader.getFullName());
        doc.setFileName(fileName);
        doc.setContentType(file.getContentType());
        doc.setSize(file.getSize());
        doc.setGridFsId(gridFsId.toHexString());
        doc.setUploadedAt(Instant.now());
        return toDto(documents.save(doc));
    }

    /** Returns the stored file; the controller streams it to the browser. */
    public StoredFile download(Long userId, String documentId) {
        MedicalDocument doc = find(documentId);
        requireParticipant(userId, doc.getAppointmentId());
        GridFSFile file = gridFs.findOne(byId(doc.getGridFsId()));
        if (file == null) throw ApiException.notFound("File content not found");
        GridFsResource resource = gridFs.getResource(file);
        try (InputStream in = resource.getInputStream()) {
            return new StoredFile(doc.getFileName(), doc.getContentType(), in.readAllBytes());
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read the file");
        }
    }

    public void delete(Long userId, String documentId) {
        MedicalDocument doc = find(documentId);
        if (!doc.getUploadedBy().equals(userId)) throw ApiException.forbidden("Only the person who uploaded a file can delete it");
        gridFs.delete(byId(doc.getGridFsId()));
        documents.delete(doc);
    }

    public record StoredFile(String fileName, String contentType, byte[] bytes) { }

    private MedicalDocument find(String id) {
        return documents.findById(id).orElseThrow(() -> ApiException.notFound("Document not found"));
    }

    private void requireParticipant(Long userId, Long appointmentId) {
        if (!appointments.existsById(appointmentId)) throw ApiException.notFound("Appointment not found");
        if (!appointments.isParticipant(appointmentId, userId))
            throw ApiException.forbidden("You can only access files of your own appointments");
    }

    private static Query byId(String gridFsId) {
        return new Query(Criteria.where("_id").is(new ObjectId(gridFsId)));
    }

    private static String cleanName(String name) {
        if (name == null || name.isBlank()) return "document";
        String base = name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return base.length() > 120 ? base.substring(base.length() - 120) : base;
    }

    private MedicalDocumentDto toDto(MedicalDocument d) {
        return new MedicalDocumentDto(d.getId(), d.getAppointmentId(), d.getFileName(), d.getContentType(), d.getSize(),
                d.getUploadedBy(), d.getUploaderName(), d.getUploadedAt());
    }
}
