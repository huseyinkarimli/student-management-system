package az.developia.studentmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import az.developia.studentmanagement.entity.AssignmentEntity;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {

    List<AssignmentEntity> findByCourseId(Long courseId);

    List<AssignmentEntity> findByCourseIdOrderByDueDateAsc(Long courseId);
}
