package az.developia.studentmanagement.config;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import az.developia.studentmanagement.entity.RoleEntity;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.repository.RoleRepository;
import az.developia.studentmanagement.repository.UserRepository;

@Component
@Order(1)
public class DatabaseSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:Admin@1234}")
    private String adminPassword;

    @Value("${app.seeder.seed-test-data:false}")
    private boolean seedTestData;

    public DatabaseSeeder(RoleRepository roleRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            // ── PHASE 1: Seed all roles ────────────────────────────────────────────
            RoleEntity roleGetStudents   = seedRole("ROLE_GET_STUDENTS");
            RoleEntity roleGetStudent    = seedRole("ROLE_GET_STUDENT");
            RoleEntity roleAddStudent    = seedRole("ROLE_ADD_STUDENT");
            RoleEntity roleUpdateStudent = seedRole("ROLE_UPDATE_STUDENT");
            RoleEntity roleDeleteStudent = seedRole("ROLE_DELETE_STUDENT");
            RoleEntity roleSearchStudent = seedRole("ROLE_SEARCH_STUDENT");
            RoleEntity roleGet           = seedRole("ROLE_GET");
            RoleEntity roleAdd           = seedRole("ROLE_ADD");
            RoleEntity roleUpdate        = seedRole("ROLE_UPDATE");
            RoleEntity roleDelete        = seedRole("ROLE_DELETE");
            RoleEntity roleAdmin         = seedRole("ROLE_ADMIN");
            RoleEntity roleUser          = seedRole("ROLE_USER");
            RoleEntity roleTeacher       = seedRole("ROLE_TEACHER");
            RoleEntity roleStudent       = seedRole("ROLE_STUDENT");

            long totalRoles = roleRepository.count();

            // ── PHASE 2: Seed admin user ───────────────────────────────────────────
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                UserEntity admin = new UserEntity();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRoles(Set.of(
                        roleGetStudents, roleGetStudent, roleAddStudent,
                        roleUpdateStudent, roleDeleteStudent, roleSearchStudent,
                        roleGet, roleAdd, roleUpdate, roleDelete,
                        roleAdmin, roleUser, roleTeacher, roleStudent
                ));
                userRepository.save(admin);
                logger.info("Admin user '{}' created.", adminUsername);
            } else {
                logger.debug("Admin user '{}' already exists — skipping.", adminUsername);
            }

            // ── PHASE 3: Seed test users (controlled by app.seeder.seed-test-data) ─
            if (seedTestData) {
                seedUserIfAbsent(
                        "teacher1",
                        "Teacher@1234",
                        Set.of(roleTeacher, roleGetStudents, roleGetStudent, roleGet)
                );
                seedUserIfAbsent(
                        "student1",
                        "Student@1234",
                        Set.of(roleStudent, roleGet, roleGetStudents, roleGetStudent)
                );
            }

            // ── PHASE 4: Log summary ───────────────────────────────────────────────
            logger.info("DatabaseSeeder completed. Roles: {}, Admin ensured, Test data: {}",
                    totalRoles, seedTestData);

        } catch (Exception e) {
            logger.error("DatabaseSeeder encountered an error during startup seeding: {}", e.getMessage(), e);
        }
    }

    private RoleEntity seedRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity();
                    role.setName(name);
                    return roleRepository.save(role);
                });
    }

    private void seedUserIfAbsent(String username, String rawPassword, Set<RoleEntity> roles) {
        if (userRepository.findByUsername(username).isEmpty()) {
            UserEntity user = new UserEntity();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRoles(roles);
            userRepository.save(user);
            logger.info("Test user '{}' created.", username);
        } else {
            logger.debug("Test user '{}' already exists — skipping.", username);
        }
    }
}
