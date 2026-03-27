package az.developia.studentmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import az.developia.studentmanagement.entity.StudentEntity;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {


    List<StudentEntity> findByUserId(Long userId);

    Optional<StudentEntity> findByIdAndUserId(Long id, Long userId);

    @Query(value = "SELECT * FROM students WHERE user_id = ?1 LIMIT ?2, ?3", nativeQuery = true)
    List<StudentEntity> findAllLimitByUserId(Long userId, Integer offset, Integer limit);

    List<StudentEntity> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    List<StudentEntity> findByUserIdAndSurnameContainingIgnoreCase(Long userId, String surname);

    Optional<StudentEntity> findByUserIdAndEmail(Long userId, String email);

    List<StudentEntity> findByUserIdAndAgeBetween(Long userId, Integer minAge, Integer maxAge);

    @Query("SELECT s FROM StudentEntity s WHERE s.userId = :userId AND "
            + "(LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR "
            + "LOWER(s.surname) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<StudentEntity> searchByUserIdAndNameOrSurname(@Param("userId") Long userId,
                                                       @Param("search") String search);

    Long countByUserId(Long userId);
}