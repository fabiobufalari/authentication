package com.constructionhub.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects; // Importar
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name", name = "uk_role_name")
})
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name; // e.g., ROLE_ADMIN, ROLE_USER

    @Column(name = "description", length = 255)
    private String description; // Descrição da role

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_roleperm_role")),
            inverseJoinColumns = @JoinColumn(name = "permission_id", foreignKey = @ForeignKey(name = "fk_roleperm_permission")))
    @Builder.Default
    private Set<PermissionEntity> permissions = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleEntity that = (RoleEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}