-- V2__create_roles_and_privileges.sql

CREATE TABLE privileges (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_privileges_name UNIQUE (name)
);

CREATE TABLE roles (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE users_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE roles_privileges (
    role_id VARCHAR(36) NOT NULL,
    privilege_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (role_id, privilege_id),
    CONSTRAINT fk_roles_privileges_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_roles_privileges_privilege FOREIGN KEY (privilege_id) REFERENCES privileges(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_users_roles_user ON users_roles(user_id);
CREATE INDEX idx_users_roles_role ON users_roles(role_id);
CREATE INDEX idx_roles_privileges_role ON roles_privileges(role_id);
CREATE INDEX idx_roles_privileges_privilege ON roles_privileges(privilege_id);