package az.developia.studentmanagement.exception;

public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(String message) {
        super(message);
    }

    public CourseNotFoundException(Long id) {
        super("Course with ID " + id + " not found");
    }
}
