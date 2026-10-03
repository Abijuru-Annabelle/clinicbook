package rw.clinicbook.web;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.clinicbook.dto.MedicalDocumentDto;
import rw.clinicbook.service.MedicalDocumentService;
import rw.clinicbook.service.MedicalDocumentService.StoredFile;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
public class MedicalDocumentController {

    private final MedicalDocumentService documents;

    public MedicalDocumentController(MedicalDocumentService documents) {
        this.documents = documents;
    }

    @GetMapping("/appointments/{appointmentId}/documents")
    public List<MedicalDocumentDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable Long appointmentId) {
        return documents.list(CurrentUser.id(jwt), appointmentId);
    }

    @PostMapping(value = "/appointments/{appointmentId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MedicalDocumentDto upload(@AuthenticationPrincipal Jwt jwt, @PathVariable Long appointmentId,
                                     @RequestParam("file") MultipartFile file) {
        return documents.upload(CurrentUser.id(jwt), appointmentId, file);
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        StoredFile file = documents.download(CurrentUser.id(jwt), id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .body(file.bytes());
    }

    @DeleteMapping("/documents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        documents.delete(CurrentUser.id(jwt), id);
    }
}
