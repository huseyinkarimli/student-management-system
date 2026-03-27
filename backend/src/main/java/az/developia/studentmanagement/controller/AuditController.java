package az.developia.studentmanagement.controller;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import az.developia.studentmanagement.audit.AuditAction;
import az.developia.studentmanagement.audit.AuditLogDocument;
import az.developia.studentmanagement.audit.AuditLogRepository;
import az.developia.studentmanagement.audit.AuditLogResponse;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "timestamp");

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getLogsByStudent(@PathVariable Long studentId) {
        List<AuditLogDocument> docs = auditLogRepository.findByAffectedStudentId(studentId, DEFAULT_SORT);
        return ResponseEntity.ok(toResponseList(docs));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getLogsByUser(@PathVariable Long userId) {
        List<AuditLogDocument> docs = auditLogRepository.findByUserId(userId, DEFAULT_SORT);
        return ResponseEntity.ok(toResponseList(docs));
    }

    @GetMapping("/range")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) Instant end) {
        List<AuditLogDocument> docs = auditLogRepository.findByTimestampBetween(start, end, DEFAULT_SORT);
        return ResponseEntity.ok(toResponseList(docs));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getLogsByAction(@PathVariable AuditAction action) {
        List<AuditLogDocument> docs = auditLogRepository.findByAction(action, DEFAULT_SORT);
        return ResponseEntity.ok(toResponseList(docs));
    }

    private List<AuditLogResponse> toResponseList(List<AuditLogDocument> docs) {
        return docs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLogDocument doc) {
        return AuditLogResponse.builder()
                .id(doc.getId())
                .timestamp(doc.getTimestamp())
                .userId(doc.getUserId())
                .username(doc.getUsername())
                .action(doc.getAction())
                .details(doc.getDetails())
                .ipAddress(doc.getIpAddress())
                .affectedStudentId(doc.getAffectedStudentId())
                .build();
    }
}
