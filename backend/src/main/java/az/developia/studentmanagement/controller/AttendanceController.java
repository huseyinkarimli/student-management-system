package az.developia.studentmanagement.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import az.developia.studentmanagement.dto.AttendanceBatchRequestDto;
import az.developia.studentmanagement.dto.AttendanceRequestDto;
import az.developia.studentmanagement.dto.AttendanceResponseDto;
import az.developia.studentmanagement.dto.AttendanceStatsDto;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.exception.OurValidationException;
import az.developia.studentmanagement.repository.UserRepository;
import az.developia.studentmanagement.service.AttendanceService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:3000"})
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserRepository userRepository;

    private Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getId();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public AttendanceResponseDto recordAttendance(@Valid @RequestBody AttendanceRequestDto request, BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }

        logger.info("Recording attendance for student {} in course {} on {}", 
                request.getStudentId(), request.getCourseId(), request.getDate());
        return attendanceService.recordAttendance(request, getCurrentUserId());
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public List<AttendanceResponseDto> recordAttendanceBatch(@Valid @RequestBody AttendanceBatchRequestDto request, BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }

        logger.info("Recording batch attendance for course {} on {}", request.getCourseId(), request.getDate());
        return attendanceService.recordAttendanceBatch(request, getCurrentUserId());
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @attendanceSecurity.isTeacherOfCourse(#courseId, principal)")
    public List<AttendanceResponseDto> getAttendanceByCourseAndDate(
            @PathVariable Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.info("Fetching attendance for course {} on {}", courseId, date);
        return attendanceService.getAttendanceByCourseAndDate(courseId, date);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public List<AttendanceResponseDto> getAttendanceByStudent(
            @PathVariable Long studentId,
            @RequestParam Long courseId) {
        logger.info("Fetching attendance for student {} in course {}", studentId, courseId);
        return attendanceService.getAttendanceByStudent(studentId, courseId);
    }

    @GetMapping("/student/{studentId}/stats")
    @PreAuthorize("isAuthenticated()")
    public AttendanceStatsDto getStudentAttendanceStats(
            @PathVariable Long studentId,
            @RequestParam Long courseId) {
        logger.info("Fetching attendance stats for student {} in course {}", studentId, courseId);
        return attendanceService.getAttendanceStatsForStudent(studentId, courseId);
    }

    @GetMapping("/course/{courseId}/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @attendanceSecurity.isTeacherOfCourse(#courseId, principal)")
    public AttendanceStatsDto getCourseAttendanceStats(@PathVariable Long courseId) {
        logger.info("Fetching attendance stats for course {}", courseId);
        return attendanceService.getAttendanceStatsForCourse(courseId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public AttendanceResponseDto updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequestDto request,
            BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }

        logger.info("Updating attendance record {}", id);
        return attendanceService.updateAttendance(id, request, getCurrentUserId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public void deleteAttendance(@PathVariable Long id) {
        logger.info("Deleting attendance record {}", id);
        attendanceService.deleteAttendance(id);
    }
}
