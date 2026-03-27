package az.developia.studentmanagement.exception;

public class DuplicateSubmissionException extends RuntimeException {

    public DuplicateSubmissionException(String message) {
        super(message);
    }

    public DuplicateSubmissionException(Long assignmentId, Long studentId) {
        super("Student " + studentId + " has already submitted assignment " + assignmentId);
    }
}
