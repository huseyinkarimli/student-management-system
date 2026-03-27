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
public class CourseResponseDto {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer credits;
    private Long teacherId;
    private String teacherName;
    private String schedule;
    private Long studentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
