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
public class AssignmentResponseDto {

    private Long id;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Integer maxScore;
    private LocalDateTime createdAt;
    private Long createdBy;
    private String createdByUsername;
    private Long submissionCount;
}
