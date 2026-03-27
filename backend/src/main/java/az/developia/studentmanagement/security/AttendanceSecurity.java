package az.developia.studentmanagement.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import az.developia.studentmanagement.entity.CourseEntity;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.repository.CourseRepository;
import az.developia.studentmanagement.repository.UserRepository;

@Component("attendanceSecurity")
public class AttendanceSecurity {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    public boolean isTeacherOfCourse(Long courseId, Authentication authentication) {
        if (authentication == null || courseId == null) {
            return false;
        }

        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }

        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course == null || course.getTeacher() == null) {
            return false;
        }

        return course.getTeacher().getId().equals(user.getId());
    }

    public boolean isStudentOrTeacherOfCourse(Long studentId, Long courseId, Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }

        // Check if user is the student
        if (studentId != null && studentId.equals(user.getId())) {
            return true;
        }

        // Check if user is teacher of the course
        if (courseId != null) {
            return isTeacherOfCourse(courseId, authentication);
        }

        return false;
    }
}
