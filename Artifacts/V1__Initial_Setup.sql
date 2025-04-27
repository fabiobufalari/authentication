-- V1__Initial_Setup.sql
-- Script para inicializar o schema básico de autenticação e usuários.

-- Habilita a extensão pgcrypto se não existir (necessária para gen_random_uuid())
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Conecta ao banco de dados correto (opcional, útil para execução via psql)
-- \c buildingDB;

-- Tabela: roles
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);
COMMENT ON TABLE roles IS 'Stores user roles like ROLE_ADMIN, ROLE_USER.';

-- Tabela: permissions (Opcional, se for usar permissões granulares no futuro)
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uk_permission_resource_action UNIQUE (resource, action)
);
COMMENT ON TABLE permissions IS 'Stores granular permissions (e.g., resource:action).';

-- Tabela: roles_permissions (Tabela de Junção)
CREATE TABLE IF NOT EXISTS roles_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);
COMMENT ON TABLE roles_permissions IS 'Associates permissions with roles.';

-- Tabela: users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    is_account_non_expired BOOLEAN NOT NULL DEFAULT true,
    is_account_non_locked BOOLEAN NOT NULL DEFAULT true,
    is_credentials_non_expired BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
COMMENT ON TABLE users IS 'Stores user account information.';

-- Tabela: users_roles (Tabela de Junção)
CREATE TABLE IF NOT EXISTS users_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
COMMENT ON TABLE users_roles IS 'Associates users with roles.';

-- Tabela: client_applications (Se usar autenticação client credentials)
CREATE TABLE IF NOT EXISTS client_applications (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    client_id VARCHAR(100) NOT NULL UNIQUE,
    client_secret VARCHAR(255) NOT NULL, -- Store hashed secrets in production
    redirect_uris TEXT,
    allowed_origins TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    owner_id UUID, -- FK para users, pode ser nulo se for um client do sistema
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL
);
COMMENT ON TABLE client_applications IS 'Stores information about client applications using the auth service.';


-- =========================================
-- INSERÇÃO DE DADOS INICIAIS
-- =========================================

DO $$
DECLARE
    admin_role_id UUID;
    user_role_id UUID;
    admin_user_id UUID;
    standard_user_id UUID;
    -- Hashes pré-gerados para senhas 'password' e 'system' (use Bcrypt!)
    -- Exemplo: use um gerador online ou um script Java simples para obter o hash real
    pwd_hash_password VARCHAR := '$2a$10$FhWfQ9rR2V.pL.mjn6MvMe3kFk2oJg9gV5Jb8X0UqJzY.YkL3yV1.'; -- Hash para 'password'
    pwd_hash_system VARCHAR := '$2a$10$jI7vjY6xGk8RzQ3j9L7xJ.Q5nF8tKzGzWzD8xY4wU6.kL3yV2M1.'; -- Hash para 'system'
