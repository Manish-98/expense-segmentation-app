CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_categories_active ON categories(active);
CREATE INDEX idx_categories_name ON categories(name);

-- Insert default categories
INSERT INTO categories (name, description) VALUES 
('Travel', 'Business travel expenses including flights, hotels, and transportation'),
('Meals', 'Business meals and entertainment expenses'),
('Supplies', 'Office supplies and equipment'),
('Entertainment', 'Client entertainment and team building'),
('Office', 'Office rent, utilities, and administrative expenses'),
('Training', 'Professional development and training courses'),
('Other', 'Miscellaneous business expenses');