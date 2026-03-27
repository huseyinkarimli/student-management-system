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
public class RecentSubmissionDto {

    private Long submissionId;
    private String studentName;
    private String assignmentTitle;
    private String courseName;
    private LocalDateTime submissionDate;
    private String status;
}
