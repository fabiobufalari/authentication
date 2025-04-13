package com.constructionhub.authentication.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String resource;

    @Column(nullable = false)
    private String action;

    @Column
    private String description;

    // Método utilitário para criar uma string no formato "resource:action"
    public String getPermissionString() {
        return resource + ":" + action;
    }
}