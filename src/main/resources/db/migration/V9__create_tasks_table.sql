-- V9__create_tasks_table.sql

CREATE TABLE tasks (
    id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    priority VARCHAR(10) NOT NULL DEFAULT 'MÉDIA',
    due_date DATE NOT NULL,
    completed_at DATE,
    created_by VARCHAR(36),
    estimated_hours DECIMAL(5, 1),
    project_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    progress_percent INT NOT NULL DEFAULT 0,
    last_progress_updated_by_id VARCHAR(36),
    last_progress_updated_at TIMESTAMP,
    category_id VARCHAR(36),
    parent_task_id VARCHAR(36),
    PRIMARY KEY (id),
    CONSTRAINT uk_task_project_title UNIQUE (project_id, title),
    CONSTRAINT fk_task_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_category FOREIGN KEY (category_id) REFERENCES task_categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_task_parent FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT chk_task_status CHECK (status IN ('PENDENTE', 'EM_PROGRESSO', 'ESPERANDO_APROVAÇÃO', 'COMPLETA', 'BLOQUEADA')),
    CONSTRAINT chk_task_priority CHECK (priority IN ('BAIXA', 'MÉDIA', 'ALTA', 'CRÍTICA')),
    CONSTRAINT chk_task_progress CHECK (progress_percent >= 0 AND progress_percent <= 100)
);

CREATE TABLE task_assignees (
    task_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (task_id, user_id),
    CONSTRAINT fk_task_assignees_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_assignees_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_by ON tasks(created_by);
CREATE INDEX idx_tasks_category ON tasks(category_id);
CREATE INDEX idx_tasks_parent ON tasks(parent_task_id);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
CREATE INDEX idx_tasks_progress ON tasks(progress_percent);

CREATE INDEX idx_task_assignees_task ON task_assignees(task_id);
CREATE INDEX idx_task_assignees_user ON task_assignees(user_id);
