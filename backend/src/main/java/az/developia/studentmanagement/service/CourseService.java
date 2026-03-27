package az.developia.studentmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import az.developia.studentmanagement.dto.CourseRequestDto;
import az.developia.studentmanagement.dto.CourseResponseDto;
import az.developia.studentmanagement.entity.CourseEntity;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.exception.CourseNotFoundException;
import az.developia.studentmanagement.repository.CourseRepository;
import az.developia.studentmanagement.repository.EnrollmentRepository;
import az.developia.studentmanagement.repository.UserRepository;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper mapper;

    public List<CourseResponseDto> findAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        if (isAdmin(auth)) {
            return courseRepository.findAll().stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } else if (isTeacher(auth)) {
            UserEntity teacher = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return courseRepository.findByTeacherId(teacher.getId()).stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } else {
            return courseRepository.findAll().stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        }
    }

    public List<CourseResponseDto> findByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public CourseResponseDto findById(Long id) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth) && isTeacher(auth)) {
            String username = auth.getName();
            UserEntity teacher = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (entity.getTeacher() == null || !entity.getTeacher().getId().equals(teacher.getId())) {
                throw new AccessDeniedException("You do not have access to this course");
            }
        }
        
        return mapToResponseDto(entity);
    }

    public CourseResponseDto create(CourseRequestDto request) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Course with code " + request.getCode() + " already exists");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CourseEntity entity = new CourseEntity();
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setCredits(request.getCredits());
        entity.setSchedule(request.getSchedule());
        
        if (isTeacher(auth) && !isAdmin(auth)) {
            String username = auth.getName();
            UserEntity teacher = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            entity.setTeacher(teacher);
        } else if (isAdmin(auth) && request.getTeacherId() != null) {
            UserEntity teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.getTeacherId()));
            entity.setTeacher(teacher);
        }
        
        CourseEntity saved = courseRepository.save(entity);
        return mapToResponseDto(saved);
    }

    public CourseResponseDto update(Long id, CourseRequestDto request) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth) && isTeacher(auth)) {
            String username = auth.getName();
            UserEntity teacher = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (entity.getTeacher() == null || !entity.getTeacher().getId().equals(teacher.getId())) {
                throw new AccessDeniedException("You can only update your own courses");
            }
        }

        if (courseRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new IllegalArgumentException("Course with code " + request.getCode() + " already exists");
        }

        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setCredits(request.getCredits());
        entity.setSchedule(request.getSchedule());
        
        if (isAdmin(auth) && request.getTeacherId() != null) {
            UserEntity teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.getTeacherId()));
            entity.setTeacher(teacher);
        }

        CourseEntity saved = courseRepository.save(entity);
        return mapToResponseDto(saved);
    }

    public void delete(Long id) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth) && isTeacher(auth)) {
            String username = auth.getName();
            UserEntity teacher = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (entity.getTeacher() == null || !entity.getTeacher().getId().equals(teacher.getId())) {
                throw new AccessDeniedException("You can only delete your own courses");
            }
        }

        Long enrollmentCount = enrollmentRepository.countByCourseId(id);
        if (enrollmentCount > 0) {
            throw new IllegalStateException("Cannot delete course with " + enrollmentCount + " enrolled students");
        }

        courseRepository.delete(entity);
    }

    private CourseResponseDto mapToResponseDto(CourseEntity entity) {
        CourseResponseDto dto = new CourseResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setDescription(entity.getDescription());
        dto.setCredits(entity.getCredits());
        dto.setSchedule(entity.getSchedule());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        if (entity.getTeacher() != null) {
            dto.setTeacherId(entity.getTeacher().getId());
            dto.setTeacherName(entity.getTeacher().getUsername());
        }
        
        Long studentCount = enrollmentRepository.countByCourseId(entity.getId());
        dto.setStudentCount(studentCount);
        return dto;
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isTeacher(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
    }

    public boolean canAccessCourse(Long courseId, String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (isAdmin(auth)) {
            return true;
        }
        
        if (isTeacher(auth)) {
            CourseEntity course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new CourseNotFoundException(courseId));
            UserEntity teacher = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return course.getTeacher() != null && course.getTeacher().getId().equals(teacher.getId());
        }
        
        return false;
    }
}
