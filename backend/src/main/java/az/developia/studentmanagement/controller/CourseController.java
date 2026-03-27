package az.developia.studentmanagement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import az.developia.studentmanagement.audit.AuditAction;
import az.developia.studentmanagement.audit.AuditLogService;
import az.developia.studentmanagement.dto.CourseRequestDto;
import az.developia.studentmanagement.dto.CourseResponseDto;
import az.developia.studentmanagement.exception.OurValidationException;
import az.developia.studentmanagement.service.CourseService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private AuditLogService auditLogService;

    private Logger logger = LoggerFactory.getLogger(CourseController.class);

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CourseResponseDto> getAllCourses() {
        logger.info("Fetching all courses");
        return courseService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CourseResponseDto getCourseById(@PathVariable Long id) {
        logger.info("Fetching course with id: {}", id);
        CourseResponseDto course = courseService.findById(id);
        auditLogService.log(AuditAction.COURSE_VIEWED, Map.of("courseId", id, "courseName", course.getName()), null);
        return course;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public CourseResponseDto createCourse(@Valid @RequestBody CourseRequestDto request, BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }

        logger.info("Creating new course: {}", request.getCode());
        CourseResponseDto created = courseService.create(request);

        Map<String, Object> details = new HashMap<>();
        details.put("courseId", created.getId());
        details.put("name", created.getName());
        details.put("code", created.getCode());
        details.put("credits", created.getCredits());
        auditLogService.log(AuditAction.COURSE_CREATED, details, null);

        return created;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public CourseResponseDto updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequestDto request, BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }

        logger.info("Updating course with id: {}", id);
        CourseResponseDto oldCourse = courseService.findById(id);
        CourseResponseDto updated = courseService.update(id, request);

        Map<String, Object> details = new HashMap<>();
        details.put("courseId", id);
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("name", oldCourse.getName());
        oldValues.put("code", oldCourse.getCode());
        oldValues.put("credits", oldCourse.getCredits());
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("name", updated.getName());
        newValues.put("code", updated.getCode());
        newValues.put("credits", updated.getCredits());
        details.put("oldValues", oldValues);
        details.put("newValues", newValues);
        auditLogService.log(AuditAction.COURSE_UPDATED, details, null);

        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public void deleteCourse(@PathVariable Long id) {
        logger.info("Deleting course with id: {}", id);
        CourseResponseDto course = courseService.findById(id);
        courseService.delete(id);

        Map<String, Object> details = new HashMap<>();
        details.put("courseId", id);
        details.put("courseName", course.getName());
        details.put("courseCode", course.getCode());
        auditLogService.log(AuditAction.COURSE_DELETED, details, null);
    }
}
