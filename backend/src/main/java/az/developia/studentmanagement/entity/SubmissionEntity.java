package az.developia.studentmanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "submissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"assignment_id", "student_id"})
})
public class SubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private AssignmentEntity assignment;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @Column(name = "submission_date", nullable = false, updatable = false)
    private LocalDateTime submissionDate;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Min(value = 0, message = "Score cannot be less than 0")
    @Max(value = 100, message = "Score cannot be greater than 100")
    private Integer score;

    @Column(length = 2000)
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @PrePersist
    protected void onCreate() {
        this.submissionDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = SubmissionStatus.PENDING;
        }
    }

    public enum SubmissionStatus {
        PENDING,
        SUBMITTED,
        GRADED
    }
}
