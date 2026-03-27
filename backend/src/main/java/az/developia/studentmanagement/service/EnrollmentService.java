package az.developia.studentmanagement.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import az.developia.studentmanagement.audit.AuditAction;
import az.developia.studentmanagement.audit.AuditLogService;
import az.developia.studentmanagement.dto.EnrollmentRequestDto;
import az.developia.studentmanagement.dto.EnrollmentResponseDto;
import az.developia.studentmanagement.entity.CourseEntity;
import az.developia.studentmanagement.entity.EnrollmentEntity;
import az.developia.studentmanagement.entity.StudentEntity;
import az.developia.studentmanagement.exception.CourseNotFoundException;
import az.developia.studentmanagement.exception.DuplicateEnrollmentException;
import az.developia.studentmanagement.exception.EnrollmentNotFoundException;
import az.developia.studentmanagement.exception.StudentNotFoundException;
import az.developia.studentmanagement.repository.CourseRepository;
import az.developia.studentmanagement.repository.EnrollmentRepository;
import az.developia.studentmanagement.repository.StudentRepository;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<EnrollmentResponseDto> findByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponseDto> findByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public EnrollmentResponseDto enroll(EnrollmentRequestDto request) {
        StudentEntity student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException(request.getStudentId()));

        CourseEntity course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException(request.getCourseId()));

        if (enrollmentRepository.existsByStudentIdAndCourseId(request.getStudentId(), request.getCourseId())) {
            throw new DuplicateEnrollmentException(request.getStudentId(), request.getCourseId());
        }

        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentEntity.EnrollmentStatus.ENROLLED);

        EnrollmentEntity saved = enrollmentRepository.save(enrollment);

        Map<String, Object> details = new HashMap<>();
        details.put("studentId", student.getId());
        details.put("studentName", student.getName() + " " + student.getSurname());
        details.put("courseId", course.getId());
        details.put("courseName", course.getName());
        auditLogService.log(AuditAction.ENROLLMENT_CREATED, details, student.getId());

        return mapToResponseDto(saved);
    }

    public void dropEnrollment(Long enrollmentId) {
        EnrollmentEntity enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        Map<String, Object> details = new HashMap<>();
        details.put("studentId", enrollment.getStudent().getId());
        details.put("studentName", enrollment.getStudent().getName() + " " + enrollment.getStudent().getSurname());
        details.put("courseId", enrollment.getCourse().getId());
        details.put("courseName", enrollment.getCourse().getName());
        auditLogService.log(AuditAction.ENROLLMENT_DELETED, details, enrollment.getStudent().getId());

        enrollmentRepository.delete(enrollment);
    }

    public EnrollmentResponseDto updateGrade(Long enrollmentId, Double grade) {
        EnrollmentEntity enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        if (grade != null && (grade < 0.0 || grade > 10.0)) {
            throw new IllegalArgumentException("Grade must be between 0.0 and 10.0");
        }

        Double oldGrade = enrollment.getGrade();
        enrollment.setGrade(grade);
        EnrollmentEntity saved = enrollmentRepository.save(enrollment);

        Map<String, Object> details = new HashMap<>();
        details.put("enrollmentId", enrollmentId);
        details.put("studentId", enrollment.getStudent().getId());
        details.put("courseId", enrollment.getCourse().getId());
        details.put("oldGrade", oldGrade);
        details.put("newGrade", grade);
        auditLogService.log(AuditAction.GRADE_UPDATED, details, enrollment.getStudent().getId());

        return mapToResponseDto(saved);
    }

    private EnrollmentResponseDto mapToResponseDto(EnrollmentEntity entity) {
        EnrollmentResponseDto dto = new EnrollmentResponseDto();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudent().getId());
        dto.setStudentName(entity.getStudent().getName() + " " + entity.getStudent().getSurname());
        dto.setCourseId(entity.getCourse().getId());
        dto.setCourseName(entity.getCourse().getName());
        dto.setEnrollmentDate(entity.getEnrollmentDate());
        dto.setGrade(entity.getGrade());
        dto.setStatus(entity.getStatus().name());
        return dto;
    }
}
