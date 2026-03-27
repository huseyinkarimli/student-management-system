package az.developia.studentmanagement.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import az.developia.studentmanagement.audit.AuditAction;
import az.developia.studentmanagement.audit.AuditLogService;
import az.developia.studentmanagement.dto.SubmissionRequestDto;
import az.developia.studentmanagement.dto.SubmissionResponseDto;
import az.developia.studentmanagement.entity.AssignmentEntity;
import az.developia.studentmanagement.entity.CourseEntity;
import az.developia.studentmanagement.entity.StudentEntity;
import az.developia.studentmanagement.entity.SubmissionEntity;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.exception.AssignmentNotFoundException;
import az.developia.studentmanagement.exception.CourseNotFoundException;
import az.developia.studentmanagement.exception.DuplicateSubmissionException;
import az.developia.studentmanagement.exception.StudentNotFoundException;
import az.developia.studentmanagement.exception.SubmissionNotFoundException;
import az.developia.studentmanagement.repository.AssignmentRepository;
import az.developia.studentmanagement.repository.CourseRepository;
import az.developia.studentmanagement.repository.EnrollmentRepository;
import az.developia.studentmanagement.repository.StudentRepository;
import az.developia.studentmanagement.repository.SubmissionRepository;
import az.developia.studentmanagement.repository.UserRepository;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<SubmissionResponseDto> findByAssignmentId(Long assignmentId) {
        AssignmentEntity assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        verifyTeacherAccessToCourse(assignment.getCourse().getId());
        
        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<SubmissionResponseDto> findByStudentId(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public SubmissionResponseDto submit(SubmissionRequestDto request) {
        AssignmentEntity assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new AssignmentNotFoundException(request.getAssignmentId()));

        StudentEntity student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException(request.getStudentId()));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), assignment.getCourse().getId())) {
            throw new IllegalStateException("Student is not enrolled in this course");
        }

        if (submissionRepository.existsByAssignmentIdAndStudentId(request.getAssignmentId(), request.getStudentId())) {
            throw new DuplicateSubmissionException(request.getAssignmentId(), request.getStudentId());
        }

        SubmissionEntity submission = new SubmissionEntity();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFileUrl(request.getFileUrl());
        submission.setStatus(SubmissionEntity.SubmissionStatus.SUBMITTED);

        SubmissionEntity saved = submissionRepository.save(submission);

        Map<String, Object> details = new HashMap<>();
        details.put("assignmentId", assignment.getId());
        details.put("assignmentTitle", assignment.getTitle());
        details.put("studentId", student.getId());
        details.put("studentName", student.getName() + " " + student.getSurname());
        details.put("courseId", assignment.getCourse().getId());
        auditLogService.log(AuditAction.SUBMISSION_CREATED, details, student.getId());

        return mapToResponseDto(saved);
    }

    public SubmissionResponseDto gradeSubmission(Long submissionId, Integer score, String feedback) {
        SubmissionEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException(submissionId));

        verifyTeacherAccessToCourse(submission.getAssignment().getCourse().getId());

        if (score != null && score > submission.getAssignment().getMaxScore()) {
            throw new IllegalArgumentException("Score cannot exceed max score of " + submission.getAssignment().getMaxScore());
        }

        if (score != null && score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        Integer oldScore = submission.getScore();
        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setStatus(SubmissionEntity.SubmissionStatus.GRADED);

        SubmissionEntity saved = submissionRepository.save(submission);

        Map<String, Object> details = new HashMap<>();
        details.put("submissionId", submissionId);
        details.put("assignmentId", submission.getAssignment().getId());
        details.put("studentId", submission.getStudent().getId());
        details.put("oldScore", oldScore);
        details.put("newScore", score);
        auditLogService.log(AuditAction.SUBMISSION_GRADED, details, submission.getStudent().getId());

        return mapToResponseDto(saved);
    }

    private SubmissionResponseDto mapToResponseDto(SubmissionEntity entity) {
        SubmissionResponseDto dto = new SubmissionResponseDto();
        dto.setId(entity.getId());
        dto.setAssignmentId(entity.getAssignment().getId());
        dto.setAssignmentTitle(entity.getAssignment().getTitle());
        dto.setStudentId(entity.getStudent().getId());
        dto.setStudentName(entity.getStudent().getName() + " " + entity.getStudent().getSurname());
        dto.setSubmissionDate(entity.getSubmissionDate());
        dto.setFileUrl(entity.getFileUrl());
        dto.setScore(entity.getScore());
        dto.setFeedback(entity.getFeedback());
        dto.setStatus(entity.getStatus().name());
        return dto;
    }

    private void verifyTeacherAccessToCourse(Long courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (isAdmin(auth)) {
            return;
        }
        
        if (isTeacher(auth)) {
            String username = auth.getName();
            UserEntity teacher = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CourseEntity course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new CourseNotFoundException(courseId));
            
            if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
                throw new AccessDeniedException("You do not have access to this course");
            }
        }
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isTeacher(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
    }
}
