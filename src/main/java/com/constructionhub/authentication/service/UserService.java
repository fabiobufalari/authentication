package com.constructionhub.authentication.service;

import com.constructionhub.authentication.model.Permission;
import com.constructionhub.authentication.model.Role;
import com.constructionhub.authentication.model.User;
import com.constructionhub.authentication.repository.PermissionRepository;
import com.constructionhub.authentication.repository.RoleRepository;
import com.constructionhub.authentication.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String password, List<String> roleNames, List<String> permissionNames) {
        List<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseGet(() -> roleRepository.save(new Role(roleName))))
                .collect(Collectors.toList());

        // As permissões devem ser adicionadas às roles, não diretamente ao usuário
        List<Permission> permissions = permissionNames.stream()
                .map(permName -> permissionRepository.findByName(permName)
                        .orElseGet(() -> permissionRepository.save(new Permission(permName))))
                .collect(Collectors.toList());

        // Adiciona as permissões à primeira role, por simplicidade
        if (!roles.isEmpty()) {
            roles.get(0).getPermissions().addAll(permissions);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(new HashSet<>(roles));

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
