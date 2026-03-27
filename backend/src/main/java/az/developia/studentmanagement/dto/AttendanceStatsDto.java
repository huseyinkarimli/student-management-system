package az.developia.studentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceStatsDto {

    private Long totalClasses;
    private Long presentCount;
    private Long absentCount;
    private Long lateCount;
    private Long excusedCount;
    private Double attendancePercentage;
}
