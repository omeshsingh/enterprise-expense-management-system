-- V4__add_manager_to_users_table.sql

ALTER TABLE users
ADD COLUMN manager_id BIGINT;

ALTER TABLE users
ADD CONSTRAINT fk_user_manager
FOREIGN KEY (manager_id)
REFERENCES users(id)
ON DELETE SET NULL; -- Or ON DELETE RESTRICT, depending on desired behavior