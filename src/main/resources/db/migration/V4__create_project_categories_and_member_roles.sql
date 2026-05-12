-- V4__create_project_categories_and_member_roles.sql

CREATE TABLE project_categories (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_project_categories_name UNIQUE (name)
);

CREATE TABLE project_member_roles (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_project_member_roles_name UNIQUE (name)
);

-- Indexes
CREATE INDEX idx_project_categories_active ON project_categories(active);
CREATE INDEX idx_project_categories_name ON project_categories(name);

CREATE INDEX idx_project_member_roles_active ON project_member_roles(active);
CREATE INDEX idx_project_member_roles_name ON project_member_roles(name);
