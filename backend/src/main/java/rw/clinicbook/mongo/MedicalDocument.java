package rw.clinicbook.mongo;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Metadata of an uploaded medical file (MongoDB collection "medical_documents").
 * The file bytes live in GridFS; gridFsId points to them.
 * appointmentId / patientId reference PostgreSQL rows (checked in the service layer).
 */
@Document("medical_documents")
@CompoundIndex(name = "patient_uploaded", def = "{'patientId': 1, 'uploadedAt': -1}")
public class MedicalDocument {

    @Id
    private String id;

    @Indexed
    private Long appointmentId;

    private Long patientId;
    private Long uploadedBy;
    private String uploaderName;
    private String fileName;
    private String contentType;
    private long size;
    private String gridFsId;
    private Instant uploadedAt;

    public String getId() { return id; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getGridFsId() { return gridFsId; }
    public void setGridFsId(String gridFsId) { this.gridFsId = gridFsId; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
