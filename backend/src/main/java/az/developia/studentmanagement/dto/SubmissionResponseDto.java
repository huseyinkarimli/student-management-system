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
public class SubmissionResponseDto {

    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentName;
    private LocalDateTime submissionDate;
    private String fileUrl;
    private Integer score;
    private String feedback;
    private String status;
}
