CREATE TABLE monthly_budgets (
    id UUID PRIMARY KEY,
    category VARCHAR(40) NOT NULL,
    year INTEGER NOT NULL CHECK (year BETWEEN 2000 AND 9999),
    month INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    limit_amount NUMERIC(19, 2) NOT NULL CHECK (limit_amount > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_monthly_budget_category_period UNIQUE (category, year, month)
);

CREATE INDEX idx_monthly_budgets_period ON monthly_budgets(year, month);
