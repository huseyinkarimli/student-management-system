package az.developia.studentmanagement.exception;

public class DuplicateEnrollmentException extends RuntimeException {

    public DuplicateEnrollmentException(String message) {
        super(message);
    }

    public DuplicateEnrollmentException(Long studentId, Long courseId) {
        super("Student " + studentId + " is already enrolled in course " + courseId);
    }
}
