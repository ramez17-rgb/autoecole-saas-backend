-- =====================================================
-- EXTENSIONS
-- =====================================================
-- Needed for UUID generation (optional but recommended)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- TENANT (Auto-école)
-- =====================================================
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    address TEXT,
    contact_email VARCHAR(150),
    status VARCHAR(20) NOT NULL CHECK (status IN ('active', 'suspended')),
    created_at TIMESTAMP DEFAULT now()
);

-- =====================================================
-- USER
-- =====================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('active', 'disabled')),
    created_at TIMESTAMP DEFAULT now(),

    CONSTRAINT fk_user_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id)
        ON DELETE CASCADE
);

-- =====================================================
-- ROLE
-- =====================================================
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- =====================================================
-- USER ↔ ROLE (RBAC)
-- =====================================================
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id INT NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE
);

-- =====================================================
-- STUDENT
-- =====================================================
CREATE TABLE students (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            tenant_id UUID NOT NULL,
            user_id UUID NOT NULL UNIQUE,
            main_monitor_id UUID,
            level VARCHAR(50),
            hours_completed INT DEFAULT 0,
            status VARCHAR(20) NOT NULL CHECK (status IN ('active', 'archived')),
            CONSTRAINT fk_student_user
                FOREIGN KEY (user_id) REFERENCES users(id)
                    ON DELETE CASCADE,
            CONSTRAINT fk_student_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants(id)
                        ON DELETE CASCADE,
            CONSTRAINT fk_student_main_monitor
                FOREIGN KEY (main_monitor_id) REFERENCES monitors(id)
                        ON DELETE SET NULL
);

-- =====================================================
-- MONITOR
-- =====================================================
CREATE TABLE monitors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL UNIQUE,

    CONSTRAINT fk_monitor_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_monitor_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id)
        ON DELETE CASCADE
);

-- =====================================================
-- SESSION
-- =====================================================
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    monitor_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('theory', 'practice')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('planned', 'done', 'canceled')),

    CONSTRAINT fk_session_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_session_monitor
        FOREIGN KEY (monitor_id) REFERENCES monitors(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_session_time
        CHECK (end_time > start_time)
);

-- =====================================================
-- SESSION ↔ STUDENT (Many-to-Many)
-- =====================================================
CREATE TABLE session_students (
    session_id UUID NOT NULL,
    student_id UUID NOT NULL,

    PRIMARY KEY (session_id, student_id),

    CONSTRAINT fk_ss_session
        FOREIGN KEY (session_id) REFERENCES sessions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ss_student
        FOREIGN KEY (student_id) REFERENCES students(id)
        ON DELETE CASCADE
);

-- =====================================================
-- EXAM
-- =====================================================
CREATE TABLE exams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    student_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('code', 'driving')),
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('planned', 'done')),
    result VARCHAR(20) CHECK (result IN ('success', 'fail', 'absent')),

    CONSTRAINT fk_exam_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_exam_student
        FOREIGN KEY (student_id) REFERENCES students(id)
        ON DELETE CASCADE
);

-- =====================================================
-- PAYMENT
-- =====================================================
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    student_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    payment_date DATE NOT NULL,
    mode VARCHAR(20) NOT NULL CHECK (mode IN ('cash', 'transfer')),

    CONSTRAINT fk_payment_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_payment_student
        FOREIGN KEY (student_id) REFERENCES students(id)
        ON DELETE CASCADE
);

-- =====================================================
-- RECEIPT
-- =====================================================
CREATE TABLE receipts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID NOT NULL UNIQUE,
    pdf_reference TEXT,

    CONSTRAINT fk_receipt_payment
        FOREIGN KEY (payment_id) REFERENCES payments(id)
        ON DELETE CASCADE
);

-- =====================================================
-- AUDIT LOG
-- =====================================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    action TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT now(),

    CONSTRAINT fk_audit_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_audit_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- =====================================================
-- INDEXES (Performance)
-- =====================================================
CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_students_tenant ON students(tenant_id);
CREATE INDEX idx_monitors_tenant ON monitors(tenant_id);
CREATE INDEX idx_sessions_tenant ON sessions(tenant_id);
CREATE INDEX idx_exams_tenant ON exams(tenant_id);
CREATE INDEX idx_payments_tenant ON payments(tenant_id);
CREATE INDEX idx_audit_tenant ON audit_logs(tenant_id);
