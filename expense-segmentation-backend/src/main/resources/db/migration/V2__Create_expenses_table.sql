-- Create expenses table for expense/invoice submission
CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    date DATE NOT NULL,
    vendor VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    created_by UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_expenses_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Create indexes for performance
CREATE INDEX idx_expenses_created_by ON expenses(created_by);
CREATE INDEX idx_expenses_date ON expenses(date);
CREATE INDEX idx_expenses_status ON expenses(status);
CREATE INDEX idx_expenses_type ON expenses(type);