BEGIN
    -- 1. Inserir Roles Básicas (somente se não existirem)
    INSERT INTO roles (id, name, description) VALUES (gen_random_uuid(), 'ROLE_ADMIN', 'Administrator Role') ON CONFLICT (name) DO NOTHING RETURNING id INTO admin_role_id;
    INSERT INTO roles (id, name, description) VALUES (gen_random_uuid(), 'ROLE_USER', 'Standard User Role') ON CONFLICT (name) DO NOTHING RETURNING id INTO user_role_id;
    -- Buscar IDs caso já existam
    IF admin_role_id IS NULL THEN SELECT id INTO admin_role_id FROM roles WHERE name = 'ROLE_ADMIN'; END IF;
    IF user_role_id IS NULL THEN SELECT id INTO user_role_id FROM roles WHERE name = 'ROLE_USER'; END IF;

    -- 2. Inserir Usuário Admin (somente se não existir)
    INSERT INTO users (id, username, email, password, first_name, last_name, created_by, updated_by)
    VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin', 'admin@example.com', pwd_hash_password, 'Admin', 'Sys', 'script', 'script')
    ON CONFLICT (username) DO NOTHING RETURNING id INTO admin_user_id;
    IF admin_user_id IS NULL THEN SELECT id INTO admin_user_id FROM users WHERE username = 'admin'; END IF;
    -- Associar ROLE_ADMIN ao admin
    IF admin_user_id IS NOT NULL AND admin_role_id IS NOT NULL THEN
        INSERT INTO users_roles (user_id, role_id) VALUES (admin_user_id, admin_role_id) ON CONFLICT DO NOTHING;
    END IF;

    -- 3. Inserir Usuário Padrão (somente se não existir)
    INSERT INTO users (id, username, email, password, first_name, last_name, created_by, updated_by)
    VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'user', 'user@example.com', pwd_hash_password, 'Standard', 'User', 'script', 'script')
    ON CONFLICT (username) DO NOTHING RETURNING id INTO standard_user_id;
    IF standard_user_id IS NULL THEN SELECT id INTO standard_user_id FROM users WHERE username = 'user'; END IF;
    -- Associar ROLE_USER ao user
    IF standard_user_id IS NOT NULL AND user_role_id IS NOT NULL THEN
        INSERT INTO users_roles (user_id, role_id) VALUES (standard_user_id, user_role_id) ON CONFLICT DO NOTHING;
    END IF;

    -- 4. Inserir Usuários Sistêmicos (Exemplos DEV - Adicione UAT, HOMOL, PROD conforme necessário)
    INSERT INTO users (id, username, email, password, first_name, last_name, created_by, updated_by) VALUES (gen_random_uuid(), 'system_payable_dev', 'system_payable_dev@system.local', pwd_hash_system, 'System', 'Payable DEV', 'script', 'script') ON CONFLICT (username) DO NOTHING;
    INSERT INTO users (id, username, email, password, first_name, last_name, created_by, updated_by) VALUES (gen_random_uuid(), 'system_receivable_dev', 'system_receivable_dev@system.local', pwd_hash_system, 'System', 'Receivable DEV', 'script', 'script') ON CONFLICT (username) DO NOTHING;
    INSERT INTO users (id, username, email, password, first_name, last_name, created_by, updated_by) VALUES (gen_random_uuid(), 'system_cashflow_dev', 'system_cashflow_dev@system.local', pwd_hash_system, 'System', 'Cashflow DEV', 'script', 'script') ON CONFLICT (username) DO NOTHING;
    INSERT INTO users (id, username, email, password, first_name, last_name, created_by, updated_by) VALUES (gen_random_uuid(), 'system_employee_dev', 'system_employee_dev@system.local', pwd_hash_system, 'System', 'Employee DEV', 'script', 'script') ON CONFLICT (username) DO NOTHING;
    INSERT INTO users (id, username, email, password, first_name, last_name, created_by, updated_by) VALUES (gen_random_uuid(), 'system_supplier_dev', 'system_supplier_dev@system.local', pwd_hash_system, 'System', 'Supplier DEV', 'script', 'script') ON CONFLICT (username) DO NOTHING;
    INSERT INTO users (id, username, email, password, first_name, last_name, created_by, updated_by) VALUES (gen_random_uuid(), 'system_project_dev', 'system_project_dev@system.local', pwd_hash_system, 'System', 'Project DEV', 'script', 'script') ON CONFLICT (username) DO NOTHING;
    -- Adicione aqui usuários para UAT, HOMOL, PROD...
    -- Exemplo UAT: INSERT INTO users (...) VALUES (gen_random_uuid(), 'system_payable_uat', ...) ON CONFLICT DO NOTHING;

END $$;

-- Adicionar índices para colunas frequentemente usadas em buscas
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);
CREATE INDEX IF NOT EXISTS idx_client_applications_client_id ON client_applications(client_id);

-- Exemplo de inserção de permissões (se for usar)
-- INSERT INTO permissions (id, resource, action, description) VALUES
-- (gen_random_uuid(), 'payable', 'read', 'Can read payables'),
-- (gen_random_uuid(), 'payable', 'create', 'Can create payables')
-- ON CONFLICT DO NOTHING;
-- INSERT INTO roles_permissions (role_id, permission_id) VALUES
-- ('uuid_da_role_admin', 'uuid_da_permissao_payable_read'),
-- ('uuid_da_role_admin', 'uuid_da_permissao_payable_create')
-- ON CONFLICT DO NOTHING;

COMMIT; -- Garante que as transações sejam salvas