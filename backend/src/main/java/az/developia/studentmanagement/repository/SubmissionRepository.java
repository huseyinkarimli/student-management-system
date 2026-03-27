package az.developia.studentmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import az.developia.studentmanagement.entity.SubmissionEntity;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {

    List<SubmissionEntity> findByAssignmentId(Long assignmentId);

    List<SubmissionEntity> findByStudentId(Long studentId);

    Optional<SubmissionEntity> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @Query("SELECT COUNT(s) FROM SubmissionEntity s WHERE s.assignment.id = :assignmentId")
    Long countByAssignmentId(@Param("assignmentId") Long assignmentId);
}
