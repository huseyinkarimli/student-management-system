package az.developia.studentmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import az.developia.studentmanagement.entity.EnrollmentEntity;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    List<EnrollmentEntity> findByStudentId(Long studentId);

    List<EnrollmentEntity> findByCourseId(Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<EnrollmentEntity> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
}
