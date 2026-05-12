-- V14__alter_tasks_completed_at_to_timestamp.sql

ALTER TABLE tasks
MODIFY completed_at DATETIME(6);