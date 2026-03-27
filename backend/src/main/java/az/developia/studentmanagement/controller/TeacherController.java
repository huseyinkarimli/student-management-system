package az.developia.studentmanagement.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.developia.studentmanagement.dto.AssignmentResponseDto;
import az.developia.studentmanagement.dto.CourseResponseDto;
import az.developia.studentmanagement.dto.RecentSubmissionDto;
import az.developia.studentmanagement.dto.TeacherDashboardDto;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.repository.UserRepository;
import az.developia.studentmanagement.service.CourseService;
import az.developia.studentmanagement.service.TeacherService;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:3000"})
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserRepository userRepository;

    private Logger logger = LoggerFactory.getLogger(TeacherController.class);

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getId();
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public TeacherDashboardDto getTeacherDashboard() {
        logger.info("Fetching teacher dashboard");
        return teacherService.getTeacherDashboard();
    }

    @GetMapping("/courses")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public List<CourseResponseDto> getTeacherCourses() {
        logger.info("Fetching courses for teacher");
        return courseService.findByTeacherId(getCurrentUserId());
    }

    @GetMapping("/assignments")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public List<AssignmentResponseDto> getTeacherAssignments() {
        logger.info("Fetching assignments for teacher");
        return teacherService.getTeacherAssignments();
    }

    @GetMapping("/submissions/pending")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public List<RecentSubmissionDto> getPendingSubmissions() {
        logger.info("Fetching pending submissions for teacher");
        return teacherService.getPendingSubmissions();
    }
}
