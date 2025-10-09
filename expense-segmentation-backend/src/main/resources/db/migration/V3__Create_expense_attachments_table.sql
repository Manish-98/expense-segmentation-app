-- Create expense_attachments table for file attachments
CREATE TABLE expense_attachments (
    id UUID PRIMARY KEY,
    expense_id UUID NOT NULL,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by UUID NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_expense_attachments_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    CONSTRAINT fk_expense_attachments_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

-- Create indexes for performance
CREATE INDEX idx_expense_attachments_expense_id ON expense_attachments(expense_id);
CREATE INDEX idx_expense_attachments_uploaded_by ON expense_attachments(uploaded_by);
