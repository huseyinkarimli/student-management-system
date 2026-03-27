package az.developia.studentmanagement.exception;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(String message) {
        super(message);
    }

    public StudentNotFoundException(Long id) {
        super("ID-si " + id + " olan tələbə tapılmadı");
    }
}