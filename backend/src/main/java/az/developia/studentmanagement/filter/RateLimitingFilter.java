package az.developia.studentmanagement.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import az.developia.studentmanagement.audit.AuditAction;
import az.developia.studentmanagement.audit.AuditLogService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Autowired
    private AuditLogService auditLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (isRateLimitedPath(path, method)) {
            String clientIp = request.getRemoteAddr();
            String bucketKey = clientIp + ":" + path;

            Bucket bucket;
            if (path.equals("/apis/register")) {
                bucket = buckets.computeIfAbsent(bucketKey, this::createBucket);
            } else {
                bucket = buckets.computeIfAbsent(bucketKey, this::createLoginBucket);
            }

            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                Map<String, Object> details = new HashMap<>();
                details.put("reason", "Rate limit exceeded");
                details.put("endpoint", path);
                auditLogService.log(AuditAction.RATE_LIMIT_EXCEEDED, details);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Çox sorğu göndərdiniz. Bir az gözləyin.");
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isRateLimitedPath(String path, String method) {
        if (!"POST".equals(method)) {
            return false;
        }
        return path.equals("/apis/register")
                || path.equals("/apis/login")
                || path.equals("/apis/refresh-token");
    }

    // 5 requests per minute — used for /apis/register
    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, java.time.Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }

    // 10 requests per minute — used for /apis/login and /apis/refresh-token
    private Bucket createLoginBucket(String key) {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, java.time.Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }
}