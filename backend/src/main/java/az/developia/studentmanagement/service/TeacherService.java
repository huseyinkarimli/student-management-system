package az.developia.studentmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import az.developia.studentmanagement.dto.AssignmentResponseDto;
import az.developia.studentmanagement.dto.RecentSubmissionDto;
import az.developia.studentmanagement.dto.TeacherDashboardDto;
import az.developia.studentmanagement.entity.AssignmentEntity;
import az.developia.studentmanagement.entity.CourseEntity;
import az.developia.studentmanagement.entity.SubmissionEntity;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.repository.AssignmentRepository;
import az.developia.studentmanagement.repository.CourseRepository;
import az.developia.studentmanagement.repository.SubmissionRepository;
import az.developia.studentmanagement.repository.UserRepository;

@Service
public class TeacherService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    public TeacherDashboardDto getTeacherDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UserEntity teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CourseEntity> courses = courseRepository.findByTeacherId(teacher.getId());
        
        List<Long> courseIds = courses.stream()
                .map(CourseEntity::getId)
                .collect(Collectors.toList());

        Long totalAssignments = 0L;
        Long pendingSubmissions = 0L;
        
        if (!courseIds.isEmpty()) {
            totalAssignments = assignmentRepository.findAll().stream()
                    .filter(a -> courseIds.contains(a.getCourse().getId()))
                    .count();

            pendingSubmissions = submissionRepository.findAll().stream()
                    .filter(s -> courseIds.contains(s.getAssignment().getCourse().getId()))
                    .filter(s -> s.getStatus() == SubmissionEntity.SubmissionStatus.SUBMITTED)
                    .count();
        }

        List<RecentSubmissionDto> recentSubmissions = getRecentSubmissions(courseIds);

        TeacherDashboardDto dashboard = new TeacherDashboardDto();
        dashboard.setTotalCourses((long) courses.size());
        dashboard.setTotalAssignments(totalAssignments);
        dashboard.setPendingSubmissions(pendingSubmissions);
        dashboard.setRecentSubmissions(recentSubmissions);

        return dashboard;
    }

    public List<AssignmentResponseDto> getTeacherAssignments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UserEntity teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CourseEntity> courses = courseRepository.findByTeacherId(teacher.getId());
        
        List<Long> courseIds = courses.stream()
                .map(CourseEntity::getId)
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return List.of();
        }

        return assignmentRepository.findAll().stream()
                .filter(a -> courseIds.contains(a.getCourse().getId()))
                .map(this::mapToAssignmentResponseDto)
                .collect(Collectors.toList());
    }

    public List<RecentSubmissionDto> getPendingSubmissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UserEntity teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CourseEntity> courses = courseRepository.findByTeacherId(teacher.getId());
        
        List<Long> courseIds = courses.stream()
                .map(CourseEntity::getId)
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return List.of();
        }

        return submissionRepository.findAll().stream()
                .filter(s -> courseIds.contains(s.getAssignment().getCourse().getId()))
                .filter(s -> s.getStatus() == SubmissionEntity.SubmissionStatus.SUBMITTED)
                .map(this::mapToRecentSubmissionDto)
                .collect(Collectors.toList());
    }

    private List<RecentSubmissionDto> getRecentSubmissions(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return List.of();
        }

        return submissionRepository.findAll().stream()
                .filter(s -> courseIds.contains(s.getAssignment().getCourse().getId()))
                .sorted((s1, s2) -> s2.getSubmissionDate().compareTo(s1.getSubmissionDate()))
                .limit(10)
                .map(this::mapToRecentSubmissionDto)
                .collect(Collectors.toList());
    }

    private RecentSubmissionDto mapToRecentSubmissionDto(SubmissionEntity entity) {
        RecentSubmissionDto dto = new RecentSubmissionDto();
        dto.setSubmissionId(entity.getId());
        dto.setStudentName(entity.getStudent().getName() + " " + entity.getStudent().getSurname());
        dto.setAssignmentTitle(entity.getAssignment().getTitle());
        dto.setCourseName(entity.getAssignment().getCourse().getName());
        dto.setSubmissionDate(entity.getSubmissionDate());
        dto.setStatus(entity.getStatus().name());
        return dto;
    }

    private AssignmentResponseDto mapToAssignmentResponseDto(AssignmentEntity entity) {
        AssignmentResponseDto dto = new AssignmentResponseDto();
        dto.setId(entity.getId());
        dto.setCourseId(entity.getCourse().getId());
        dto.setCourseName(entity.getCourse().getName());
        dto.setCourseCode(entity.getCourse().getCode());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setDueDate(entity.getDueDate());
        dto.setMaxScore(entity.getMaxScore());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());

        userRepository.findById(entity.getCreatedBy()).ifPresent(user -> 
            dto.setCreatedByUsername(user.getUsername())
        );

        Long submissionCount = submissionRepository.countByAssignmentId(entity.getId());
        dto.setSubmissionCount(submissionCount);

        return dto;
    }
}
