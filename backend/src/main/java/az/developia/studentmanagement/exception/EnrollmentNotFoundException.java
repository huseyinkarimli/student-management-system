package az.developia.studentmanagement.exception;

public class EnrollmentNotFoundException extends RuntimeException {

    public EnrollmentNotFoundException(String message) {
        super(message);
    }

    public EnrollmentNotFoundException(Long id) {
        super("Enrollment with ID " + id + " not found");
    }
}
