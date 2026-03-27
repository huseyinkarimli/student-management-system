package az.developia.studentmanagement.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import az.developia.studentmanagement.dto.SubmissionRequestDto;
import az.developia.studentmanagement.dto.SubmissionResponseDto;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.exception.OurValidationException;
import az.developia.studentmanagement.repository.UserRepository;
import az.developia.studentmanagement.service.SubmissionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:3000"})
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private UserRepository userRepository;

    private Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getId();
    }

    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public List<SubmissionResponseDto> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        logger.info("Fetching submissions for assignment: {}", assignmentId);
        return submissionService.findByAssignmentId(assignmentId);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public List<SubmissionResponseDto> getSubmissionsByStudent(@PathVariable Long studentId) {
        logger.info("Fetching submissions for student: {}", studentId);
        return submissionService.findByStudentId(studentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_STUDENT')")
    public SubmissionResponseDto submitAssignment(@Valid @RequestBody SubmissionRequestDto request, BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }

        logger.info("Creating submission for assignment {} by student {}", request.getAssignmentId(), request.getStudentId());
        return submissionService.submit(request);
    }

    @PutMapping("/{id}/grade")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
    public SubmissionResponseDto gradeSubmission(@PathVariable Long id, @RequestBody GradeSubmissionRequest request) {
        logger.info("Grading submission: {}", id);
        return submissionService.gradeSubmission(id, request.getScore(), request.getFeedback());
    }

    public static class GradeSubmissionRequest {
        private Integer score;
        private String feedback;

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }
    }
}
