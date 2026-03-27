package az.developia.studentmanagement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import az.developia.studentmanagement.audit.AuditAction;
import az.developia.studentmanagement.audit.AuditLogService;
import az.developia.studentmanagement.dto.StudentRequestDto;
import az.developia.studentmanagement.dto.StudentResponseDto;
import az.developia.studentmanagement.dto.StudentUpdateRequest;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.exception.OurValidationException;
import az.developia.studentmanagement.repository.UserRepository;
import az.developia.studentmanagement.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/students")
@Validated
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    private Logger logger = LoggerFactory.getLogger(StudentController.class);

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("İstifadəçi tapılmadı"));
        return user.getId();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_GET_STUDENTS')")
    public List<StudentResponseDto> findAllStudents() {
        logger.info("Bütün tələbələr çəkilir");
        return studentService.findAllByUserId(getCurrentUserId());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_SEARCH_STUDENT')")
    public List<StudentResponseDto> searchStudents(@RequestParam(name = "query") String query) {
        logger.info("Tələbə axtarışı: {}", query);
        List<StudentResponseDto> results = studentService.searchByUserIdAndNameOrSurname(getCurrentUserId(), query);
        Map<String, Object> details = new HashMap<>();
        details.put("query", query);
        details.put("resultCount", results.size());
        auditLogService.log(AuditAction.STUDENT_SEARCHED, details, null);
        return results;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_GET_STUDENT')")
    public StudentResponseDto findById(@PathVariable @Min(1) Long id) {
        try {
            StudentResponseDto student = studentService.findByIdAndUserId(id, getCurrentUserId());
            auditLogService.log(AuditAction.STUDENT_VIEWED, new HashMap<>(), id);
            return student;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADD_STUDENT')")
    public void createStudent(@Valid @RequestBody StudentRequestDto request, BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }
        studentService.save(request, getCurrentUserId());
        Map<String, Object> details = new HashMap<>();
        details.put("name", request.getName());
        details.put("surname", request.getSurname());
        details.put("email", request.getEmail());
        details.put("age", request.getAge());
        auditLogService.log(AuditAction.STUDENT_CREATED, details, null);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE_STUDENT')")
    public void updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request, BindingResult br) {
        if (br.hasErrors()) {
            throw new OurValidationException(br);
        }

        if (!id.equals(request.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL-dəki ID ilə request-dəki ID uyğun deyil");
        }

        try {
            StudentResponseDto oldStudent = studentService.findByIdAndUserId(id, getCurrentUserId());
            studentService.update(id, request, getCurrentUserId());
            Map<String, Object> details = new HashMap<>();
            Map<String, Object> oldValues = new HashMap<>();
            oldValues.put("name", oldStudent.getName());
            oldValues.put("surname", oldStudent.getSurname());
            oldValues.put("email", oldStudent.getEmail());
            oldValues.put("age", oldStudent.getAge());
            Map<String, Object> newValues = new HashMap<>();
            newValues.put("name", request.getName());
            newValues.put("surname", request.getSurname());
            newValues.put("email", request.getEmail());
            newValues.put("age", request.getAge());
            details.put("oldValues", oldValues);
            details.put("newValues", newValues);
            auditLogService.log(AuditAction.STUDENT_UPDATED, details, id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_DELETE_STUDENT')")
    public void deleteStudent(@PathVariable @Min(1) Long id) {
        try {
            studentService.deleteByIdAndUserId(id, getCurrentUserId());
            auditLogService.log(AuditAction.STUDENT_DELETED, new HashMap<>(), id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    @GetMapping("/page")
    @PreAuthorize("hasAuthority('ROLE_GET_STUDENTS')")
    public List<StudentResponseDto> findStudentsWithPagination(
            @RequestParam @Min(0) Integer begin,
            @RequestParam @Positive Integer length) {
        return studentService.findAllLimitByUserId(getCurrentUserId(), begin, length);
    }


    @GetMapping("/filter/age")
    @PreAuthorize("hasAuthority('ROLE_GET_STUDENTS')")
    public List<StudentResponseDto> filterByAge(
            @RequestParam @Min(18) Integer minAge,
            @RequestParam @Min(18) Integer maxAge) {
        return studentService.findAllByUserIdAndAgeBetween(getCurrentUserId(), minAge, maxAge);
    }


    @GetMapping("/search/name")
    @PreAuthorize("hasAuthority('ROLE_SEARCH_STUDENT')")
    public List<StudentResponseDto> searchByName(@RequestParam String name) {
        List<StudentResponseDto> results = studentService.findAllByUserIdSearch(getCurrentUserId(), name);
        Map<String, Object> details = new HashMap<>();
        details.put("query", name);
        details.put("searchType", "name");
        details.put("resultCount", results.size());
        auditLogService.log(AuditAction.STUDENT_SEARCHED, details, null);
        return results;
    }


    @GetMapping("/search/surname")
    @PreAuthorize("hasAuthority('ROLE_SEARCH_STUDENT')")
    public List<StudentResponseDto> searchBySurname(@RequestParam String surname) {
        List<StudentResponseDto> results = studentService.findAllByUserIdSurnameSearch(getCurrentUserId(), surname);
        Map<String, Object> details = new HashMap<>();
        details.put("query", surname);
        details.put("searchType", "surname");
        details.put("resultCount", results.size());
        auditLogService.log(AuditAction.STUDENT_SEARCHED, details, null);
        return results;
    }


    @GetMapping("/count")
    @PreAuthorize("hasAuthority('ROLE_GET_STUDENTS')")
    public Long getStudentCount() {
        return studentService.countByUserId(getCurrentUserId());
    }
}