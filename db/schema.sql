-- 1. Reference Tables
CREATE TABLE production_lines (
    id BIGSERIAL PRIMARY KEY,
    line_name VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE defect_types (
    id BIGSERIAL PRIMARY KEY,
    defect_code VARCHAR(20) NOT NULL UNIQUE,
    defect_name VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('Critical', 'Major', 'Minor')),
    description TEXT
);

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(150) NOT NULL UNIQUE,
    region VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Core Entities
CREATE TABLE lots (
    id BIGSERIAL PRIMARY KEY,
    lot_identifier VARCHAR(50) NOT NULL UNIQUE, -- The "Business Key" used for fuzzy matching
    normalized_lot_id VARCHAR(50),              -- AC2: Stripped/uppercased version for fuzzy lookups
    part_number VARCHAR(100) NOT NULL,
    created_date DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 3. Activity Logs (Transactional Data)
CREATE TABLE production_logs (
    id BIGSERIAL PRIMARY KEY,
    production_date DATE NOT NULL,
    shift VARCHAR(20) NOT NULL CHECK (shift IN ('Day', 'Swing', 'Night')),
    production_line_id BIGINT NOT NULL REFERENCES production_lines(id) ON DELETE CASCADE,
    lot_id BIGINT NOT NULL REFERENCES lots(id) ON DELETE CASCADE,
    defect_type_id BIGINT REFERENCES defect_types(id) ON DELETE SET NULL,
    units_planned INTEGER NOT NULL CHECK (units_planned >= 0),
    units_actual INTEGER NOT NULL CHECK (units_actual >= 0),
    downtime_minutes INTEGER NOT NULL DEFAULT 0 CHECK (downtime_minutes >= 0),
    issue_flag BOOLEAN NOT NULL DEFAULT FALSE,
    supervisor_notes TEXT,
    source_file VARCHAR(255),                 -- AC9: Original file this record was imported from
    source_row_number INTEGER                  -- AC9: Row number in the source file
);

CREATE TABLE shipping_logs (
    id BIGSERIAL PRIMARY KEY,
    ship_date DATE NOT NULL,
    lot_id BIGINT NOT NULL REFERENCES lots(id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    sales_order_number VARCHAR(50) NOT NULL,
    destination_state VARCHAR(2) NOT NULL,
    carrier VARCHAR(100),
    bol_number VARCHAR(100) NOT NULL UNIQUE,
    tracking_number VARCHAR(100),
    qty_shipped INTEGER NOT NULL CHECK (qty_shipped > 0),
    ship_status VARCHAR(50) NOT NULL CHECK (ship_status IN ('Shipped', 'On Hold', 'Partial')),
    hold_reason TEXT,
    shipping_notes TEXT,
    source_file VARCHAR(255),                 -- AC9: Original file this record was imported from
    source_row_number INTEGER                  -- AC9: Row number in the source file
);

-- 4. Indexes for Query Optimization
CREATE INDEX idx_production_date ON production_logs(production_date);
CREATE INDEX idx_production_lot_id ON production_logs(lot_id);
CREATE INDEX idx_shipping_lot_id ON shipping_logs(lot_id);
CREATE INDEX idx_shipping_date ON shipping_logs(ship_date);

-- AC2: Index for fuzzy matching via normalized lot IDs
CREATE INDEX idx_lots_normalized_id ON lots(normalized_lot_id);
