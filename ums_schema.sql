-- =====================================================================
-- University Management System - PostgreSQL schema (118 tables, 20 modules)
-- Java Spring Boot + JPA. Use as Flyway V1__init_schema.sql
-- =====================================================================

-- ---------- 1. SECURITY & DYNAMIC RBAC (8) ----------
CREATE TABLE users (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username            VARCHAR(100) NOT NULL UNIQUE,
    email               VARCHAR(150) UNIQUE,
    phone               VARCHAR(30),
    password_hash       VARCHAR(255) NOT NULL,
    user_type           VARCHAR(20)  NOT NULL CHECK (user_type IN ('STUDENT','STAFF','GUARDIAN','APPLICANT','ADMIN')),
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','LOCKED','DISABLED')),
    failed_login_count  INT NOT NULL DEFAULT 0,
    last_login_at       TIMESTAMPTZ,
    session_ttl_days    INT NOT NULL DEFAULT 180 CHECK (session_ttl_days IN (7,30,90,180,365)),  -- auto-terminate sessions inactive this long
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,   -- temporary password (new account, admin reset): only a password change is allowed
    password_changed_at TIMESTAMPTZ,
    preferred_language  VARCHAR(2) NOT NULL DEFAULT 'km' CHECK (preferred_language IN ('km','en')),  -- notifications and emails go out in this language
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          BIGINT,
    updated_by          BIGINT
);

CREATE TABLE roles (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    name_kh     VARCHAR(100),
    description TEXT,
    description_kh TEXT,
    is_system   BOOLEAN NOT NULL DEFAULT FALSE,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  BIGINT,
    updated_by  BIGINT
);

CREATE TABLE permissions (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR(80) NOT NULL UNIQUE,
    module      VARCHAR(50) NOT NULL,
    action      VARCHAR(20) NOT NULL CHECK (action IN ('VIEW','CREATE','UPDATE','DELETE','APPROVE','EXPORT','MANAGE')),
    description TEXT,
    description_kh TEXT
);

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    scope_type  VARCHAR(20) NOT NULL DEFAULT 'GLOBAL' CHECK (scope_type IN ('GLOBAL','FACULTY','DEPARTMENT','PROGRAM','CLASS_SECTION')),
    scope_id    BIGINT,
    valid_from  DATE,
    valid_to    DATE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  BIGINT,
    CHECK ((scope_type = 'GLOBAL' AND scope_id IS NULL) OR (scope_type <> 'GLOBAL' AND scope_id IS NOT NULL))
);
CREATE UNIQUE INDEX ux_user_roles ON user_roles (user_id, role_id, scope_type, COALESCE(scope_id, 0));

CREATE TABLE menus (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id     BIGINT REFERENCES menus(id),
    name          VARCHAR(100) NOT NULL,
    name_kh       VARCHAR(100),
    path          VARCHAR(200),
    icon          VARCHAR(50),
    sort_order    INT NOT NULL DEFAULT 0,
    permission_id BIGINT REFERENCES permissions(id)
);

CREATE TABLE user_sessions (                     -- Telegram-style sessions: one row per logged-in device (web and mobile)
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL UNIQUE,   -- rotated on every refresh, stored hashed
    device_id          VARCHAR(100),                   -- mobile: generated on first launch; web: random id kept in localStorage
    device_model       VARCHAR(100),                   -- iPhone 15, Samsung A54, Chrome on Windows
    platform           VARCHAR(10) NOT NULL CHECK (platform IN ('WEB','ANDROID','IOS','DESKTOP')),
    os_version         VARCHAR(50),
    app_name           VARCHAR(50),                    -- UMS Student, UMS Staff, UMS Web
    app_version        VARCHAR(20),
    ip_address         VARCHAR(45),
    country            VARCHAR(60),                    -- from IP (GeoLite2), shown as "Phnom Penh, Cambodia"
    city               VARCHAR(80),
    user_agent         VARCHAR(500),
    fcm_token          VARCHAR(255),                   -- push notifications to this device
    login_method       VARCHAR(10) NOT NULL DEFAULT 'PASSWORD' CHECK (login_method IN ('PASSWORD','QR','OTP')),
    remember_me        BOOLEAN NOT NULL DEFAULT TRUE,  -- web: FALSE = ends when the browser closes or after session.web.max_hours
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_active_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at         TIMESTAMPTZ NOT NULL,
    revoked_at         TIMESTAMPTZ,
    revoked_by         BIGINT REFERENCES users(id),     -- the user, or an admin
    revoked_reason     VARCHAR(25) CHECK (revoked_reason IN ('LOGOUT','TERMINATED_BY_USER','TERMINATE_OTHERS','PASSWORD_CHANGED','ADMIN','USER_LOCKED','INACTIVE','EXPIRED','MAX_DEVICES','TOKEN_REUSE','REPLACED'))
);
CREATE INDEX ix_sessions_user_active ON user_sessions (user_id, last_active_at DESC) WHERE revoked_at IS NULL;
CREATE INDEX ix_sessions_fcm ON user_sessions (user_id) WHERE revoked_at IS NULL AND fcm_token IS NOT NULL;
CREATE UNIQUE INDEX ux_sessions_device_active ON user_sessions (user_id, device_id) WHERE revoked_at IS NULL AND device_id IS NOT NULL;  -- same device logging in again replaces its old session

