-- =====================================================
-- EXTENSIONS
-- =====================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- TENANT (Auto-école)
-- =====================================================
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    address TEXT,
    contact_email VARCHAR(150) NOT NULL,
    contact_phone VARCHAR(20),

    -- Cycle de vie SaaS
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING','ACTIVE','SUSPENDED','REJECTED'))
        DEFAULT 'PENDING',

    refusal_reason TEXT,
    validated_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT now()
);

-- =====================================================
-- USER
-- =====================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    tenant_id UUID,
    nom VARCHAR(255) ,
    prenom VARCHAR(255) ,
    telephone Int,

    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash TEXT, -- NULL tant que le mot de passe n'est pas défini

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('active','disabled'))
        DEFAULT 'disabled',

    created_at TIMESTAMP DEFAULT now(),

    CONSTRAINT fk_user_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
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
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
);

-- =====================================================
-- PASSWORD SETUP TOKEN (Activation sécurisée)
-- =====================================================
CREATE TABLE password_setup_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_password_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =====================================================
-- STUDENT
-- =====================================================
CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    user_id UUID NOT NULL UNIQUE,
    main_monitor_id UUID,
    level VARCHAR(50),
    hours_completed INT DEFAULT 0,
    telephone Int,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('active','archived'))
        DEFAULT 'active',

    CONSTRAINT fk_student_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_student_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE,
        FOREIGN KEY (main_monitor_id)
        REFERENCES monitors(id)
        ON DELETE SET NULL
);

-- =====================================================
-- ENROLLMENT REQUESTS
-- =====================================================
CREATE TABLE enrollment_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED'))
        DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT now(),

    CONSTRAINT fk_en_req_student
        FOREIGN KEY (student_id)
        REFERENCES students(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_en_req_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE
);

-- =====================================================
-- MONITOR
-- =====================================================
CREATE TABLE monitors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL UNIQUE,

    CONSTRAINT fk_monitor_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_monitor_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE
);

-- =====================================================
-- SESSION
-- =====================================================
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    monitor_id UUID NOT NULL,

    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('PLANNED','COMPLETED','CANCELLED'))
        DEFAULT 'PLANNED',

    notes TEXT,
    created_at TIMESTAMP DEFAULT now(),

    CONSTRAINT fk_session_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_session_monitor
        FOREIGN KEY (monitor_id)
        REFERENCES monitors(id)
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
        FOREIGN KEY (session_id)
        REFERENCES sessions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ss_student
        FOREIGN KEY (student_id)
        REFERENCES students(id)
        ON DELETE CASCADE
);

-- =====================================================
-- EXAM
-- =====================================================
CREATE TABLE exams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    student_id UUID NOT NULL,

    type VARCHAR(20) NOT NULL
        CHECK (type IN ('code','driving')),

    date DATE NOT NULL,

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('planned','done'))
        DEFAULT 'planned',

    result VARCHAR(20)
        CHECK (result IN ('success','fail','absent')),
        
    score INT,
    created_at TIMESTAMP DEFAULT now(),

    CONSTRAINT fk_exam_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_exam_student
        FOREIGN KEY (student_id)
        REFERENCES students(id)
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

    mode VARCHAR(20) NOT NULL
        CHECK (mode IN ('cash','transfer')),

    CONSTRAINT fk_payment_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_payment_student
        FOREIGN KEY (student_id)
        REFERENCES students(id)
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
        FOREIGN KEY (payment_id)
        REFERENCES payments(id)
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
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_audit_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =====================================================
-- INDEXES (Performance Multi-Tenant)
-- =====================================================
CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_students_tenant ON students(tenant_id);
CREATE INDEX idx_monitors_tenant ON monitors(tenant_id);
CREATE INDEX idx_sessions_tenant ON sessions(tenant_id);
CREATE INDEX idx_exams_tenant ON exams(tenant_id);
CREATE INDEX idx_payments_tenant ON payments(tenant_id);
CREATE INDEX idx_audit_tenant ON audit_logs(tenant_id);



-- =====================================================
-- MOCK TESTS & QUESTIONS (Sprint 4)
-- =====================================================
CREATE TABLE questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    category VARCHAR(100) NOT NULL,
    question_text TEXT NOT NULL,
    correct_answer VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT now(),
    CONSTRAINT fk_questions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE TABLE question_options (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question_id UUID NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    CONSTRAINT fk_qo_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE TABLE mock_tests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    student_id UUID NOT NULL,
    score INT,
    total_questions INT NOT NULL DEFAULT 40,
    passed BOOLEAN,
    taken_at TIMESTAMP DEFAULT now(),
    CONSTRAINT fk_mock_test_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_mock_test_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE mock_test_answers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mock_test_id UUID NOT NULL,
    question_id UUID NOT NULL,
    selected_answer VARCHAR(255),
    is_correct BOOLEAN,
    CONSTRAINT fk_mta_mock_test FOREIGN KEY (mock_test_id) REFERENCES mock_tests(id) ON DELETE CASCADE,
    CONSTRAINT fk_mta_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE INDEX idx_questions_tenant ON questions(tenant_id);
CREATE INDEX idx_mock_tests_tenant ON mock_tests(tenant_id);
CREATE INDEX idx_mock_tests_student ON mock_tests(student_id);
