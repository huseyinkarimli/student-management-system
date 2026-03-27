package az.developia.studentmanagement.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import az.developia.studentmanagement.exception.AssignmentNotFoundException;
import az.developia.studentmanagement.exception.CourseNotFoundException;
import az.developia.studentmanagement.exception.DuplicateEnrollmentException;
import az.developia.studentmanagement.exception.DuplicateSubmissionException;
import az.developia.studentmanagement.exception.EnrollmentNotFoundException;
import az.developia.studentmanagement.exception.OurNotFoundException;
import az.developia.studentmanagement.exception.OurValidationException;
import az.developia.studentmanagement.exception.StudentNotFoundException;
import az.developia.studentmanagement.exception.SubmissionNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.Data;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OurNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOurNotFoundException(OurNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStudentNotFoundException(StudentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCourseNotFoundException(CourseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(EnrollmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEnrollmentNotFoundException(EnrollmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEnrollmentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEnrollmentException(DuplicateEnrollmentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(AssignmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAssignmentNotFoundException(AssignmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(SubmissionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSubmissionNotFoundException(SubmissionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateSubmissionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSubmissionException(DuplicateSubmissionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(OurValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<FieldErrorResponse> handleOurValidationException(OurValidationException ex) {
        BindingResult br = ex.getBr();
        List<FieldError> fieldErrors = br.getFieldErrors();
        List<FieldErrorResponse> errors = new ArrayList<>();

        for (FieldError fe : fieldErrors) {
            FieldErrorResponse f = new FieldErrorResponse();
            f.setField(fe.getField());
            f.setMessage(fe.getDefaultMessage());
            errors.add(f);
        }

        return errors;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<FieldErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .map(v -> {
                    FieldErrorResponse f = new FieldErrorResponse();
                    f.setField(v.getPropertyPath().toString());
                    f.setMessage(v.getMessage());
                    return f;
                })
                .collect(Collectors.toList());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR",
                        "Xəta baş verdi: " + ex.getMessage()));
    }
}

record ErrorResponse(String error, String message) {}

@Data
class FieldErrorResponse {
    private String field;
    private String message;
}