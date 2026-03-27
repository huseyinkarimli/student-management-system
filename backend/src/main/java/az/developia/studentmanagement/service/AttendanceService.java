package az.developia.studentmanagement.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import az.developia.studentmanagement.audit.AuditAction;
import az.developia.studentmanagement.audit.AuditLogService;
import az.developia.studentmanagement.dto.AttendanceBatchRequestDto;
import az.developia.studentmanagement.dto.AttendanceRequestDto;
import az.developia.studentmanagement.dto.AttendanceResponseDto;
import az.developia.studentmanagement.dto.AttendanceStatsDto;
import az.developia.studentmanagement.entity.AttendanceEntity;
import az.developia.studentmanagement.entity.AttendanceStatus;
import az.developia.studentmanagement.entity.CourseEntity;
import az.developia.studentmanagement.entity.StudentEntity;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.exception.CourseNotFoundException;
import az.developia.studentmanagement.exception.StudentNotFoundException;
import az.developia.studentmanagement.repository.AttendanceRepository;
import az.developia.studentmanagement.repository.CourseRepository;
import az.developia.studentmanagement.repository.EnrollmentRepository;
import az.developia.studentmanagement.repository.StudentRepository;
import az.developia.studentmanagement.repository.UserRepository;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public AttendanceResponseDto recordAttendance(AttendanceRequestDto request, Long currentUserId) {
        verifyTeacherAccessToCourse(request.getCourseId());

        StudentEntity student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException(request.getStudentId()));

        CourseEntity course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException(request.getCourseId()));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new IllegalStateException("Student is not enrolled in this course");
        }

        Optional<AttendanceEntity> existing = attendanceRepository.findByStudentIdAndCourseIdAndDate(
                request.getStudentId(), request.getCourseId(), request.getDate());

        AttendanceEntity attendance;
        boolean isUpdate = false;

        if (existing.isPresent()) {
            attendance = existing.get();
            attendance.setStatus(request.getStatus());
            attendance.setRemarks(request.getRemarks());
            attendance.setRecordedBy(currentUserId);
            isUpdate = true;
        } else {
            attendance = new AttendanceEntity();
            attendance.setStudent(student);
            attendance.setCourse(course);
            attendance.setDate(request.getDate());
            attendance.setStatus(request.getStatus());
            attendance.setRemarks(request.getRemarks());
            attendance.setRecordedBy(currentUserId);
        }

        AttendanceEntity saved = attendanceRepository.save(attendance);

        Map<String, Object> details = new HashMap<>();
        details.put("attendanceId", saved.getId());
        details.put("studentId", student.getId());
        details.put("studentName", student.getName() + " " + student.getSurname());
        details.put("courseId", course.getId());
        details.put("courseName", course.getName());
        details.put("date", request.getDate().toString());
        details.put("status", request.getStatus().name());

        auditLogService.log(isUpdate ? AuditAction.ATTENDANCE_UPDATED : AuditAction.ATTENDANCE_RECORDED, 
                details, student.getId());

        return mapToResponseDto(saved);
    }

    @Transactional
    public List<AttendanceResponseDto> recordAttendanceBatch(AttendanceBatchRequestDto request, Long currentUserId) {
        verifyTeacherAccessToCourse(request.getCourseId());

        CourseEntity course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException(request.getCourseId()));

        return request.getRecords().stream().map(record -> {
            StudentEntity student = studentRepository.findById(record.getStudentId())
                    .orElseThrow(() -> new StudentNotFoundException(record.getStudentId()));

            if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
                throw new IllegalStateException("Student " + student.getName() + " is not enrolled in this course");
            }

            Optional<AttendanceEntity> existing = attendanceRepository.findByStudentIdAndCourseIdAndDate(
                    record.getStudentId(), request.getCourseId(), request.getDate());

            AttendanceEntity attendance;
            boolean isUpdate = false;

            if (existing.isPresent()) {
                attendance = existing.get();
                attendance.setStatus(record.getStatus());
                attendance.setRemarks(record.getRemarks());
                attendance.setRecordedBy(currentUserId);
                isUpdate = true;
            } else {
                attendance = new AttendanceEntity();
                attendance.setStudent(student);
                attendance.setCourse(course);
                attendance.setDate(request.getDate());
                attendance.setStatus(record.getStatus());
                attendance.setRemarks(record.getRemarks());
                attendance.setRecordedBy(currentUserId);
            }

            AttendanceEntity saved = attendanceRepository.save(attendance);

            Map<String, Object> details = new HashMap<>();
            details.put("attendanceId", saved.getId());
            details.put("studentId", student.getId());
            details.put("courseId", course.getId());
            details.put("date", request.getDate().toString());
            details.put("status", record.getStatus().name());

            auditLogService.log(isUpdate ? AuditAction.ATTENDANCE_UPDATED : AuditAction.ATTENDANCE_RECORDED, 
                    details, student.getId());

            return mapToResponseDto(saved);
        }).collect(Collectors.toList());
    }

    public List<AttendanceResponseDto> getAttendanceByCourseAndDate(Long courseId, LocalDate date) {
        verifyTeacherAccessToCourse(courseId);

        return attendanceRepository.findByCourseIdAndDate(courseId, date).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponseDto> getAttendanceByStudent(Long studentId, Long courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        UserEntity currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!isAdmin(auth) && !currentUser.getId().equals(studentId)) {
            verifyTeacherAccessToCourse(courseId);
        }

        return attendanceRepository.findByCourseIdAndStudentId(courseId, studentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public AttendanceStatsDto getAttendanceStatsForStudent(Long studentId, Long courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        UserEntity currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!isAdmin(auth) && !currentUser.getId().equals(studentId)) {
            verifyTeacherAccessToCourse(courseId);
        }

        List<AttendanceEntity> records = attendanceRepository.findByCourseIdAndStudentId(courseId, studentId);

        return calculateStats(records);
    }

    public AttendanceStatsDto getAttendanceStatsForCourse(Long courseId) {
        verifyTeacherAccessToCourse(courseId);

        List<AttendanceEntity> records = attendanceRepository.findByCourseId(courseId);

        return calculateStats(records);
    }

    @Transactional
    public AttendanceResponseDto updateAttendance(Long id, AttendanceRequestDto request, Long currentUserId) {
        AttendanceEntity attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        verifyTeacherAccessToCourse(attendance.getCourse().getId());

        attendance.setStatus(request.getStatus());
        attendance.setRemarks(request.getRemarks());
        attendance.setRecordedBy(currentUserId);

        AttendanceEntity saved = attendanceRepository.save(attendance);

        Map<String, Object> details = new HashMap<>();
        details.put("attendanceId", saved.getId());
        details.put("studentId", saved.getStudent().getId());
        details.put("courseId", saved.getCourse().getId());
        details.put("date", saved.getDate().toString());
        details.put("status", request.getStatus().name());

        auditLogService.log(AuditAction.ATTENDANCE_UPDATED, details, saved.getStudent().getId());

        return mapToResponseDto(saved);
    }

    @Transactional
    public void deleteAttendance(Long id) {
        AttendanceEntity attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        verifyTeacherAccessToCourse(attendance.getCourse().getId());

        Map<String, Object> details = new HashMap<>();
        details.put("attendanceId", attendance.getId());
        details.put("studentId", attendance.getStudent().getId());
        details.put("courseId", attendance.getCourse().getId());
        details.put("date", attendance.getDate().toString());

        auditLogService.log(AuditAction.ATTENDANCE_DELETED, details, attendance.getStudent().getId());

        attendanceRepository.delete(attendance);
    }

    private AttendanceResponseDto mapToResponseDto(AttendanceEntity entity) {
        AttendanceResponseDto dto = new AttendanceResponseDto();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudent().getId());
        dto.setStudentName(entity.getStudent().getName() + " " + entity.getStudent().getSurname());
        dto.setCourseId(entity.getCourse().getId());
        dto.setCourseName(entity.getCourse().getName());
        dto.setDate(entity.getDate());
        dto.setStatus(entity.getStatus());
        dto.setRemarks(entity.getRemarks());
        dto.setRecordedBy(entity.getRecordedBy());
        dto.setRecordedAt(entity.getRecordedAt());

        userRepository.findById(entity.getRecordedBy()).ifPresent(user -> 
            dto.setRecordedByUsername(user.getUsername())
        );

        return dto;
    }

    private AttendanceStatsDto calculateStats(List<AttendanceEntity> records) {
        AttendanceStatsDto stats = new AttendanceStatsDto();
        stats.setTotalClasses((long) records.size());

        long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long late = records.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
        long excused = records.stream().filter(r -> r.getStatus() == AttendanceStatus.EXCUSED).count();

        stats.setPresentCount(present);
        stats.setAbsentCount(absent);
        stats.setLateCount(late);
        stats.setExcusedCount(excused);

        if (records.size() > 0) {
            double percentage = ((double) (present + late) / records.size()) * 100;
            stats.setAttendancePercentage(Math.round(percentage * 100.0) / 100.0);
        } else {
            stats.setAttendancePercentage(0.0);
        }

        return stats;
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
        } else {
            throw new AccessDeniedException("You do not have permission to manage attendance");
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
