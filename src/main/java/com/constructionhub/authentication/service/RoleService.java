package com.constructionhub.authentication.service;

import com.constructionhub.authentication.model.Permission;
import com.constructionhub.authentication.model.Role;
import com.constructionhub.authentication.repository.PermissionRepository;
import com.constructionhub.authentication.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public Role createRole(Role role) {
        Set<Permission> resolvedPermissions = new HashSet<>();
        if (role.getPermissions() != null) {
            for (Permission p : role.getPermissions()) {
                Permission found = permissionRepository.findById(p.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Permissão não encontrada: ID " + p.getId()));
                resolvedPermissions.add(found);
            }
        }
        role.setPermissions(resolvedPermissions);
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}
