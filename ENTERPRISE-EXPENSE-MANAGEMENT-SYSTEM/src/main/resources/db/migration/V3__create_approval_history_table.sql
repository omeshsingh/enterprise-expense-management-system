-- V3__create_approval_history_table.sql

CREATE TABLE approval_history (
    id BIGSERIAL PRIMARY KEY,
    expense_id BIGINT NOT NULL,
    approver_user_id BIGINT NOT NULL,
    status_before VARCHAR(50),
    status_after VARCHAR(50) NOT NULL,
    comments TEXT,
    action_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    FOREIGN KEY (approver_user_id) REFERENCES users(id)
);