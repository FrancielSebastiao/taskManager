-- V7__create_project_activities_and_files.sql

CREATE TABLE project_activities (
    id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36),
    text TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_activity_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_activity_type CHECK (type IN ('TASK_COMPLETED', 'FILE_ADDED', 'COMMENT_ADDED', 
        'DEADLINE_UPDATED', 'TASK_ASSIGNED', 'MEMBER_ADDED', 'STATUS_CHANGED', 'PROGRESS_UPDATED'))
);

CREATE TABLE project_files (
    id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    uploaded_by VARCHAR(36),
    original_name VARCHAR(255) NOT NULL,
    s3_key VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    extension VARCHAR(20) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_file_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_file_user FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_project_activities_project ON project_activities(project_id);
CREATE INDEX idx_project_activities_user ON project_activities(user_id);
CREATE INDEX idx_project_activities_type ON project_activities(type);
CREATE INDEX idx_project_activities_created_at ON project_activities(created_at);

CREATE INDEX idx_project_files_project ON project_files(project_id);
CREATE INDEX idx_project_files_uploaded_by ON project_files(uploaded_by);
CREATE INDEX idx_project_files_uploaded_at ON project_files(uploaded_at);
CREATE INDEX idx_project_files_extension ON project_files(extension);
