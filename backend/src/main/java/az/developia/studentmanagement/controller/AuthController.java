package az.developia.studentmanagement.controller;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.developia.studentmanagement.audit.AuditAction;
import az.developia.studentmanagement.audit.AuditLogService;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.repository.RoleRepository;
import az.developia.studentmanagement.repository.UserRepository;
import az.developia.studentmanagement.request.AuthRequest;
import az.developia.studentmanagement.request.RegisterRequest;
import az.developia.studentmanagement.request.TokenRequest;
import az.developia.studentmanagement.response.AuthResponse;
import az.developia.studentmanagement.service.UserDetailsServiceImpl;
import az.developia.studentmanagement.utils.JwtUtil;
import az.developia.studentmanagement.utils.RefreshTokenUtil;
import az.developia.studentmanagement.entity.RoleEntity;

@RestController
@RequestMapping("/apis")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private RefreshTokenUtil refreshTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            Map<String, Object> details = new HashMap<>();
            details.put("reason", "Username already exists");
            details.put("username", request.getUsername());
            auditLogService.log(AuditAction.REGISTER_FAILED, details);
            return ResponseEntity.badRequest().body("Username already exists");
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(getDefaultRolesForNewUser());
        userRepository.save(user);
        Map<String, Object> details = new HashMap<>();
        details.put("username", request.getUsername());
        auditLogService.log(AuditAction.REGISTER_SUCCESS, details);
        return ResponseEntity.ok("User registered successfully");
    }

    /**
     * Returns minimal default roles for newly registered users.
     * Never includes ROLE_ADMIN. Prefers ROLE_USER; falls back to ROLE_GET and ROLE_GET_STUDENTS.
     */
    private Set<RoleEntity> getDefaultRolesForNewUser() {
        Set<RoleEntity> roles = new HashSet<>();
        roleRepository.findByName("ROLE_USER").ifPresent(roles::add);
        roleRepository.findByName("ROLE_GET").ifPresent(roles::add);
        roleRepository.findByName("ROLE_GET_STUDENTS").ifPresent(roles::add);
        if (roles.isEmpty()) {
            roleRepository.findByName("ROLE_GET").ifPresent(roles::add);
        }
        return roles;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            Map<String, Object> details = new HashMap<>();
            details.put("reason", "Incorrect username or password");
            details.put("username", authRequest.getUsername());
            auditLogService.log(AuditAction.LOGIN_FAILED, details);
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        final String refreshToken = refreshTokenUtil.generateRefreshToken(userDetails);

        Map<String, Object> details = new HashMap<>();
        details.put("username", authRequest.getUsername());
        auditLogService.log(AuditAction.LOGIN_SUCCESS, details);

        return ResponseEntity.ok(new AuthResponse(jwt, refreshToken));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRequest tokenRequest) {
        try {
            String refreshToken = tokenRequest.getRefreshToken();
            String username = refreshTokenUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (refreshTokenUtil.validateToken(refreshToken, userDetails)) {
                final String newAccessToken = jwtUtil.generateToken(userDetails);
                Map<String, Object> details = new HashMap<>();
                details.put("username", username);
                auditLogService.log(AuditAction.TOKEN_REFRESH_SUCCESS, details);
                return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
            } else {
                Map<String, Object> details = new HashMap<>();
                details.put("reason", "Invalid refresh token");
                details.put("username", username);
                auditLogService.log(AuditAction.TOKEN_REFRESH_FAILED, details);
                return ResponseEntity.status(403).body("Invalid refresh token");
            }
        } catch (Exception e) {
            Map<String, Object> details = new HashMap<>();
            details.put("reason", e.getMessage() != null ? e.getMessage() : "Token extraction or validation failed");
            auditLogService.log(AuditAction.TOKEN_REFRESH_FAILED, details);
            return ResponseEntity.status(403).body("Invalid refresh token");
        }
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_ADD')")
    public String addData() {
        return "add success";
    }

    @GetMapping("/get")
    @PreAuthorize("hasAuthority('ROLE_GET')")
    public String getData() {
        return "get success";
    }

    @GetMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public String updateData() {
        return "update success";
    }

    @GetMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public String deleteData() {
        return "delete success";
    }
}