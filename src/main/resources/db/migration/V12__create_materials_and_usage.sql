-- V12__create_materials_and_usage.sql

CREATE TABLE materials (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    unit VARCHAR(50),
    unit_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_materials_name UNIQUE (name)
);

CREATE TABLE material_usages (
    id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    material_id VARCHAR(36) NOT NULL,
    quantity_used DECIMAL(10, 3) NOT NULL,
    usage_date DATE NOT NULL,
    notes VARCHAR(255),
    recorded_by VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_matusage_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_matusage_material FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE CASCADE,
    CONSTRAINT fk_matusage_user FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_materials_name ON materials(name);

CREATE INDEX idx_material_usages_project ON material_usages(project_id);
CREATE INDEX idx_material_usages_material ON material_usages(material_id);
CREATE INDEX idx_material_usages_date ON material_usages(usage_date);
CREATE INDEX idx_material_usages_recorded_by ON material_usages(recorded_by);
