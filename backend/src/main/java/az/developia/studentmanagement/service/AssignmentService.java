package az.developia.studentmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import az.developia.studentmanagement.dto.AssignmentRequestDto;
import az.developia.studentmanagement.dto.AssignmentResponseDto;
import az.developia.studentmanagement.entity.AssignmentEntity;
import az.developia.studentmanagement.entity.CourseEntity;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.exception.AssignmentNotFoundException;
import az.developia.studentmanagement.exception.CourseNotFoundException;
import az.developia.studentmanagement.repository.AssignmentRepository;
import az.developia.studentmanagement.repository.CourseRepository;
import az.developia.studentmanagement.repository.SubmissionRepository;
import az.developia.studentmanagement.repository.UserRepository;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper mapper;

    public List<AssignmentResponseDto> findByCourseId(Long courseId) {
        verifyTeacherAccessToCourse(courseId);
        return assignmentRepository.findByCourseIdOrderByDueDateAsc(courseId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public AssignmentResponseDto findById(Long id) {
        AssignmentEntity entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(id));
        verifyTeacherAccessToCourse(entity.getCourse().getId());
        return mapToResponseDto(entity);
    }

    public AssignmentResponseDto create(AssignmentRequestDto request, Long createdBy) {
        CourseEntity course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException(request.getCourseId()));

        verifyTeacherAccessToCourse(request.getCourseId());

        AssignmentEntity entity = new AssignmentEntity();
        entity.setCourse(course);
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setDueDate(request.getDueDate());
        entity.setMaxScore(request.getMaxScore());
        entity.setCreatedBy(createdBy);

        AssignmentEntity saved = assignmentRepository.save(entity);
        return mapToResponseDto(saved);
    }

    public AssignmentResponseDto update(Long id, AssignmentRequestDto request) {
        AssignmentEntity entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(id));

        verifyTeacherAccessToCourse(entity.getCourse().getId());

        CourseEntity course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException(request.getCourseId()));

        entity.setCourse(course);
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setDueDate(request.getDueDate());
        entity.setMaxScore(request.getMaxScore());

        AssignmentEntity saved = assignmentRepository.save(entity);
        return mapToResponseDto(saved);
    }

    public void delete(Long id) {
        AssignmentEntity entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(id));

        verifyTeacherAccessToCourse(entity.getCourse().getId());

        Long submissionCount = submissionRepository.countByAssignmentId(id);
        if (submissionCount > 0) {
            throw new IllegalStateException("Cannot delete assignment with " + submissionCount + " submissions");
        }

        assignmentRepository.delete(entity);
    }

    private AssignmentResponseDto mapToResponseDto(AssignmentEntity entity) {
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
