-- Sample data for local development only (loaded by the dev profile).
-- Login: admin / Admin@12345

INSERT INTO users (username, email, password_hash, user_type)
VALUES ('admin', 'admin@ums.local', '$2a$10$jV1wmmr1aKRMvhY3LVarnOV2RfYabge0YXiILO2D6IcF63Rw6mcU2', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO faculties (code, name) VALUES ('FIT', 'Faculty of Information Technology') ON CONFLICT (code) DO NOTHING;

INSERT INTO departments (faculty_id, code, name)
SELECT id, 'CS', 'Computer Science' FROM faculties WHERE code = 'FIT'
ON CONFLICT (code) DO NOTHING;

INSERT INTO programs (department_id, code, name, degree_level, duration_years, total_credits)
SELECT id, 'BSCS', 'Bachelor of Computer Science', 'BACHELOR', 4, 132 FROM departments WHERE code = 'CS'
ON CONFLICT (code) DO NOTHING;

INSERT INTO batches (program_id, name, start_year)
SELECT id, 'Batch 2026', 2026 FROM programs WHERE code = 'BSCS'
ON CONFLICT (program_id, name) DO NOTHING;

-- Other study levels, for students who continue (Bachelor → Master → PhD) or study two programs
INSERT INTO programs (department_id, code, name, degree_level, duration_years, total_credits)
SELECT id, 'MSCS', 'Master of Computer Science', 'MASTER', 2, 45 FROM departments WHERE code = 'CS'
ON CONFLICT (code) DO NOTHING;

INSERT INTO programs (department_id, code, name, degree_level, duration_years, total_credits)
SELECT id, 'PHDCS', 'Doctor of Philosophy in Computer Science', 'PHD', 3, 60 FROM departments WHERE code = 'CS'
ON CONFLICT (code) DO NOTHING;

INSERT INTO programs (department_id, code, name, degree_level, duration_years, total_credits)
SELECT id, 'FDN', 'Foundation Year', 'FOUNDATION', 1, 30 FROM departments WHERE code = 'CS'
ON CONFLICT (code) DO NOTHING;
