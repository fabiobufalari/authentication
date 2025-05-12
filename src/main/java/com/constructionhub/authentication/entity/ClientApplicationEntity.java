package com.constructionhub.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Para campos de auditoria
import java.util.HashSet;
import java.util.Objects; // Importar
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "client_applications", uniqueConstraints = {
    @UniqueConstraint(columnNames = "clientId", name = "uk_clientapp_clientid"),
    @UniqueConstraint(columnNames = "applicationName", name = "uk_clientapp_appname")
})
public class ClientApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "client_id", unique = true, nullable = false, length = 100) // client_id textual
    private String clientId;

    @Column(name = "client_secret", nullable = false) // Hashed
    private String clientSecret;

    @Column(name = "application_name", unique = true, nullable = false, length = 100)
    private String applicationName;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY) // Relação opcional com um usuário proprietário
    @JoinColumn(name = "owner_user_id", foreignKey = @ForeignKey(name = "fk_clientapp_owner"))
    private UserEntity owner; // Proprietário da aplicação (um usuário)

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_application_scopes",
                     joinColumns = @JoinColumn(name = "client_app_id", foreignKey = @ForeignKey(name = "fk_clientappscopes_clientapp")))
    @Column(name = "scope", length = 50)
    @Builder.Default
    private Set<String> scopes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_application_grant_types",
                     joinColumns = @JoinColumn(name = "client_app_id", foreignKey = @ForeignKey(name = "fk_clientappgrants_clientapp")))
    @Column(name = "grant_type", length = 50)
    @Builder.Default
    private Set<String> authorizedGrantTypes = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "client_application_redirect_uris",
                     joinColumns = @JoinColumn(name = "client_app_id", foreignKey = @ForeignKey(name = "fk_clientappuris_clientapp")))
    @Column(name = "redirect_uri", length = 500)
    @Builder.Default
    private Set<String> redirectUris = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "client_application_allowed_origins",
            joinColumns = @JoinColumn(name = "client_app_id", foreignKey = @ForeignKey(name = "fk_clientapporigins_clientapp")))
    @Column(name = "allowed_origin", length = 255)
    @Builder.Default
    private Set<String> allowedOrigins = new HashSet<>();


    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientApplicationEntity that = (ClientApplicationEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}