package com.constructionhub.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Entity class representing a user in the system.
 * 
 * EN: This class defines the structure and properties of a user entity,
 * including authentication details, personal information, and security roles.
 * It implements UserDetails for Spring Security integration.
 * 
 * PT: Esta classe define a estrutura e propriedades de uma entidade usuário,
 * incluindo detalhes de autenticação, informações pessoais e funções de segurança.
 * Implementa UserDetails para integração com Spring Security.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username", name = "uk_user_username"),
    @UniqueConstraint(columnNames = "email", name = "uk_user_email")
})
// Se for usar auditoria JPA, extender AuditableBaseEntity
// public class UserEntity extends AuditableBaseEntity implements UserDetails {
public class UserEntity implements UserDetails { // Implementa UserDetails para integração com Spring Security

    /**
     * Unique identifier for the user.
     * 
     * EN: UUID used as the primary key for the user record.
     * PT: UUID usado como chave primária para o registro do usuário.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * User's login username.
     * 
     * EN: The unique username used for authentication. Required field.
     * PT: O nome de usuário único usado para autenticação. Campo obrigatório.
     */
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    /**
     * User's password (encrypted).
     * 
     * EN: The encrypted password used for authentication. Required field.
     * PT: A senha criptografada usada para autenticação. Campo obrigatório.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User's email address.
     * 
     * EN: The unique email address associated with the user. Required field.
     * PT: O endereço de email único associado ao usuário. Campo obrigatório.
     */
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    /**
     * User's first name.
     * 
     * EN: The first name of the user.
     * PT: O primeiro nome do usuário.
     */
    @Column(name = "first_name", length = 50)
    private String firstName;

    /**
     * User's last name.
     * 
     * EN: The last name of the user.
     * PT: O sobrenome do usuário.
     */
    @Column(name = "last_name", length = 50)
    private String lastName;

    /**
     * Flag indicating if the user account is enabled.
     * 
     * EN: Indicates whether the user account is currently active and enabled.
     * PT: Indica se a conta do usuário está atualmente ativa e habilitada.
     */
    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Flag indicating if the user account is not expired.
     * 
     * EN: Indicates whether the user account has not expired.
     * PT: Indica se a conta do usuário não expirou.
     */
    @Builder.Default
    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    /**
     * Flag indicating if the user account is not locked.
     * 
     * EN: Indicates whether the user account is not locked due to security reasons.
     * PT: Indica se a conta do usuário não está bloqueada por motivos de segurança.
     */
    @Builder.Default
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    /**
     * Flag indicating if the user credentials are not expired.
     * 
     * EN: Indicates whether the user's credentials (password) have not expired.
     * PT: Indica se as credenciais do usuário (senha) não expiraram.
     */
    @Builder.Default
    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    /**
     * Set of roles assigned to the user.
     * 
     * EN: The security roles assigned to this user, defining their access permissions.
     * PT: As funções de segurança atribuídas a este usuário, definindo suas permissões de acesso.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_userroles_user")),
            inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_userroles_role")))
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();

    /**
     * Timestamp of when the user record was created.
     * 
     * EN: Automatically generated timestamp when the record is first created.
     * PT: Timestamp gerado automaticamente quando o registro é criado pela primeira vez.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of when the user record was last updated.
     * 
     * EN: Automatically updated timestamp when the record is modified.
     * PT: Timestamp atualizado automaticamente quando o registro é modificado.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sets creation timestamp before persisting the entity.
     * 
     * EN: Automatically sets the creation and update timestamps before the entity is saved.
     * PT: Define automaticamente os timestamps de criação e atualização antes que a entidade seja salva.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the update timestamp before updating the entity.
     * 
     * EN: Automatically updates the update timestamp before the entity is updated.
     * PT: Atualiza automaticamente o timestamp de atualização antes que a entidade seja atualizada.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the authorities granted to the user.
     * 
     * EN: Returns the authorities (roles and permissions) granted to the user for authentication.
     * PT: Retorna as autoridades (funções e permissões) concedidas ao usuário para autenticação.
     * 
     * @return Collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Coleta as roles e as permissões associadas a essas roles
        // EN: Collects roles and permissions associated with those roles
        Set<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        // Adiciona permissões individuais (se houver um campo direto de permissões no usuário)
        // ou permissões das roles
        // EN: Adds individual permissions from roles
        roles.forEach(role -> role.getPermissions().forEach(permission ->
            authorities.add(new SimpleGrantedAuthority(permission.getName()))
        ));
        return authorities;
    }

    /**
     * Indicates whether the user is enabled.
     * 
     * EN: Returns whether the user account is currently enabled.
     * PT: Retorna se a conta do usuário está atualmente habilitada.
     * 
     * @return true if the user is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Indicates whether the user account has not expired.
     * 
     * EN: Returns whether the user account is non-expired.
     * PT: Retorna se a conta do usuário não expirou.
     * 
     * @return true if the account is non-expired, false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    /**
     * Indicates whether the user account is not locked.
     * 
     * EN: Returns whether the user account is not locked.
     * PT: Retorna se a conta do usuário não está bloqueada.
     * 
     * @return true if the account is not locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * Indicates whether the user's credentials have not expired.
     * 
     * EN: Returns whether the user's credentials (password) are non-expired.
     * PT: Retorna se as credenciais do usuário (senha) não expiraram.
     * 
     * @return true if the credentials are non-expired, false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    /**
     * Compares this user entity with another object for equality.
     * 
     * EN: Two user entities are considered equal if they have the same ID.
     * PT: Duas entidades de usuário são consideradas iguais se tiverem o mesmo ID.
     * 
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Generates a hash code for this user entity.
     * 
     * EN: The hash code is based on the user's ID.
     * PT: O código hash é baseado no ID do usuário.
     * 
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
