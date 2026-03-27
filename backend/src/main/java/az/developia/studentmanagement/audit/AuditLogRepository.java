package az.developia.studentmanagement.audit;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLogDocument, String> {

    List<AuditLogDocument> findByAffectedStudentId(Long affectedStudentId, Sort sort);

    List<AuditLogDocument> findByUserId(Long userId, Sort sort);

    List<AuditLogDocument> findByTimestampBetween(Instant start, Instant end, Sort sort);

    List<AuditLogDocument> findByAction(AuditAction action, Sort sort);
}
