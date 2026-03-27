package az.developia.studentmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionRequestDto {

    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    private String fileUrl;
}
