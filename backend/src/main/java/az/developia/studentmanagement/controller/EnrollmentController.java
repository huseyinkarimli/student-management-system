package az.developia.studentmanagement.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import az.developia.studentmanagement.dto.EnrollmentRequestDto;
import az.developia.studentmanagement.dto.EnrollmentResponseDto;
import az.developia.studentmanagement.exception.OurValidationException;
import az.developia.studentmanagement.service.EnrollmentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    private Logger logger = LoggerFactory.getLogger(EnrollmentController.class);

    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public List<EnrollmentResponseDto> getEnrollmentsByStudent(@PathVariable Long studentId) {
        logger.info("Fetching enrollments for student: {}", studentId);
        return enrollmentService.findByStudentId(studentId);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public List<EnrollmentResponseDto> getEnrollmentsByCourse(@PathVariable Long courseId) {
        logger.info("Fetching enrollments for course: {}", courseId);
        return enrollmentService.findByCourseId(courseId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public EnrollmentResponseDto enrollStudent(@Valid @RequestBody EnrollmentRequestDto request, BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }

        logger.info("Enrolling student {} in course {}", request.getStudentId(), request.getCourseId());
        return enrollmentService.enroll(request);
    }

    @PutMapping("/{id}/grade")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public EnrollmentResponseDto updateGrade(@PathVariable Long id, @RequestBody GradeUpdateRequest request) {
        logger.info("Updating grade for enrollment: {}", id);
        return enrollmentService.updateGrade(id, request.getGrade());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void dropEnrollment(@PathVariable Long id) {
        logger.info("Dropping enrollment: {}", id);
        enrollmentService.dropEnrollment(id);
    }

    public static class GradeUpdateRequest {
        private Double grade;

        public Double getGrade() {
            return grade;
        }

        public void setGrade(Double grade) {
            this.grade = grade;
        }
    }
}
