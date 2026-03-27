package az.developia.studentmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import az.developia.studentmanagement.entity.CourseEntity;

public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    Optional<CourseEntity> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<CourseEntity> findByTeacherId(Long teacherId);
}
