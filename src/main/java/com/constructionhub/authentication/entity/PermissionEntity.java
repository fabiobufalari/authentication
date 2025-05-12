package com.constructionhub.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects; // Importar
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name", name = "uk_permission_name")
})
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name; // e.g., SUPPLIERS_READ, PROJECTS_CREATE

    @Column(name = "description", length = 255)
    private String description; // Descrição da permissão

    // Adicionar campos `resource` e `action` se o nome da permissão não for suficiente
    // @Column(name = "resource_name", length = 100)
    // private String resource; // e.g., SUPPLIERS, PROJECTS

    // @Column(name = "action_name", length = 50)
    // private String action; // e.g., READ, CREATE, UPDATE, DELETE


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionEntity that = (PermissionEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}