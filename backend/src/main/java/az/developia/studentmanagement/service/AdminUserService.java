package az.developia.studentmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import az.developia.studentmanagement.dto.RoleDto;
import az.developia.studentmanagement.dto.UserDto;
import az.developia.studentmanagement.entity.RoleEntity;
import az.developia.studentmanagement.entity.UserEntity;
import az.developia.studentmanagement.repository.RoleRepository;
import az.developia.studentmanagement.repository.UserRepository;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    public List<String> getRolesByUserId(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return user.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateRoles(Long userId, List<String> roleNames, String currentUsername) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getUsername().equals(currentUsername)) {
            boolean hadAdmin = user.getRoles().stream()
                    .anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
            boolean willHaveAdmin = roleNames != null && roleNames.contains("ROLE_ADMIN");
            if (hadAdmin && !willHaveAdmin) {
                throw new IllegalStateException("You cannot remove ROLE_ADMIN from your own account.");
            }
        }

        List<RoleEntity> newRoles = roleNames == null ? List.of() : roleNames.stream()
                .map(name -> roleRepository.findByName(name.trim()))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toList());

        user.setRoles(new java.util.HashSet<>(newRoles));
        userRepository.save(user);
    }

    public List<RoleDto> findAllRoles() {
        return roleRepository.findAll().stream()
                .map(r -> new RoleDto(r.getId(), r.getName()))
                .collect(Collectors.toList());
    }

    public List<UserDto> findUsersByRole(String roleName) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(roleName)))
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    private UserDto toUserDto(UserEntity user) {
        List<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());
        return new UserDto(user.getId(), user.getUsername(), roles);
    }
}
