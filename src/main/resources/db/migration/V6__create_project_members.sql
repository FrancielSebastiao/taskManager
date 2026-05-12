-- V6__create_project_members.sql

CREATE TABLE project_members (
    id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_project_member UNIQUE (project_id, user_id),
    CONSTRAINT fk_member_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_role FOREIGN KEY (role_id) REFERENCES project_member_roles(id)
);

-- Indexes
CREATE INDEX idx_project_members_project ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);
CREATE INDEX idx_project_members_role ON project_members(role_id);
CREATE INDEX idx_project_members_joined_at ON project_members(joined_at);
