-- V8__create_task_categories.sql

CREATE TABLE task_categories (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_task_categories_name UNIQUE (name)
);

-- Indexes
CREATE INDEX idx_task_categories_active ON task_categories(active);
CREATE INDEX idx_task_categories_name ON task_categories(name);
