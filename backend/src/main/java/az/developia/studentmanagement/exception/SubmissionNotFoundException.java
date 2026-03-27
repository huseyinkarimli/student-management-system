package az.developia.studentmanagement.exception;

public class SubmissionNotFoundException extends RuntimeException {

    public SubmissionNotFoundException(String message) {
        super(message);
    }

    public SubmissionNotFoundException(Long id) {
        super("Submission with ID " + id + " not found");
    }
}
