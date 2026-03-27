package az.developia.studentmanagement.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import az.developia.studentmanagement.entity.AttendanceEntity;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

    List<AttendanceEntity> findByCourseIdAndDate(Long courseId, LocalDate date);

    List<AttendanceEntity> findByStudentId(Long studentId);

    Optional<AttendanceEntity> findByStudentIdAndCourseIdAndDate(Long studentId, Long courseId, LocalDate date);

    List<AttendanceEntity> findByCourseIdAndStudentId(Long courseId, Long studentId);

    List<AttendanceEntity> findByCourseId(Long courseId);
}
