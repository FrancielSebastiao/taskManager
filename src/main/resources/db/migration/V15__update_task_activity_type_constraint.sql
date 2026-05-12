-- V15__create_task_related_tables.sql

ALTER TABLE task_activities
DROP CHECK chk_task_activity_type;

ALTER TABLE task_activities
ADD CONSTRAINT chk_task_activity_type CHECK (
    type IN (
        'TASK_CREATED', 
        'PROGRESS_UPDATED', 
        'STATUS_CHANGED', 
        'COMMENT_ADDED', 
        'FILE_UPLOADED', 
        'PHOTO_UPLOADED', 
        'ASSIGNEE_ADDED', 
        'ASSIGNEE_REMOVED', 
        'DEADLINE_CHANGED',
        'TASK_COMPLETED'
        )
    );