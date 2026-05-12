-- V11__create_labor_entries.sql

CREATE TABLE labor_entries (
    id VARCHAR(36) NOT NULL,
    task_id VARCHAR(36) NOT NULL,
    worker_id VARCHAR(36) NOT NULL,
    start_date DATE NOT NULL,
    expected_end_date DATE NOT NULL,
    actual_end_date DATE,
    agreed_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_labor_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_labor_worker FOREIGN KEY (worker_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_labor_entries_task ON labor_entries(task_id);
CREATE INDEX idx_labor_entries_worker ON labor_entries(worker_id);
CREATE INDEX idx_labor_entries_start_date ON labor_entries(start_date);
CREATE INDEX idx_labor_entries_expected_end ON labor_entries(expected_end_date);
CREATE INDEX idx_labor_entries_actual_end ON labor_entries(actual_end_date);
