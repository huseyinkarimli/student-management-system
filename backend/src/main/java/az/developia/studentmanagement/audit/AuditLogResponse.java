package az.developia.studentmanagement.audit;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private String id;
    private Instant timestamp;
    private Long userId;
    private String username;
    private AuditAction action;
    private Map<String, Object> details;
    private String ipAddress;
    private Long affectedStudentId;
}
