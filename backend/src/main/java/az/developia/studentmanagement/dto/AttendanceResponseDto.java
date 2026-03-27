package az.developia.studentmanagement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import az.developia.studentmanagement.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResponseDto {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private LocalDate date;
    private AttendanceStatus status;
    private String remarks;
    private Long recordedBy;
    private String recordedByUsername;
    private LocalDateTime recordedAt;
}
