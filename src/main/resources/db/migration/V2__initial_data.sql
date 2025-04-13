-- Adicionar roles básicas (UUIDs explícitos)
INSERT INTO roles (id, name, description)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'ADMIN', 'Administrador'),
    ('22222222-2222-2222-2222-222222222222', 'USER', 'Usuário comum');

-- Adicionar permissões (UUIDs explícitos)
INSERT INTO permissions (id, resource, action, description)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'user', 'read', 'Ver usuários'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'user', 'write', 'Criar/alterar usuários'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'user', 'delete', 'Excluir usuários'),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'client', 'read', 'Ver aplicações cliente'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'client', 'write', 'Criar/alterar aplicações cliente'),
    ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'client', 'delete', 'Excluir aplicações cliente');

-- Associar permissões ao ADMIN
INSERT INTO roles_permissions (role_id, permission_id)
SELECT
    '11111111-1111-1111-1111-111111111111',
    id
FROM permissions;

-- Associar permissões ao USER
INSERT INTO roles_permissions (role_id, permission_id)
SELECT
    '22222222-2222-2222-2222-222222222222',
    id
FROM permissions
WHERE action = 'read';

-- Criar usuário admin inicial (senha: admin123)
INSERT INTO users (
    id,
    username,
    email,
    password,
    first_name,
    last_name,
    is_enabled,
    is_account_non_expired,
    is_account_non_locked,
    is_credentials_non_expired
) VALUES (
    '99999999-9999-9999-9999-999999999999',
    'admin',
    'admin@example.com',
    '$2a$10$NvWwLuQS9KoCOfb6hXFN5eMgYCsizdiN/v6bGI9mQQtTbklhvdC0W',
    'Admin',
    'System',
    true,
    true,
    true,
    true
);

-- Associar admin ao role ADMIN
INSERT INTO users_roles (user_id, role_id)
VALUES ('99999999-9999-9999-9999-999999999999', '11111111-1111-1111-1111-111111111111');