CREATE TABLE audit_logs (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT REFERENCES users(id),
    action      VARCHAR(50) NOT NULL,
    entity      VARCHAR(80) NOT NULL,
    entity_id   BIGINT,
    old_value   JSONB,
    new_value   JSONB,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_audit_entity ON audit_logs (entity, entity_id);

-- ---------- 2. ORGANIZATION (5) ----------
CREATE TABLE faculties (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code          VARCHAR(20)  NOT NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    name_kh       VARCHAR(150),
    dean_staff_id BIGINT,                 -- FK added after staff
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMPTZ
);

CREATE TABLE departments (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    faculty_id    BIGINT NOT NULL REFERENCES faculties(id),
    code          VARCHAR(20)  NOT NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    name_kh       VARCHAR(150),
    head_staff_id BIGINT,                 -- FK added after staff
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMPTZ
);

CREATE TABLE programs (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    department_id  BIGINT NOT NULL REFERENCES departments(id),
    code           VARCHAR(20)  NOT NULL UNIQUE,
    name           VARCHAR(150) NOT NULL,
    name_kh        VARCHAR(150),
    degree_level   VARCHAR(20) NOT NULL CHECK (degree_level IN ('FOUNDATION','CERTIFICATE','DIPLOMA','ASSOCIATE','BACHELOR','MASTER','PHD')),
    duration_years NUMERIC(3,1) NOT NULL,
    total_credits  INT NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ
);

CREATE TABLE buildings (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(20)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    name_kh VARCHAR(100)
);

CREATE TABLE rooms (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES buildings(id),
    code        VARCHAR(20) NOT NULL,
    capacity    INT NOT NULL,
    room_type   VARCHAR(20) NOT NULL CHECK (room_type IN ('LECTURE','LAB','HALL','EXAM')),
    UNIQUE (building_id, code)
);

-- ---------- 3. PEOPLE (9) ----------
-- Every country (ISO 3166-1), for a person's nationality and an address's country. Seeded by V5__seed_countries.sql.
CREATE TABLE countries (
    code           VARCHAR(2)   PRIMARY KEY CHECK (code ~ '^[A-Z]{2}$'),   -- KH, TH, VN …
    name_en        VARCHAR(100) NOT NULL,
    name_kh        VARCHAR(100) NOT NULL,
    nationality_en VARCHAR(100),                 -- Cambodian, Thai …; empty for uninhabited territories
    nationality_kh VARCHAR(100),                 -- ខ្មែរ, ថៃ …
    is_active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE persons (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name     VARCHAR(80) NOT NULL,
    last_name      VARCHAR(80) NOT NULL,
    first_name_kh  VARCHAR(80),
    last_name_kh   VARCHAR(80),
    gender         VARCHAR(10) CHECK (gender IN ('MALE','FEMALE','OTHER')),
    dob            DATE,
    nationality_code VARCHAR(2) DEFAULT 'KH' REFERENCES countries(code),
    national_id    VARCHAR(50),
    phone          VARCHAR(30),
    email          VARCHAR(150),
    photo_url      VARCHAR(500),               -- birth place and addresses: person_addresses
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Cambodia's administrative areas, one table for all four levels: province → district → commune → village.
-- Seeded by V4__seed_geo_areas.sql; admins keep it up to date (new sub-decrees, renamed or merged areas).
CREATE TABLE geo_areas (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code       VARCHAR(10)  NOT NULL UNIQUE,     -- official code: 2 digits province, 4 district, 6 commune, 8 village
    level      VARCHAR(10)  NOT NULL CHECK (level IN ('PROVINCE','DISTRICT','COMMUNE','VILLAGE')),
    parent_id  BIGINT REFERENCES geo_areas(id),
    name_kh    VARCHAR(150) NOT NULL,
    name_en    VARCHAR(150) NOT NULL,
    unit_kh    VARCHAR(30),                      -- រាជធានី, ខេត្ត, ខណ្ឌ, ក្រុង, ស្រុក, សង្កាត់, ឃុំ, ភូមិ
    unit_en    VARCHAR(30),                      -- Capital, Province, Section, Municipality, District, Quarter, Commune, Village
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,    -- FALSE: merged or abolished; kept so old addresses still show
    CHECK ((level = 'PROVINCE') = (parent_id IS NULL))
);
CREATE INDEX ix_geo_areas_parent ON geo_areas (parent_id, name_en);
CREATE INDEX ix_geo_areas_level ON geo_areas (level, name_en);

-- The database itself keeps the tree right, even for rows typed in by hand (e.g. in DBeaver):
-- a district's parent is a province, a commune's a district, a village's a commune,
-- and every code is its parent's code plus 2 digits (01 → 0102 → 010201 → 01020101).
CREATE FUNCTION geo_areas_check_tree() RETURNS trigger AS $$
DECLARE
    parent_level VARCHAR(10);
    parent_code  VARCHAR(10);
    expected     VARCHAR(10);
    digits       INT;
BEGIN
    -- worked out first: in IF ... THEN, PL/pgSQL would stop the condition at the THEN inside a CASE
    digits := CASE NEW.level WHEN 'PROVINCE' THEN 2 WHEN 'DISTRICT' THEN 4 WHEN 'COMMUNE' THEN 6 ELSE 8 END;
    IF length(NEW.code) <> digits THEN
        RAISE EXCEPTION 'geo_areas %: a % code has % digits', NEW.code, lower(NEW.level), digits;
    END IF;
    IF NEW.level = 'PROVINCE' THEN
        RETURN NEW;
    END IF;
    SELECT level, code INTO parent_level, parent_code FROM geo_areas WHERE id = NEW.parent_id;
    expected := CASE NEW.level WHEN 'DISTRICT' THEN 'PROVINCE' WHEN 'COMMUNE' THEN 'DISTRICT' ELSE 'COMMUNE' END;
    IF parent_level IS DISTINCT FROM expected THEN
        RAISE EXCEPTION 'geo_areas %: a % must be inside a %', NEW.code, lower(NEW.level), lower(expected);
    END IF;
    IF left(NEW.code, length(parent_code)) <> parent_code THEN
        RAISE EXCEPTION 'geo_areas %: the code must start with its parent''s code %', NEW.code, parent_code;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_geo_areas_check_tree
    BEFORE INSERT OR UPDATE OF code, level, parent_id ON geo_areas
    FOR EACH ROW EXECUTE FUNCTION geo_areas_check_tree();

-- Read-only views, one per level, for browsing and reports (DBeaver, SQL): each shows the levels above it.
-- The application keeps using geo_areas; changes go there.
CREATE VIEW provinces AS
SELECT p.id, p.code, p.name_kh, p.name_en, p.unit_kh, p.unit_en, p.is_active
FROM geo_areas p
WHERE p.level = 'PROVINCE';

CREATE VIEW districts AS
SELECT d.id, d.code, d.name_kh, d.name_en, d.unit_kh, d.unit_en, d.is_active,
       p.id AS province_id, p.code AS province_code, p.name_kh AS province_name_kh, p.name_en AS province_name_en
FROM geo_areas d
JOIN geo_areas p ON p.id = d.parent_id
WHERE d.level = 'DISTRICT';

CREATE VIEW communes AS
SELECT c.id, c.code, c.name_kh, c.name_en, c.unit_kh, c.unit_en, c.is_active,
       d.id AS district_id, d.name_kh AS district_name_kh, d.name_en AS district_name_en,
       p.id AS province_id, p.name_kh AS province_name_kh, p.name_en AS province_name_en
FROM geo_areas c
JOIN geo_areas d ON d.id = c.parent_id
JOIN geo_areas p ON p.id = d.parent_id
WHERE c.level = 'COMMUNE';

CREATE VIEW villages AS
SELECT v.id, v.code, v.name_kh, v.name_en, v.is_active,
       c.id AS commune_id, c.name_kh AS commune_name_kh, c.name_en AS commune_name_en,
       d.id AS district_id, d.name_kh AS district_name_kh, d.name_en AS district_name_en,
       p.id AS province_id, p.name_kh AS province_name_kh, p.name_en AS province_name_en
FROM geo_areas v
JOIN geo_areas c ON c.id = v.parent_id
JOIN geo_areas d ON d.id = c.parent_id
JOIN geo_areas p ON p.id = d.parent_id
WHERE v.level = 'VILLAGE';

-- Birth place, permanent and current address of a student, staff member, guardian or applicant.
-- The app sends only the smallest area chosen (usually the village); the server fills the levels above it.
CREATE TABLE person_addresses (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person_id    BIGINT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    address_type VARCHAR(20) NOT NULL CHECK (address_type IN ('BIRTH_PLACE','PERMANENT','CURRENT')),
    house_no     VARCHAR(30),
    street       VARCHAR(100),
    village_id   BIGINT REFERENCES geo_areas(id),
    commune_id   BIGINT REFERENCES geo_areas(id),
    district_id  BIGINT REFERENCES geo_areas(id),
    province_id  BIGINT REFERENCES geo_areas(id),   -- kept for reports such as "students per province"
    country_code VARCHAR(2) NOT NULL DEFAULT 'KH' REFERENCES countries(code),
    address_line TEXT,                              -- an address outside Cambodia, or extra detail
    UNIQUE (person_id, address_type),
    CHECK (province_id IS NOT NULL OR address_line IS NOT NULL),
    -- provinces, districts, communes and villages are Cambodia's: no area ids on an address abroad
    CHECK (country_code = 'KH' OR (province_id IS NULL AND district_id IS NULL AND commune_id IS NULL AND village_id IS NULL))
);
CREATE INDEX ix_person_addresses_province ON person_addresses (province_id);

CREATE TABLE batches (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id),
    name       VARCHAR(50) NOT NULL,
    name_kh    VARCHAR(50),
    start_year INT NOT NULL,
    UNIQUE (program_id, name)
);

CREATE TABLE staff (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        BIGINT UNIQUE REFERENCES users(id),
    person_id      BIGINT NOT NULL REFERENCES persons(id),
    staff_code     VARCHAR(30) NOT NULL UNIQUE,
    department_id  BIGINT REFERENCES departments(id),
    position       VARCHAR(100),
    position_kh    VARCHAR(100),
    academic_title VARCHAR(50),
    academic_title_kh VARCHAR(50),
    staff_type     VARCHAR(20) NOT NULL CHECK (staff_type IN ('LECTURER','OFFICE','BOTH')),   -- BOTH: teaches and does office work; permissions come from roles
    hire_date      DATE,
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','ON_LEAVE','RESIGNED','TERMINATED','RETIRED')),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ
);

ALTER TABLE faculties   ADD CONSTRAINT fk_faculty_dean FOREIGN KEY (dean_staff_id) REFERENCES staff(id);
ALTER TABLE departments ADD CONSTRAINT fk_dept_head    FOREIGN KEY (head_staff_id) REFERENCES staff(id);

CREATE TABLE students (                         -- one row per program studied: Bachelor, then Master, or a double degree
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id                  BIGINT REFERENCES users(id),   -- one login can own several rows
    person_id                BIGINT NOT NULL REFERENCES persons(id),
    application_id           BIGINT UNIQUE,      -- the application that admitted them; FK added after applications
    student_code             VARCHAR(30) NOT NULL UNIQUE,
    program_id               BIGINT NOT NULL REFERENCES programs(id),
    batch_id                 BIGINT REFERENCES batches(id),
    year_level               INT NOT NULL DEFAULT 1 CHECK (year_level BETWEEN 1 AND 10),  -- moved up by the registrar each year
    admission_date           DATE NOT NULL,
    expected_graduation_year INT,
    status                   VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','SUSPENDED','ON_LEAVE','GRADUATED','DROPPED')),
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at               TIMESTAMPTZ
);
CREATE INDEX ix_students_program ON students (program_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_students_user ON students (user_id) WHERE deleted_at IS NULL;
-- A login can't be a current student of the same program twice
CREATE UNIQUE INDEX ux_students_user_program_current ON students (user_id, program_id)
    WHERE status IN ('ACTIVE','SUSPENDED','ON_LEAVE') AND deleted_at IS NULL AND user_id IS NOT NULL;

CREATE TABLE guardians (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT UNIQUE REFERENCES users(id),
    person_id  BIGINT NOT NULL REFERENCES persons(id),
    occupation VARCHAR(100)
);

CREATE TABLE student_guardians (
    student_id   BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    guardian_id  BIGINT NOT NULL REFERENCES guardians(id) ON DELETE CASCADE,
    relationship VARCHAR(30) NOT NULL,
    is_primary   BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (student_id, guardian_id)
);

-- ---------- 4. ACADEMIC STRUCTURE (9) ----------
CREATE TABLE academic_years (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(20) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE semesters (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    academic_year_id   BIGINT NOT NULL REFERENCES academic_years(id),
    name               VARCHAR(50) NOT NULL,
    name_kh            VARCHAR(50),
    semester_no        INT NOT NULL,
    start_date         DATE NOT NULL,
    end_date           DATE NOT NULL,
    registration_start DATE,
    registration_end   DATE,
    is_current         BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (academic_year_id, semester_no)
);

CREATE TABLE courses (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES departments(id),
    code          VARCHAR(20)  NOT NULL UNIQUE,
    title         VARCHAR(200) NOT NULL,
    title_kh      VARCHAR(200),
    credits       INT NOT NULL,
    theory_hours  INT NOT NULL DEFAULT 0,
    lab_hours     INT NOT NULL DEFAULT 0,
    description   TEXT,
    description_kh TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMPTZ
);

CREATE TABLE course_prerequisites (
    course_id              BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    prerequisite_course_id BIGINT NOT NULL REFERENCES courses(id),
    PRIMARY KEY (course_id, prerequisite_course_id),
    CHECK (course_id <> prerequisite_course_id)
);

CREATE TABLE curriculums (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    program_id     BIGINT NOT NULL REFERENCES programs(id),
    version        VARCHAR(20) NOT NULL,
    effective_year INT NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    UNIQUE (program_id, version)
);

CREATE TABLE curriculum_courses (
    curriculum_id BIGINT NOT NULL REFERENCES curriculums(id) ON DELETE CASCADE,
    course_id     BIGINT NOT NULL REFERENCES courses(id),
    year_level    INT NOT NULL,
    semester_no   INT NOT NULL,
    is_required   BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (curriculum_id, course_id)
);

CREATE TABLE class_sections (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id    BIGINT NOT NULL REFERENCES courses(id),
    semester_id  BIGINT NOT NULL REFERENCES semesters(id),
    section_code VARCHAR(10) NOT NULL,
    capacity     INT NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','CLOSED','CANCELLED','COMPLETED')),
    UNIQUE (course_id, semester_id, section_code)
);

CREATE TABLE class_section_lecturers (           -- a class can have several teachers: theory, lab, tutorial
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_section_id BIGINT NOT NULL REFERENCES class_sections(id) ON DELETE CASCADE,
    staff_id         BIGINT NOT NULL REFERENCES staff(id),
    teaching_role    VARCHAR(20) NOT NULL DEFAULT 'MAIN' CHECK (teaching_role IN ('MAIN','ASSISTANT','LAB','TUTORIAL')),
    UNIQUE (class_section_id, staff_id)
);
-- MAIN is responsible for the class: submits the final grades
CREATE UNIQUE INDEX ux_section_main_lecturer ON class_section_lecturers (class_section_id) WHERE teaching_role = 'MAIN';
CREATE INDEX ix_section_lecturers_staff ON class_section_lecturers (staff_id);

CREATE TABLE class_schedules (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_section_id BIGINT NOT NULL REFERENCES class_sections(id) ON DELETE CASCADE,
    lecturer_id      BIGINT REFERENCES staff(id),   -- who teaches this slot, e.g. the lab teacher; empty = the MAIN lecturer
    room_id          BIGINT REFERENCES rooms(id),
    day_of_week      INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time       TIME NOT NULL,
    end_time         TIME NOT NULL,
    CHECK (end_time > start_time)
);

-- ---------- 10a. FINANCE masters needed early (fee_types, scholarships) ----------
CREATE TABLE fee_types (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code          VARCHAR(30) NOT NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    name_kh       VARCHAR(100),
    is_refundable BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE scholarships (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code                   VARCHAR(30) NOT NULL UNIQUE,
    name                   VARCHAR(150) NOT NULL,
    name_kh                VARCHAR(150),
    discount_type          VARCHAR(10) NOT NULL CHECK (discount_type IN ('PERCENT','FIXED')),
    value                  NUMERIC(12,2) NOT NULL,
    applies_to_fee_type_id BIGINT REFERENCES fee_types(id)
);

-- ---------- 5. ADMISSION (7) ----------
CREATE TABLE admission_intakes (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id),
    name             VARCHAR(100) NOT NULL,
    name_kh          VARCHAR(100),
    open_date        DATE NOT NULL,
    close_date       DATE NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','OPEN','CLOSED'))
);

CREATE TABLE intake_programs (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    intake_id     BIGINT NOT NULL REFERENCES admission_intakes(id) ON DELETE CASCADE,
    program_id    BIGINT NOT NULL REFERENCES programs(id),
    quota         INT,
    form_price    NUMERIC(12,2) NOT NULL DEFAULT 0,
    admission_fee NUMERIC(12,2) NOT NULL DEFAULT 0,
    exam_date     DATE,
    UNIQUE (intake_id, program_id)
);

CREATE TABLE applicants (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person_id      BIGINT NOT NULL REFERENCES persons(id),
    user_id        BIGINT UNIQUE REFERENCES users(id),
    high_school    VARCHAR(200),
    grade12_year   INT,
    grade12_result VARCHAR(10),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ
);

-- ---------- 10b. FINANCE: invoices & payments ----------
CREATE TABLE invoices (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    invoice_no      VARCHAR(30) NOT NULL UNIQUE,
    student_id      BIGINT REFERENCES students(id),
    applicant_id    BIGINT REFERENCES applicants(id),
    semester_id     BIGINT REFERENCES semesters(id),
    subtotal        NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount    NUMERIC(12,2) NOT NULL DEFAULT 0,
    paid_amount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD' CHECK (currency IN ('USD','KHR')),
    due_date        DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'UNPAID' CHECK (status IN ('UNPAID','PARTIAL','PAID','CANCELLED','OVERDUE')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      BIGINT REFERENCES users(id),
    CHECK ((student_id IS NOT NULL)::int + (applicant_id IS NOT NULL)::int = 1)
);
CREATE INDEX ix_invoices_student   ON invoices (student_id);
CREATE INDEX ix_invoices_applicant ON invoices (applicant_id);

CREATE TABLE invoice_items (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    invoice_id  BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    fee_type_id BIGINT NOT NULL REFERENCES fee_types(id),
    description VARCHAR(255),
    quantity    INT NOT NULL DEFAULT 1,
    unit_price  NUMERIC(12,2) NOT NULL,
    amount      NUMERIC(12,2) NOT NULL
);

CREATE TABLE payments (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_no    VARCHAR(30) NOT NULL UNIQUE,
    invoice_id    BIGINT REFERENCES invoices(id),   -- NULL allowed for walk-in form sales
    amount        NUMERIC(12,2) NOT NULL,
    currency      VARCHAR(3) NOT NULL DEFAULT 'USD' CHECK (currency IN ('USD','KHR')),
    exchange_rate NUMERIC(12,4),
    method        VARCHAR(20) NOT NULL CHECK (method IN ('CASH','BANK_TRANSFER','ABA','KHQR','WING','ACLEDA')),
    reference_no  VARCHAR(100),
    paid_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    received_by   BIGINT REFERENCES users(id),
    status        VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' CHECK (status IN ('SUCCESS','REVERSED'))
);
CREATE INDEX ix_payments_invoice ON payments (invoice_id);
CREATE INDEX ix_payments_paid_at ON payments (paid_at);

CREATE TABLE application_form_sales (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    form_no     VARCHAR(30) NOT NULL UNIQUE,
    intake_id   BIGINT NOT NULL REFERENCES admission_intakes(id),
    buyer_name  VARCHAR(150) NOT NULL,
    buyer_phone VARCHAR(30),
    amount      NUMERIC(12,2) NOT NULL,
    payment_id  BIGINT REFERENCES payments(id),
    sold_by     BIGINT REFERENCES users(id),
    sold_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    status      VARCHAR(20) NOT NULL DEFAULT 'SOLD' CHECK (status IN ('SOLD','USED','VOID','REFUNDED'))
);

CREATE TABLE applications (
    id                        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_no            VARCHAR(30) NOT NULL UNIQUE,
    applicant_id              BIGINT NOT NULL REFERENCES applicants(id),
    intake_id                 BIGINT NOT NULL REFERENCES admission_intakes(id),
    form_sale_id              BIGINT UNIQUE REFERENCES application_form_sales(id),
    first_choice_program_id   BIGINT NOT NULL REFERENCES programs(id),
    second_choice_program_id  BIGINT REFERENCES programs(id),
    admitted_program_id       BIGINT REFERENCES programs(id),
    status                    VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED'
                              CHECK (status IN ('SUBMITTED','REVIEWING','EXAM','ACCEPTED','WAITLISTED','REJECTED','ENROLLED','CANCELLED')),
    -- the last study done before THIS application (applicants keeps the grade 12 details)
    previous_level            VARCHAR(20) CHECK (previous_level IN ('HIGH_SCHOOL','FOUNDATION','CERTIFICATE','DIPLOMA','ASSOCIATE','BACHELOR','MASTER','PHD')),
    previous_institution      VARCHAR(200),
    previous_major            VARCHAR(150),
    previous_year             INT,
    previous_result           VARCHAR(20),     -- GPA, grade or mention, as written on the certificate
    previous_student_id       BIGINT REFERENCES students(id),   -- set when they studied here before
    submitted_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    decided_by                BIGINT REFERENCES staff(id),
    decided_at                TIMESTAMPTZ
);
CREATE INDEX ix_applications_intake_status ON applications (intake_id, status);
ALTER TABLE students ADD CONSTRAINT fk_student_application FOREIGN KEY (application_id) REFERENCES applications(id);

CREATE TABLE application_documents (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    doc_type       VARCHAR(20) NOT NULL CHECK (doc_type IN ('ID_CARD','PHOTO','BIRTH_CERT','GRADE12_CERT',
                                                            'CERTIFICATE','DIPLOMA','ASSOCIATE_DEGREE','BACHELOR_DEGREE','MASTER_DEGREE','TRANSCRIPT',
                                                            'CV','RECOMMENDATION','RESEARCH_PROPOSAL','LANGUAGE_CERT','OTHER')),
    file_url       VARCHAR(500) NOT NULL,
    verified       BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by    BIGINT REFERENCES staff(id)
);

CREATE TABLE application_results (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_id  BIGINT NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    exam_score      NUMERIC(5,2),
    interview_score NUMERIC(5,2),
    total_score     NUMERIC(5,2),
    decision        VARCHAR(20) CHECK (decision IN ('ACCEPTED','WAITLISTED','REJECTED')),
    scholarship_id  BIGINT REFERENCES scholarships(id),
    remark          TEXT
);

-- ---------- 10c. FINANCE: remaining ----------
CREATE TABLE fee_structures (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    program_id       BIGINT NOT NULL REFERENCES programs(id),
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id),
    fee_type_id      BIGINT NOT NULL REFERENCES fee_types(id),
    year_level       INT NOT NULL DEFAULT 1,
    amount           NUMERIC(12,2) NOT NULL,
    currency         VARCHAR(3) NOT NULL DEFAULT 'USD' CHECK (currency IN ('USD','KHR')),
    UNIQUE (program_id, academic_year_id, fee_type_id, year_level)
);

CREATE TABLE student_scholarships (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id       BIGINT NOT NULL REFERENCES students(id),
    scholarship_id   BIGINT NOT NULL REFERENCES scholarships(id),
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id),
    approved_by      BIGINT REFERENCES staff(id),
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    UNIQUE (student_id, scholarship_id, academic_year_id)
);

-- ---------- 11. CHAT & NOTIFICATIONS (5) ----------
CREATE TABLE conversations (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type             VARCHAR(20) NOT NULL CHECK (type IN ('DIRECT','GROUP','CLASS','LEAVE_REQUEST','STUDENT_REQUEST')),
    title            VARCHAR(200),
    class_section_id BIGINT REFERENCES class_sections(id),
    reference_type   VARCHAR(30),
    reference_id     BIGINT,
    created_by       BIGINT REFERENCES users(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_message_at  TIMESTAMPTZ
);

CREATE TABLE messages (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id       BIGINT REFERENCES users(id),     -- NULL for SYSTEM messages
    message_type    VARCHAR(10) NOT NULL DEFAULT 'TEXT' CHECK (message_type IN ('TEXT','IMAGE','FILE','SYSTEM')),
    content         TEXT,
    reply_to_id     BIGINT REFERENCES messages(id),
    edited_at       TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_messages_conv ON messages (conversation_id, id DESC);

CREATE TABLE conversation_participants (
    conversation_id      BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id              BIGINT NOT NULL REFERENCES users(id),
    role                 VARCHAR(10) NOT NULL DEFAULT 'MEMBER' CHECK (role IN ('MEMBER','ADMIN')),
    last_read_message_id BIGINT REFERENCES messages(id),
    is_muted             BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    left_at              TIMESTAMPTZ,
    PRIMARY KEY (conversation_id, user_id)
);
CREATE INDEX ix_participants_user ON conversation_participants (user_id);

CREATE TABLE message_attachments (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    file_name  VARCHAR(255) NOT NULL,
    file_url   VARCHAR(500) NOT NULL,
    mime_type  VARCHAR(100),
    size_bytes BIGINT
);

CREATE TABLE notifications (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       VARCHAR(50) NOT NULL,
    title      VARCHAR(200) NOT NULL,
    message    TEXT,
    link       VARCHAR(500),
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_notifications_user_unread ON notifications (user_id) WHERE is_read = FALSE;

-- ---------- 8a. LEAVE REQUESTS (needed before attendance) ----------
CREATE TABLE leave_types (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code              VARCHAR(20) NOT NULL UNIQUE,
    name              VARCHAR(100) NOT NULL,
    name_kh           VARCHAR(100),
    requires_document BOOLEAN NOT NULL DEFAULT FALSE,
    max_days          INT
);

CREATE TABLE leave_requests (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_no      VARCHAR(30) NOT NULL UNIQUE,
    student_id      BIGINT NOT NULL REFERENCES students(id),
    leave_type_id   BIGINT NOT NULL REFERENCES leave_types(id),
    from_date       DATE NOT NULL,
    to_date         DATE NOT NULL,
    reason          TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','PARTIAL','REJECTED','CANCELLED')),
    conversation_id BIGINT UNIQUE REFERENCES conversations(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (to_date >= from_date)
);
CREATE INDEX ix_leave_student ON leave_requests (student_id);

-- ---------- 6. ENROLLMENT, ATTENDANCE & GRADING (9) ----------
CREATE TABLE enrollments (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id       BIGINT NOT NULL REFERENCES students(id),
    class_section_id BIGINT NOT NULL REFERENCES class_sections(id),
    enrolled_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    status           VARCHAR(20) NOT NULL DEFAULT 'ENROLLED' CHECK (status IN ('ENROLLED','DROPPED','COMPLETED','WITHDRAWN')),
    is_retake        BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (student_id, class_section_id)
);
CREATE INDEX ix_enrollments_section ON enrollments (class_section_id);

CREATE TABLE attendance_sessions (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_section_id BIGINT NOT NULL REFERENCES class_sections(id),
    session_date     DATE NOT NULL,
    start_time       TIME,
    end_time         TIME,
    topic            VARCHAR(255),
    taken_by         BIGINT REFERENCES staff(id)
);
CREATE INDEX ix_att_sessions ON attendance_sessions (class_section_id, session_date);

CREATE TABLE attendance_records (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id       BIGINT NOT NULL REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    enrollment_id    BIGINT NOT NULL REFERENCES enrollments(id),
    status           VARCHAR(10) NOT NULL CHECK (status IN ('PRESENT','ABSENT','LATE','EXCUSED')),
    leave_request_id BIGINT REFERENCES leave_requests(id),
    note             VARCHAR(255),
    UNIQUE (session_id, enrollment_id)
);

CREATE TABLE assessment_types (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    name_kh VARCHAR(50)
);

CREATE TABLE assessments (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_section_id   BIGINT NOT NULL REFERENCES class_sections(id) ON DELETE CASCADE,
    assessment_type_id BIGINT NOT NULL REFERENCES assessment_types(id),
    title              VARCHAR(150) NOT NULL,
    max_score          NUMERIC(6,2) NOT NULL,
    weight_percent     NUMERIC(5,2) NOT NULL CHECK (weight_percent BETWEEN 0 AND 100),
    due_date           DATE
);

CREATE TABLE assessment_scores (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    enrollment_id BIGINT NOT NULL REFERENCES enrollments(id),
    score         NUMERIC(6,2),
    graded_by     BIGINT REFERENCES staff(id),
    graded_at     TIMESTAMPTZ,
    UNIQUE (assessment_id, enrollment_id)
);

CREATE TABLE grade_scales (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    letter      VARCHAR(3) NOT NULL UNIQUE,
    min_score   NUMERIC(5,2) NOT NULL,
    max_score   NUMERIC(5,2) NOT NULL,
    grade_point NUMERIC(3,2) NOT NULL,
    is_pass     BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE final_grades (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    enrollment_id BIGINT NOT NULL UNIQUE REFERENCES enrollments(id),
    total_score   NUMERIC(5,2),
    letter_grade  VARCHAR(3),
    grade_point   NUMERIC(3,2),
    status        VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','PUBLISHED')),
    approved_by   BIGINT REFERENCES staff(id),
    published_at  TIMESTAMPTZ
);

CREATE TABLE student_semester_results (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id        BIGINT NOT NULL REFERENCES students(id),
    semester_id       BIGINT NOT NULL REFERENCES semesters(id),
    credits_attempted INT NOT NULL DEFAULT 0,
    credits_earned    INT NOT NULL DEFAULT 0,
    gpa               NUMERIC(3,2),
    cgpa              NUMERIC(3,2),
    academic_standing VARCHAR(20) CHECK (academic_standing IN ('GOOD','PROBATION','DISMISSED')),
    UNIQUE (student_id, semester_id)
);

-- ---------- 7. EXAMS (4) ----------
CREATE TABLE exam_schedules (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_section_id BIGINT NOT NULL REFERENCES class_sections(id),
    assessment_id    BIGINT REFERENCES assessments(id),
    exam_type        VARCHAR(10) NOT NULL CHECK (exam_type IN ('MIDTERM','FINAL','RE_EXAM')),
    exam_date        DATE NOT NULL,
    start_time       TIME NOT NULL,
    end_time         TIME NOT NULL,
    room_id          BIGINT REFERENCES rooms(id)
);

CREATE TABLE exam_seat_assignments (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    exam_schedule_id BIGINT NOT NULL REFERENCES exam_schedules(id) ON DELETE CASCADE,
    enrollment_id    BIGINT NOT NULL REFERENCES enrollments(id),
    seat_no          VARCHAR(10),
    is_eligible      BOOLEAN NOT NULL DEFAULT TRUE,
    ineligible_reason VARCHAR(100),
    attended         BOOLEAN,
    UNIQUE (exam_schedule_id, enrollment_id)
);

CREATE TABLE exam_invigilators (
    exam_schedule_id BIGINT NOT NULL REFERENCES exam_schedules(id) ON DELETE CASCADE,
    staff_id         BIGINT NOT NULL REFERENCES staff(id),
    role             VARCHAR(10) NOT NULL DEFAULT 'ASSISTANT' CHECK (role IN ('CHIEF','ASSISTANT')),
    PRIMARY KEY (exam_schedule_id, staff_id)
);

CREATE TABLE grade_appeals (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    final_grade_id BIGINT NOT NULL REFERENCES final_grades(id),
    student_id     BIGINT NOT NULL REFERENCES students(id),
    reason         TEXT NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','REVIEWING','ACCEPTED','REJECTED')),
    old_score      NUMERIC(5,2),
    new_score      NUMERIC(5,2),
    reviewed_by    BIGINT REFERENCES staff(id),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------- 8b. LEAVE & STUDENT REQUESTS (remaining) ----------
CREATE TABLE leave_request_classes (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    leave_request_id BIGINT NOT NULL REFERENCES leave_requests(id) ON DELETE CASCADE,
    class_section_id BIGINT NOT NULL REFERENCES class_sections(id),
    teacher_id       BIGINT NOT NULL REFERENCES staff(id),
    status           VARCHAR(10) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    comment          TEXT,
    decided_at       TIMESTAMPTZ,
    UNIQUE (leave_request_id, class_section_id)
);
CREATE INDEX ix_leave_classes_teacher ON leave_request_classes (teacher_id, status);

CREATE TABLE student_requests (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_no      VARCHAR(30) NOT NULL UNIQUE,
    student_id      BIGINT NOT NULL REFERENCES students(id),
    request_type    VARCHAR(30) NOT NULL CHECK (request_type IN ('ADD_DROP','PROGRAM_TRANSFER','LEAVE_OF_ABSENCE','WITHDRAWAL','RE_EXAM','DOCUMENT')),
    payload         JSONB,
    reason          TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','IN_REVIEW','APPROVED','REJECTED','CANCELLED')),
    conversation_id BIGINT UNIQUE REFERENCES conversations(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE request_approvals (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_request_id BIGINT NOT NULL REFERENCES student_requests(id) ON DELETE CASCADE,
    step_no            INT NOT NULL,
    approver_role_id   BIGINT NOT NULL REFERENCES roles(id),
    approver_id        BIGINT REFERENCES users(id),
    status             VARCHAR(10) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED','SKIPPED')),
    comment            TEXT,
    decided_at         TIMESTAMPTZ,
    UNIQUE (student_request_id, step_no)
);

-- ---------- 9. GRADUATION & TRANSCRIPTS (3) ----------
CREATE TABLE graduation_applications (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id        BIGINT NOT NULL REFERENCES students(id),
    academic_year_id  BIGINT NOT NULL REFERENCES academic_years(id),
    credits_completed INT,
    cgpa              NUMERIC(3,2),
    requirements_met  BOOLEAN NOT NULL DEFAULT FALSE,
    fees_cleared      BOOLEAN NOT NULL DEFAULT FALSE,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','VERIFIED','APPROVED','REJECTED')),
    approved_by       BIGINT REFERENCES staff(id),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (student_id, academic_year_id)
);

CREATE TABLE transcript_requests (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_no      VARCHAR(30) NOT NULL UNIQUE,
    student_id      BIGINT NOT NULL REFERENCES students(id),
    transcript_type VARCHAR(20) NOT NULL CHECK (transcript_type IN ('OFFICIAL','UNOFFICIAL')),
    copies          INT NOT NULL DEFAULT 1,
    invoice_id      BIGINT REFERENCES invoices(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','PAID','PRINTED','COLLECTED','CANCELLED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE certificates (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id        BIGINT NOT NULL REFERENCES students(id),
    certificate_type  VARCHAR(20) NOT NULL CHECK (certificate_type IN ('DEGREE','ENROLLMENT','COMPLETION')),
    certificate_no    VARCHAR(30) NOT NULL UNIQUE,
    issue_date        DATE NOT NULL,
    verification_code VARCHAR(64) NOT NULL UNIQUE,
    file_url          VARCHAR(500)
);

-- ---------- 12. SUPPORTING (3) ----------
CREATE TABLE announcements (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    title_kh     VARCHAR(200),
    content      TEXT NOT NULL,
    content_kh   TEXT,
    target_type  VARCHAR(20) NOT NULL DEFAULT 'ALL' CHECK (target_type IN ('ALL','ROLE','FACULTY','PROGRAM','CLASS_SECTION')),
    target_id    BIGINT,
    published_by BIGINT REFERENCES users(id),
    published_at TIMESTAMPTZ,
    expires_at   TIMESTAMPTZ
);

CREATE TABLE documents (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_type  VARCHAR(30) NOT NULL,
    owner_id    BIGINT NOT NULL,
    doc_type    VARCHAR(30),
    file_name   VARCHAR(255) NOT NULL,
    file_url    VARCHAR(500) NOT NULL,
    mime_type   VARCHAR(100),
    uploaded_by BIGINT REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_documents_owner ON documents (owner_type, owner_id);

CREATE TABLE system_settings (
    key         VARCHAR(100) PRIMARY KEY,
    value       TEXT,
    description TEXT,
    updated_by  BIGINT REFERENCES users(id),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =====================================================================
-- EXTENDED MODULES (13-20)
-- =====================================================================

-- ---------- 13. HR & PAYROLL (9) ----------
CREATE TABLE employment_contracts (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    contract_no   VARCHAR(30) NOT NULL UNIQUE,
    staff_id      BIGINT NOT NULL REFERENCES staff(id),
    contract_type VARCHAR(20) NOT NULL CHECK (contract_type IN ('FULL_TIME','PART_TIME','VISITING')),
    start_date    DATE NOT NULL,
    end_date      DATE,
    base_salary   NUMERIC(12,2) NOT NULL DEFAULT 0,
    hourly_rate   NUMERIC(12,2),
    currency      VARCHAR(3) NOT NULL DEFAULT 'USD' CHECK (currency IN ('USD','KHR')),
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','EXPIRED','TERMINATED')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_contracts_staff ON employment_contracts (staff_id);

CREATE TABLE salary_components (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code           VARCHAR(30) NOT NULL UNIQUE,
    name           VARCHAR(100) NOT NULL,
    name_kh        VARCHAR(100),
    component_type VARCHAR(20) NOT NULL CHECK (component_type IN ('ALLOWANCE','DEDUCTION')),
    is_taxable     BOOLEAN NOT NULL DEFAULT TRUE,
    calc_type      VARCHAR(10) NOT NULL DEFAULT 'FIXED' CHECK (calc_type IN ('FIXED','PERCENT'))
);

CREATE TABLE staff_salary_components (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_id     BIGINT NOT NULL REFERENCES staff(id),
    component_id BIGINT NOT NULL REFERENCES salary_components(id),
    amount       NUMERIC(12,2) NOT NULL,
    valid_from   DATE NOT NULL,
    valid_to     DATE
);

CREATE TABLE staff_leave_types (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code          VARCHAR(20) NOT NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    name_kh       VARCHAR(100),
    days_per_year NUMERIC(5,1),
    is_paid       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE staff_leave_requests (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_id      BIGINT NOT NULL REFERENCES staff(id),
    leave_type_id BIGINT NOT NULL REFERENCES staff_leave_types(id),
    from_date     DATE NOT NULL,
    to_date       DATE NOT NULL,
    days          NUMERIC(5,1) NOT NULL,
    reason        TEXT,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED')),
    approved_by   BIGINT REFERENCES staff(id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (to_date >= from_date)
);

CREATE TABLE teaching_loads (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_id         BIGINT NOT NULL REFERENCES staff(id),
    class_section_id BIGINT NOT NULL REFERENCES class_sections(id),
    semester_id      BIGINT NOT NULL REFERENCES semesters(id),
    hours_planned    NUMERIC(6,1) NOT NULL DEFAULT 0,
    hours_taught     NUMERIC(6,1) NOT NULL DEFAULT 0,
    rate             NUMERIC(12,2),
    amount           NUMERIC(12,2),
    UNIQUE (staff_id, class_section_id)
);

CREATE TABLE payroll_periods (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    name_kh     VARCHAR(50),
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','PROCESSING','APPROVED','PAID')),
    approved_by BIGINT REFERENCES staff(id)
);

CREATE TABLE payslips (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payroll_period_id BIGINT NOT NULL REFERENCES payroll_periods(id),
    staff_id          BIGINT NOT NULL REFERENCES staff(id),
    contract_id       BIGINT REFERENCES employment_contracts(id),
    gross             NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_allowance   NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_deduction   NUMERIC(12,2) NOT NULL DEFAULT 0,
    tax               NUMERIC(12,2) NOT NULL DEFAULT 0,
    net               NUMERIC(12,2) NOT NULL DEFAULT 0,
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','APPROVED','PAID')),
    UNIQUE (payroll_period_id, staff_id)
);

CREATE TABLE payslip_items (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payslip_id   BIGINT NOT NULL REFERENCES payslips(id) ON DELETE CASCADE,
    component_id BIGINT REFERENCES salary_components(id),
    description  VARCHAR(200),
    item_type    VARCHAR(20) NOT NULL CHECK (item_type IN ('BASIC','TEACHING','ALLOWANCE','DEDUCTION','TAX')),
    amount       NUMERIC(12,2) NOT NULL
);

-- ---------- 14. LIBRARY (7) ----------
CREATE TABLE library_categories (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code      VARCHAR(20) NOT NULL UNIQUE,
    name      VARCHAR(100) NOT NULL,
    name_kh   VARCHAR(100),
    parent_id BIGINT REFERENCES library_categories(id)
);

CREATE TABLE books (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn         VARCHAR(20),
    title        VARCHAR(300) NOT NULL,
    authors      VARCHAR(300),
    publisher    VARCHAR(150),
    publish_year INT,
    edition      VARCHAR(30),
    language     VARCHAR(20),
    category_id  BIGINT REFERENCES library_categories(id),
    cover_url    VARCHAR(500),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_books_isbn ON books (isbn);

CREATE TABLE book_copies (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id        BIGINT NOT NULL REFERENCES books(id),
    barcode        VARCHAR(50) NOT NULL UNIQUE,
    shelf_location VARCHAR(50),
    acquired_date  DATE,
    price          NUMERIC(12,2),
    status         VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE','ON_LOAN','RESERVED','LOST','DAMAGED','WITHDRAWN'))
);

CREATE TABLE book_loans (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    copy_id     BIGINT NOT NULL REFERENCES book_copies(id),
    user_id     BIGINT NOT NULL REFERENCES users(id),
    issued_by   BIGINT REFERENCES users(id),
    borrowed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    due_date    DATE NOT NULL,
    returned_at TIMESTAMPTZ,
    renew_count INT NOT NULL DEFAULT 0,
    status      VARCHAR(20) NOT NULL DEFAULT 'ON_LOAN' CHECK (status IN ('ON_LOAN','RETURNED','OVERDUE','LOST'))
);
CREATE INDEX ix_loans_user ON book_loans (user_id, status);
CREATE UNIQUE INDEX ux_loans_open_copy ON book_loans (copy_id) WHERE returned_at IS NULL;

CREATE TABLE book_reservations (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id     BIGINT NOT NULL REFERENCES books(id),
    user_id     BIGINT NOT NULL REFERENCES users(id),
    reserved_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ,
    status      VARCHAR(20) NOT NULL DEFAULT 'WAITING' CHECK (status IN ('WAITING','READY','FULFILLED','CANCELLED','EXPIRED'))
);

CREATE TABLE library_fines (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    loan_id    BIGINT NOT NULL REFERENCES book_loans(id),
    user_id    BIGINT NOT NULL REFERENCES users(id),
    reason     VARCHAR(20) NOT NULL CHECK (reason IN ('OVERDUE','LOST','DAMAGED')),
    amount     NUMERIC(12,2) NOT NULL,
    invoice_id BIGINT REFERENCES invoices(id),
    status     VARCHAR(20) NOT NULL DEFAULT 'UNPAID' CHECK (status IN ('UNPAID','PAID','WAIVED')),
    waived_by  BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Borrowing rules per kind of borrower; the librarian changes them in Settings.
-- The borrower type comes from what the person is: a student's current program level, or a staff member's type.
-- Someone who is two kinds at once (a lecturer studying a PhD) gets the more generous rules.
CREATE TABLE library_loan_policies (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    borrower_type          VARCHAR(20) NOT NULL UNIQUE
                           CHECK (borrower_type IN ('UNDERGRADUATE','POSTGRADUATE','LECTURER','STAFF')),  -- UNDERGRADUATE: Foundation to Bachelor; POSTGRADUATE: Master and PhD
    max_books              INT NOT NULL CHECK (max_books >= 0),            -- books on loan at the same time; 0 = may not borrow
    loan_days              INT NOT NULL CHECK (loan_days > 0),
    max_renewals           INT NOT NULL DEFAULT 0 CHECK (max_renewals >= 0),
    renew_days             INT NOT NULL DEFAULT 0 CHECK (renew_days >= 0), -- days added per renewal
    fine_per_day           NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (fine_per_day >= 0),
    max_fine_per_loan      NUMERIC(12,2) CHECK (max_fine_per_loan >= 0),   -- cap on the overdue fine; NULL = no cap
    lost_processing_fee    NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (lost_processing_fee >= 0),  -- charged on top of the copy's price
    currency               VARCHAR(3) NOT NULL DEFAULT 'USD' CHECK (currency IN ('USD','KHR')),
    reservation_hold_days  INT NOT NULL DEFAULT 3 CHECK (reservation_hold_days > 0),  -- READY reservations expire after this
    block_on_unpaid_fines  BOOLEAN NOT NULL DEFAULT TRUE,                  -- no new loans while a fine is UNPAID
    updated_by             BIGINT REFERENCES users(id),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO library_loan_policies
    (borrower_type, max_books, loan_days, max_renewals, renew_days, fine_per_day, max_fine_per_loan, lost_processing_fee, reservation_hold_days, block_on_unpaid_fines)
VALUES
    ('UNDERGRADUATE',  3, 14, 1,  7, 0.25, 10.00, 5.00, 3, TRUE),
    ('POSTGRADUATE',   5, 30, 2, 14, 0.25, 10.00, 5.00, 3, TRUE),
    ('LECTURER',      10, 60, 3, 30, 0.00,  NULL, 5.00, 5, FALSE),
    ('STAFF',          3, 14, 1,  7, 0.25, 10.00, 5.00, 3, TRUE)
ON CONFLICT (borrower_type) DO NOTHING;

-- ---------- 15. HOSTEL (5) ----------
CREATE TABLE hostels (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code            VARCHAR(20) NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    name_kh         VARCHAR(100),
    gender_type     VARCHAR(10) NOT NULL CHECK (gender_type IN ('MALE','FEMALE','MIXED')),
    address         TEXT,
    warden_staff_id BIGINT REFERENCES staff(id)
);

CREATE TABLE hostel_rooms (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hostel_id          BIGINT NOT NULL REFERENCES hostels(id),
    room_no            VARCHAR(20) NOT NULL,
    floor              INT,
    room_type          VARCHAR(10) NOT NULL CHECK (room_type IN ('SINGLE','DOUBLE','QUAD')),
    capacity           INT NOT NULL,
    price_per_semester NUMERIC(12,2) NOT NULL,
    status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    UNIQUE (hostel_id, room_no)
);

CREATE TABLE hostel_beds (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hostel_room_id BIGINT NOT NULL REFERENCES hostel_rooms(id),
    bed_no         VARCHAR(10) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE','OCCUPIED','MAINTENANCE')),
    UNIQUE (hostel_room_id, bed_no)
);

CREATE TABLE hostel_allocations (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id     BIGINT NOT NULL REFERENCES students(id),
    bed_id         BIGINT NOT NULL REFERENCES hostel_beds(id),
    semester_id    BIGINT NOT NULL REFERENCES semesters(id),
    check_in_date  DATE,
    check_out_date DATE,
    invoice_id     BIGINT REFERENCES invoices(id),
    status         VARCHAR(20) NOT NULL DEFAULT 'RESERVED' CHECK (status IN ('RESERVED','ACTIVE','CHECKED_OUT','CANCELLED'))
);
CREATE UNIQUE INDEX ux_hostel_bed_active     ON hostel_allocations (bed_id)     WHERE status = 'ACTIVE';
CREATE UNIQUE INDEX ux_hostel_student_active ON hostel_allocations (student_id) WHERE status = 'ACTIVE';

CREATE TABLE hostel_maintenance_requests (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hostel_room_id BIGINT NOT NULL REFERENCES hostel_rooms(id),
    reported_by    BIGINT REFERENCES users(id),
    description    TEXT NOT NULL,
    priority       VARCHAR(10) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW','MEDIUM','HIGH')),
    status         VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','IN_PROGRESS','DONE')),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at    TIMESTAMPTZ
);

-- ---------- 16. TRANSPORT (4) ----------
CREATE TABLE vehicles (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plate_no     VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type VARCHAR(10) NOT NULL CHECK (vehicle_type IN ('BUS','VAN')),
    capacity     INT NOT NULL,
    driver_name  VARCHAR(100),
    driver_phone VARCHAR(30),
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE transport_routes (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code              VARCHAR(20) NOT NULL UNIQUE,
    name              VARCHAR(100) NOT NULL,
    name_kh           VARCHAR(100),
    vehicle_id        BIGINT REFERENCES vehicles(id),
    morning_departure TIME,
    evening_departure TIME,
    fee_per_semester  NUMERIC(12,2) NOT NULL DEFAULT 0,
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE route_stops (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    route_id    BIGINT NOT NULL REFERENCES transport_routes(id) ON DELETE CASCADE,
    stop_order  INT NOT NULL,
    name        VARCHAR(100) NOT NULL,
    name_kh     VARCHAR(100),
    latitude    NUMERIC(9,6),
    longitude   NUMERIC(9,6),
    pickup_time TIME,
    UNIQUE (route_id, stop_order)
);

CREATE TABLE transport_subscriptions (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id  BIGINT NOT NULL REFERENCES students(id),
    route_id    BIGINT NOT NULL REFERENCES transport_routes(id),
    stop_id     BIGINT REFERENCES route_stops(id),
    semester_id BIGINT NOT NULL REFERENCES semesters(id),
    invoice_id  BIGINT REFERENCES invoices(id),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CANCELLED')),
    UNIQUE (student_id, semester_id)
);

-- ---------- 17. LMS (5) ----------
CREATE TABLE course_materials (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_section_id BIGINT NOT NULL REFERENCES class_sections(id) ON DELETE CASCADE,
    week_no          INT,
    title            VARCHAR(200) NOT NULL,
    material_type    VARCHAR(10) NOT NULL CHECK (material_type IN ('FILE','LINK','VIDEO','NOTE')),
    file_url         VARCHAR(500),
    link_url         VARCHAR(500),
    content          TEXT,
    published_at     TIMESTAMPTZ,
    created_by       BIGINT REFERENCES staff(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE assignment_submissions (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    enrollment_id BIGINT NOT NULL REFERENCES enrollments(id),
    file_url      VARCHAR(500),
    text_answer   TEXT,
    submitted_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_late       BOOLEAN NOT NULL DEFAULT FALSE,
    attempt_no    INT NOT NULL DEFAULT 1,
    feedback      TEXT,
    UNIQUE (assessment_id, enrollment_id, attempt_no)
);

CREATE TABLE quiz_questions (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type VARCHAR(10) NOT NULL CHECK (question_type IN ('SINGLE','MULTIPLE','TRUE_FALSE','SHORT')),
    points        NUMERIC(6,2) NOT NULL DEFAULT 1,
    sort_order    INT NOT NULL DEFAULT 0
);

CREATE TABLE quiz_question_options (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    is_correct  BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order  INT NOT NULL DEFAULT 0
);

CREATE TABLE quiz_attempts (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    enrollment_id BIGINT NOT NULL REFERENCES enrollments(id),
    attempt_no    INT NOT NULL DEFAULT 1,
    started_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    submitted_at  TIMESTAMPTZ,
    answers       JSONB,
    score         NUMERIC(6,2),
    status        VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS','SUBMITTED','GRADED')),
    UNIQUE (assessment_id, enrollment_id, attempt_no)
);

-- ---------- 18. TIMETABLE (4) ----------
CREATE TABLE lecturer_availability (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_id     BIGINT NOT NULL REFERENCES staff(id),
    semester_id  BIGINT NOT NULL REFERENCES semesters(id),
    day_of_week  INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (end_time > start_time)
);

CREATE TABLE timetable_constraints (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    semester_id     BIGINT NOT NULL REFERENCES semesters(id),
    constraint_type VARCHAR(30) NOT NULL CHECK (constraint_type IN ('NO_ROOM_CLASH','NO_LECTURER_CLASH','NO_BATCH_CLASH','MAX_HOURS_PER_DAY','PREFERRED_ROOM','BREAK_TIME')),
    scope_type      VARCHAR(20),
    scope_id        BIGINT,
    value           JSONB,
    is_hard         BOOLEAN NOT NULL DEFAULT TRUE,
    weight          INT NOT NULL DEFAULT 1
);

CREATE TABLE timetable_runs (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    semester_id    BIGINT NOT NULL REFERENCES semesters(id),
    started_by     BIGINT REFERENCES users(id),
    started_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at    TIMESTAMPTZ,
    status         VARCHAR(20) NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING','DONE','FAILED','APPROVED')),
    conflict_count INT,
    score          NUMERIC(10,2)
);

CREATE TABLE timetable_run_items (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    run_id           BIGINT NOT NULL REFERENCES timetable_runs(id) ON DELETE CASCADE,
    class_section_id BIGINT NOT NULL REFERENCES class_sections(id),
    room_id          BIGINT REFERENCES rooms(id),
    day_of_week      INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time       TIME NOT NULL,
    end_time         TIME NOT NULL
);
CREATE INDEX ix_run_items_run ON timetable_run_items (run_id);

-- ---------- 19. ALUMNI, EVENTS & SURVEYS (6) ----------
CREATE TABLE alumni_profiles (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id         BIGINT NOT NULL UNIQUE REFERENCES students(id),
    graduation_year    INT NOT NULL,
    current_employer   VARCHAR(150),
    job_title          VARCHAR(100),
    industry           VARCHAR(100),
    linkedin_url       VARCHAR(300),
    consent_to_contact BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE events (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    title_kh     VARCHAR(200),
    description  TEXT,
    description_kh TEXT,
    event_type   VARCHAR(20) NOT NULL CHECK (event_type IN ('SEMINAR','WORKSHOP','CEREMONY','ALUMNI','SPORTS')),
    start_at     TIMESTAMPTZ NOT NULL,
    end_at       TIMESTAMPTZ,
    venue        VARCHAR(200),
    room_id      BIGINT REFERENCES rooms(id),
    organizer_id BIGINT REFERENCES users(id),
    capacity     INT,
    status       VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED','CANCELLED','COMPLETED'))
);

CREATE TABLE event_registrations (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id      BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id       BIGINT NOT NULL REFERENCES users(id),
    registered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    status        VARCHAR(20) NOT NULL DEFAULT 'REGISTERED' CHECK (status IN ('REGISTERED','CANCELLED')),
    attended      BOOLEAN,
    UNIQUE (event_id, user_id)
);

CREATE TABLE surveys (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    title_kh     VARCHAR(200),
    survey_type  VARCHAR(30) NOT NULL CHECK (survey_type IN ('TEACHER_EVALUATION','COURSE_FEEDBACK','GENERAL','ALUMNI')),
    semester_id  BIGINT REFERENCES semesters(id),
    is_anonymous BOOLEAN NOT NULL DEFAULT TRUE,
    open_at      TIMESTAMPTZ,
    close_at     TIMESTAMPTZ,
    created_by   BIGINT REFERENCES users(id),
    status       VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','OPEN','CLOSED'))
);

CREATE TABLE survey_questions (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    survey_id     BIGINT NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_text_kh TEXT,
    question_type VARCHAR(10) NOT NULL CHECK (question_type IN ('RATING','TEXT','SINGLE','MULTIPLE')),
    options       JSONB,
    is_required   BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order    INT NOT NULL DEFAULT 0
);

CREATE TABLE survey_responses (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    survey_id        BIGINT NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    class_section_id BIGINT REFERENCES class_sections(id),
    respondent_id    BIGINT REFERENCES users(id),     -- NULL when the survey is anonymous
    respondent_token VARCHAR(64) NOT NULL,            -- one-way hash, blocks double submit
    answers          JSONB NOT NULL,
    submitted_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_survey_once ON survey_responses (survey_id, COALESCE(class_section_id, 0), respondent_token);

-- ---------- 20. ASSETS (4) ----------
CREATE TABLE asset_categories (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code               VARCHAR(20) NOT NULL UNIQUE,
    name               VARCHAR(100) NOT NULL,
    name_kh            VARCHAR(100),
    depreciation_years INT
);

CREATE TABLE assets (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_code     VARCHAR(30) NOT NULL UNIQUE,
    name           VARCHAR(200) NOT NULL,
    category_id    BIGINT NOT NULL REFERENCES asset_categories(id),
    serial_no      VARCHAR(100),
    room_id        BIGINT REFERENCES rooms(id),
    department_id  BIGINT REFERENCES departments(id),
    purchase_date  DATE,
    purchase_cost  NUMERIC(12,2),
    supplier       VARCHAR(150),
    warranty_until DATE,
    status         VARCHAR(20) NOT NULL DEFAULT 'IN_USE' CHECK (status IN ('IN_USE','IN_STORE','UNDER_REPAIR','DISPOSED'))
);

CREATE TABLE asset_assignments (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id       BIGINT NOT NULL REFERENCES assets(id),
    staff_id       BIGINT NOT NULL REFERENCES staff(id),
    assigned_at    DATE NOT NULL DEFAULT CURRENT_DATE,
    returned_at    DATE,
    condition_note TEXT
);
CREATE UNIQUE INDEX ux_asset_open_assignment ON asset_assignments (asset_id) WHERE returned_at IS NULL;

CREATE TABLE asset_maintenance (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id         BIGINT NOT NULL REFERENCES assets(id),
    maintenance_date DATE NOT NULL,
    description      TEXT NOT NULL,
    cost             NUMERIC(12,2),
    vendor           VARCHAR(150),
    next_due_date    DATE
);

-- ---------- Session defaults (change in Settings; never hard-code) ----------
INSERT INTO system_settings (key, value, description) VALUES
    ('session.mobile.access_token_minutes', '30', 'Access token lifetime on Android / iOS'),
    ('session.web.access_token_minutes', '15', 'Access token lifetime in the browser'),
    ('session.default_ttl_days', '180', 'Default auto-terminate for inactive sessions (users can change it)'),
    ('session.web.remember_days', '30', 'Web session length when Remember me is ticked'),
    ('session.web.max_hours', '8', 'Web session length when Remember me is not ticked'),
    ('session.web.idle_minutes.staff', '30', 'Web idle logout for staff (finance, grades, admin)'),
    ('session.web.idle_minutes.student', '120', 'Web idle logout for students'),
    ('session.max_devices.student', '0', 'Max active sessions per student (0 = unlimited)'),
    ('session.max_devices.staff', '0', 'Max active sessions per staff member (0 = unlimited)'),
    ('session.new_login_alert', 'true', 'Alert other devices when a new session is created'),
    ('session.qr_login.enabled', 'false', 'QR login for lab and library PCs'),
    ('session.rule_24h.enabled', 'false', 'Sessions under 24 hours old cannot terminate older sessions')
ON CONFLICT (key) DO NOTHING;
