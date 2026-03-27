package az.developia.studentmanagement.exception;

public class AssignmentNotFoundException extends RuntimeException {

    public AssignmentNotFoundException(String message) {
        super(message);
    }

    public AssignmentNotFoundException(Long id) {
        super("Assignment with ID " + id + " not found");
    }
}
