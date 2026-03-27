package az.developia.studentmanagement.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentRequestDto {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Assignment title cannot be blank")
    private String title;

    private String description;

    private LocalDateTime dueDate;

    @NotNull(message = "Max score is required")
    @Min(value = 1, message = "Max score must be at least 1")
    private Integer maxScore;
}
