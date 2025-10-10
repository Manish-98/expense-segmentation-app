CREATE TABLE expense_segments (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    expense_id UUID NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    percentage DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_expense_segments_expense_id ON expense_segments(expense_id);
CREATE INDEX idx_expense_segments_category ON expense_segments(category);

-- Add constraint to ensure percentage is between 0 and 100
ALTER TABLE expense_segments ADD CONSTRAINT chk_percentage_range 
    CHECK (percentage >= 0 AND percentage <= 100);

-- Add constraint to ensure amount is non-negative
ALTER TABLE expense_segments ADD CONSTRAINT chk_amount_non_negative 
    CHECK (amount >= 0);