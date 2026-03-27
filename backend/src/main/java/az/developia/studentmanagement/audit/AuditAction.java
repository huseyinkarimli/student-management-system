package az.developia.studentmanagement.audit;

public enum AuditAction {

    // Student actions
    STUDENT_CREATED,
    STUDENT_UPDATED,
    STUDENT_DELETED,
    STUDENT_VIEWED,
    STUDENT_SEARCHED,

    // Course actions
    COURSE_CREATED,
    COURSE_UPDATED,
    COURSE_DELETED,
    COURSE_VIEWED,

    // Enrollment actions
    ENROLLMENT_CREATED,
    ENROLLMENT_DELETED,
    GRADE_UPDATED,

    // Assignment actions
    ASSIGNMENT_CREATED,
    ASSIGNMENT_UPDATED,
    ASSIGNMENT_DELETED,
    ASSIGNMENT_VIEWED,

    // Submission actions
    SUBMISSION_CREATED,
    SUBMISSION_GRADED,

    // Attendance actions
    ATTENDANCE_RECORDED,
    ATTENDANCE_UPDATED,
    ATTENDANCE_DELETED,

    // Authentication actions
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    REGISTER_SUCCESS,
    REGISTER_FAILED,
    TOKEN_REFRESH_SUCCESS,
    TOKEN_REFRESH_FAILED,
    LOGOUT,

    // Other events
    RATE_LIMIT_EXCEEDED
}
