package az.developia.studentmanagement.audit;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import az.developia.studentmanagement.repository.UserRepository;
import az.developia.studentmanagement.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;


@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Log an audit event with full context.
     */
    public void log(AuditAction action, Map<String, Object> details, Long affectedStudentId) {
        try {
            AuditLogDocument doc = AuditLogDocument.builder()
                    .timestamp(Instant.now())
                    .userId(getCurrentUserId())
                    .username(getCurrentUsername())
                    .action(action)
                    .details(details != null ? details : new HashMap<>())
                    .ipAddress(getClientIpAddress())
                    .affectedStudentId(affectedStudentId)
                    .build();

            auditLogRepository.save(doc);
        } catch (Exception e) {
            logger.warn("Failed to persist audit log for action {}: {}", action, e.getMessage());
        }
    }

    /**
     * Log an audit event without student context.
     */
    public void log(AuditAction action, Map<String, Object> details) {
        log(action, details, null);
    }

    /**
     * Log an audit event with minimal details.
     */
    public void log(AuditAction action) {
        log(action, new HashMap<>(), null);
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            UserEntity user = userRepository.findByUsername(auth.getName()).orElse(null);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            return auth.getName();
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            if (request == null) {
                return null;
            }
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
