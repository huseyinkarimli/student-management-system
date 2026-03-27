package az.developia.studentmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.developia.studentmanagement.dto.UserDto;
import az.developia.studentmanagement.dto.RoleDto;
import az.developia.studentmanagement.request.UpdateRolesRequest;
import az.developia.studentmanagement.service.AdminUserService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.findAllUsers());
    }

    @GetMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getRolesByUserId(id));
    }

    @PutMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> updateUserRoles(@PathVariable Long id, @RequestBody UpdateRolesRequest request) {
        String currentUsername = getCurrentUsername();
        adminUserService.updateRoles(id, request.getRoles(), currentUsername);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(adminUserService.findAllRoles());
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> getTeachers() {
        return ResponseEntity.ok(adminUserService.findUsersByRole("ROLE_TEACHER"));
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
