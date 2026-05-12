-- V10__create_task_related_tables.sql

CREATE TABLE task_activities (
    id VARCHAR(36) NOT NULL,
    task_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36),
    text TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_taskactivity_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_taskactivity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_task_activity_type CHECK (type IN ('TASK_CREATED', 'PROGRESS_UPDATED', 'STATUS_CHANGED', 
        'COMMENT_ADDED', 'FILE_UPLOADED', 'PHOTO_UPLOADED', 'ASSIGNEE_ADDED', 'ASSIGNEE_REMOVED', 'DEADLINE_CHANGED'))
);

CREATE TABLE task_files (
    id VARCHAR(36) NOT NULL,
    task_id VARCHAR(36) NOT NULL,
    uploaded_by VARCHAR(36),
    original_name VARCHAR(255) NOT NULL,
    s3_key VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    extension VARCHAR(20) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_taskfile_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_taskfile_user FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE task_photos (
    id VARCHAR(36) NOT NULL,
    task_id VARCHAR(36) NOT NULL,
    uploaded_by VARCHAR(36) NOT NULL,
    s3_key VARCHAR(255) NOT NULL,
    extension VARCHAR(10) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    caption VARCHAR(255),
    uploaded_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_photo_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_photo_user FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE task_comments (
    id VARCHAR(36) NOT NULL,
    task_id VARCHAR(36) NOT NULL,
    author_id VARCHAR(36) NOT NULL,
    category VARCHAR(30) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    attachment_file_id VARCHAR(36),
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_file FOREIGN KEY (attachment_file_id) REFERENCES task_files(id) ON DELETE SET NULL,
    CONSTRAINT chk_comment_category CHECK (category IN ('MATERIAL_SHORTAGE', 'WEATHER_CONDITIONS', 
        'EQUIPMENT_FAILURE', 'WAITING_FOR_APPROVAL', 'OTHER'))
);

-- Indexes
CREATE INDEX idx_task_activities_task ON task_activities(task_id);
CREATE INDEX idx_task_activities_user ON task_activities(user_id);
CREATE INDEX idx_task_activities_type ON task_activities(type);
CREATE INDEX idx_task_activities_created_at ON task_activities(created_at);

CREATE INDEX idx_task_files_task ON task_files(task_id);
CREATE INDEX idx_task_files_uploaded_by ON task_files(uploaded_by);
CREATE INDEX idx_task_files_uploaded_at ON task_files(uploaded_at);

CREATE INDEX idx_task_photos_task ON task_photos(task_id);
CREATE INDEX idx_task_photos_uploaded_by ON task_photos(uploaded_by);
CREATE INDEX idx_task_photos_uploaded_at ON task_photos(uploaded_at);

CREATE INDEX idx_task_comments_task ON task_comments(task_id);
CREATE INDEX idx_task_comments_author ON task_comments(author_id);
CREATE INDEX idx_task_comments_category ON task_comments(category);
CREATE INDEX idx_task_comments_created_at ON task_comments(created_at);
CREATE INDEX idx_task_comments_attachment ON task_comments(attachment_file_id);
