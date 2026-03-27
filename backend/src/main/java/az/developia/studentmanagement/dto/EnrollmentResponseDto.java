package az.developia.studentmanagement.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentResponseDto {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private LocalDateTime enrollmentDate;
    private Double grade;
    private String status;
}
