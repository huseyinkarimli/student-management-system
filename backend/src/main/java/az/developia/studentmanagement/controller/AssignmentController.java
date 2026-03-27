package az.developia.studentmanagement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import az.developia.studentmanagement.audit.AuditAction;
import az.developia.studentmanagement.audit.AuditLogService;
import az.developia.studentmanagement.dto.AssignmentRequestDto;
import az.developia.studentmanagement.dto.AssignmentResponseDto;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.exception.OurValidationException;
import az.developia.studentmanagement.repository.UserRepository;
import az.developia.studentmanagement.service.AssignmentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:3000"})
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserRepository userRepository;

    private Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getId();
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public List<AssignmentResponseDto> getAssignmentsByCourse(@PathVariable Long courseId) {
        logger.info("Fetching assignments for course: {}", courseId);
        return assignmentService.findByCourseId(courseId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public AssignmentResponseDto getAssignmentById(@PathVariable Long id) {
        logger.info("Fetching assignment with id: {}", id);
        AssignmentResponseDto assignment = assignmentService.findById(id);
        auditLogService.log(AuditAction.ASSIGNMENT_VIEWED, Map.of("assignmentId", id, "assignmentTitle", assignment.getTitle()), null);
        return assignment;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public AssignmentResponseDto createAssignment(@Valid @RequestBody AssignmentRequestDto request, BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }

        logger.info("Creating new assignment: {}", request.getTitle());
        AssignmentResponseDto created = assignmentService.create(request, getCurrentUserId());

        Map<String, Object> details = new HashMap<>();
        details.put("assignmentId", created.getId());
        details.put("title", created.getTitle());
        details.put("courseId", created.getCourseId());
        details.put("maxScore", created.getMaxScore());
        auditLogService.log(AuditAction.ASSIGNMENT_CREATED, details, null);

        return created;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public AssignmentResponseDto updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentRequestDto request, BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }

        logger.info("Updating assignment with id: {}", id);
        AssignmentResponseDto oldAssignment = assignmentService.findById(id);
        AssignmentResponseDto updated = assignmentService.update(id, request);

        Map<String, Object> details = new HashMap<>();
        details.put("assignmentId", id);
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("title", oldAssignment.getTitle());
        oldValues.put("maxScore", oldAssignment.getMaxScore());
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("title", updated.getTitle());
        newValues.put("maxScore", updated.getMaxScore());
        details.put("oldValues", oldValues);
        details.put("newValues", newValues);
        auditLogService.log(AuditAction.ASSIGNMENT_UPDATED, details, null);

        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public void deleteAssignment(@PathVariable Long id) {
        logger.info("Deleting assignment with id: {}", id);
        AssignmentResponseDto assignment = assignmentService.findById(id);
        assignmentService.delete(id);

        Map<String, Object> details = new HashMap<>();
        details.put("assignmentId", id);
        details.put("assignmentTitle", assignment.getTitle());
        details.put("courseId", assignment.getCourseId());
        auditLogService.log(AuditAction.ASSIGNMENT_DELETED, details, null);
    }
}
