-- V5__create_projects_table.sql

CREATE TABLE projects (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    deadline DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANEANDO',
    budget DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    manager_id VARCHAR(36) NOT NULL,
    manual_progress INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    category_id VARCHAR(36),
    priority VARCHAR(10) NOT NULL DEFAULT 'MÉDIA',
    PRIMARY KEY (id),
    CONSTRAINT fk_projects_manager FOREIGN KEY (manager_id) REFERENCES users(id),
    CONSTRAINT fk_project_category FOREIGN KEY (category_id) REFERENCES project_categories(id),
    CONSTRAINT chk_project_status CHECK (status IN ('PLANEANDO', 'EM_PROGRESSO', 'EM_PAUSA', 'COMPLETO', 'CANCELADO')),
    CONSTRAINT chk_project_priority CHECK (priority IN ('BAIXA', 'MÉDIA', 'ALTA', 'CRÍTICA'))
);

-- Indexes
CREATE INDEX idx_projects_manager ON projects(manager_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_priority ON projects(priority);
CREATE INDEX idx_projects_category ON projects(category_id);
CREATE INDEX idx_projects_deadline ON projects(deadline);
CREATE INDEX idx_projects_start_date ON projects(start_date);
CREATE INDEX idx_projects_created_at ON projects(created_at);
