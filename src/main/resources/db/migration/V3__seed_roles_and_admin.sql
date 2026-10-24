-- =====================================================================
-- V3__seed_roles_and_admin.sql  -  system roles and the first admin
-- =====================================================================

INSERT INTO roles (code, name, name_kh, description, description_kh, is_system) VALUES
    ('SUPER_ADMIN', 'Super Administrator', 'អ្នកគ្រប់គ្រងប្រព័ន្ធជាន់ខ្ពស់', 'Full access to every module',        'មានសិទ្ធិគ្រប់ផ្នែកទាំងអស់',            TRUE),
    ('ADMIN',       'Administrator',       'អ្នកគ្រប់គ្រងរដ្ឋបាល',          'University administration staff',    'បុគ្គលិករដ្ឋបាលសាកលវិទ្យាល័យ',        TRUE),
    ('REGISTRAR',   'Registrar',           'ការិយាល័យសិក្សា',              'Admissions, enrollment and records', 'ការចុះឈ្មោះចូលរៀន ការសិក្សា និងឯកសារ', TRUE),
    ('ACCOUNTANT',  'Accountant',          'គណនេយ្យករ',                   'Fees, invoices and payments',        'ថ្លៃសិក្សា វិក្កយបត្រ និងការបង់ប្រាក់',   TRUE),
    ('LECTURER',    'Lecturer',            'សាស្ត្រាចារ្យ',                 'Teaching staff',                     'បុគ្គលិកបង្រៀន',                     TRUE),
    ('STUDENT',     'Student',             'និស្សិត',                      'Enrolled students',                  'និស្សិតកំពុងសិក្សា',                   TRUE),
    ('GUARDIAN',    'Guardian',            'អាណាព្យាបាល',                  'Parents and guardians',              'ឪពុកម្តាយ ឬអាណាព្យាបាល',              TRUE),
    ('APPLICANT',   'Applicant',           'បេក្ខជន',                      'Prospective students',               'អ្នកដាក់ពាក្យចូលរៀន',                  TRUE)
ON CONFLICT (code) DO NOTHING;

-- SUPER_ADMIN gets every permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- Default permissions of the other system roles. Only a starting point:
-- admins change them later with the roles API (/api/v1/roles).
-- "VIEW" permissions for students, guardians and lecturers mean their OWN
-- records (own grades, own children, own classes); the services filter that.
-- ---------------------------------------------------------------------
CREATE TEMP TABLE role_permission_defaults (role_code VARCHAR(50), permission_code VARCHAR(80)) ON COMMIT DROP;

INSERT INTO role_permission_defaults (role_code, permission_code)
SELECT d.role_code, unnest(d.codes) FROM (VALUES
    -- every staff role: chat, own leave and payslip, library, events, surveys
    ('ADMIN',      ARRAY['CHAT_SEND','CHAT_CREATE_GROUP','STAFF_LEAVE_CREATE','PAYSLIP_VIEW_OWN','RESERVATION_CREATE','EVENT_REGISTER','SURVEY_RESPOND']),
    ('REGISTRAR',  ARRAY['CHAT_SEND','CHAT_CREATE_GROUP','STAFF_LEAVE_CREATE','PAYSLIP_VIEW_OWN','RESERVATION_CREATE','EVENT_REGISTER','SURVEY_RESPOND']),
    ('ACCOUNTANT', ARRAY['CHAT_SEND','CHAT_CREATE_GROUP','STAFF_LEAVE_CREATE','PAYSLIP_VIEW_OWN','RESERVATION_CREATE','EVENT_REGISTER','SURVEY_RESPOND']),
    ('LECTURER',   ARRAY['CHAT_SEND','CHAT_CREATE_GROUP','STAFF_LEAVE_CREATE','PAYSLIP_VIEW_OWN','RESERVATION_CREATE','EVENT_REGISTER','SURVEY_RESPOND']),

    -- ADMIN: accounts, organization, HR, campus services. Roles stay with SUPER_ADMIN (ROLE_MANAGE).
    ('ADMIN', ARRAY[
        'USER_VIEW','USER_CREATE','USER_UPDATE','USER_LOCK','ROLE_VIEW','AUDIT_VIEW',
        'FACULTY_MANAGE','DEPARTMENT_MANAGE','PROGRAM_MANAGE','ROOM_MANAGE',
        'STUDENT_VIEW','STAFF_VIEW','STAFF_MANAGE','GEO_AREA_MANAGE',
        'ANNOUNCEMENT_PUBLISH','SETTINGS_MANAGE','REPORT_EXPORT',
        'CONTRACT_MANAGE','SALARY_COMPONENT_MANAGE','STAFF_LEAVE_APPROVE','PAYROLL_APPROVE',
        'BOOK_MANAGE','LOAN_ISSUE','LOAN_RETURN','FINE_WAIVE','LIBRARY_POLICY_MANAGE',
        'HOSTEL_MANAGE','HOSTEL_ALLOCATE','HOSTEL_MAINTENANCE_MANAGE',
        'TRANSPORT_MANAGE','TRANSPORT_SUBSCRIBE',
        'ALUMNI_MANAGE','EVENT_MANAGE','SURVEY_MANAGE','SURVEY_RESULT_VIEW',
        'ASSET_MANAGE','ASSET_ASSIGN','ASSET_MAINTENANCE_MANAGE']),

    -- REGISTRAR: admission, student records, academic setup, enrollment, exams, graduation
    ('REGISTRAR', ARRAY[
        'USER_VIEW','USER_UPDATE','USER_LOCK',
        'STUDENT_VIEW','STUDENT_CREATE','STUDENT_UPDATE','STAFF_VIEW','GUARDIAN_MANAGE',
        'COURSE_MANAGE','CURRICULUM_MANAGE','SEMESTER_MANAGE','CLASS_SECTION_MANAGE','SCHEDULE_MANAGE',
        'FORM_SALE_CREATE','FORM_SALE_VOID','APPLICATION_VIEW','APPLICATION_REVIEW','APPLICATION_DECIDE','APPLICANT_ENROLL',
        'ENROLLMENT_MANAGE','ATTENDANCE_VIEW','GRADE_APPROVE','GRADE_PUBLISH',
        'EXAM_SCHEDULE_MANAGE','EXAM_SEAT_MANAGE','GRADE_APPEAL_REVIEW',
        'LEAVE_REQUEST_VIEW_ALL','STUDENT_REQUEST_APPROVE',
        'GRADUATION_VERIFY','GRADUATION_APPROVE','TRANSCRIPT_ISSUE','CERTIFICATE_ISSUE','THESIS_MANAGE',
        'TEACHING_LOAD_MANAGE','AVAILABILITY_MANAGE','TIMETABLE_GENERATE','TIMETABLE_APPROVE',
        'INVOICE_VIEW','ANNOUNCEMENT_PUBLISH','REPORT_EXPORT']),

    -- ACCOUNTANT: fees, invoices, payments, scholarships, payroll (ADMIN approves payroll)
    ('ACCOUNTANT', ARRAY[
        'STUDENT_VIEW','APPLICATION_VIEW','FORM_SALE_CREATE','FORM_SALE_VOID',
        'FEE_MANAGE','INVOICE_VIEW','INVOICE_CREATE','PAYMENT_RECEIVE','PAYMENT_REVERSE',
        'SCHOLARSHIP_MANAGE','FINANCE_REPORT_VIEW','PAYROLL_PROCESS',
        'STUDENT_REQUEST_APPROVE','REPORT_EXPORT']),

    -- LECTURER: own classes (REGISTRAR approves and publishes the grades)
    ('LECTURER', ARRAY[
        'STUDENT_VIEW','ATTENDANCE_TAKE','ATTENDANCE_VIEW','SCORE_ENTER','GRADE_SUBMIT','GRADE_APPEAL_REVIEW',
        'LEAVE_REQUEST_APPROVE','STUDENT_REQUEST_APPROVE',
        'MATERIAL_MANAGE','QUIZ_MANAGE','AVAILABILITY_MANAGE','THESIS_SUPERVISE']),

    -- STUDENT: own classes, grades, invoices and requests
    ('STUDENT', ARRAY[
        'ENROLLMENT_SELF','ATTENDANCE_VIEW','GRADE_VIEW_OWN','GRADE_APPEAL_CREATE',
        'LEAVE_REQUEST_CREATE','STUDENT_REQUEST_CREATE','INVOICE_VIEW',
        'CHAT_SEND','CHAT_CREATE_GROUP','ASSIGNMENT_SUBMIT','QUIZ_TAKE',
        'RESERVATION_CREATE','EVENT_REGISTER','SURVEY_RESPOND','THESIS_SUBMIT']),

    -- GUARDIAN: their children's attendance, grades and invoices, and chat with the school
    ('GUARDIAN', ARRAY['ATTENDANCE_VIEW','GRADE_VIEW_OWN','INVOICE_VIEW','CHAT_SEND']),

    -- APPLICANT: own admission invoice (the application itself is theirs by ownership)
    ('APPLICANT', ARRAY['INVOICE_VIEW'])
) AS d(role_code, codes);

-- Stop here if a code above is misspelled, instead of silently leaving it out
DO $$
DECLARE unknown TEXT;
BEGIN
    SELECT string_agg(DISTINCT d.permission_code, ', ') INTO unknown
    FROM role_permission_defaults d LEFT JOIN permissions p ON p.code = d.permission_code
    WHERE p.id IS NULL;
    IF unknown IS NOT NULL THEN
        RAISE EXCEPTION 'Unknown permission codes in V3 defaults: %', unknown;
    END IF;
END $$;

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT r.id, p.id
FROM role_permission_defaults d
JOIN roles r ON r.code = d.role_code
JOIN permissions p ON p.code = d.permission_code
ON CONFLICT DO NOTHING;

-- First admin: not inserted here (a password must never sit in a migration). On a new server set
-- UMS_ADMIN_USERNAME / UMS_ADMIN_PASSWORD / UMS_ADMIN_EMAIL; FirstAdminSetup creates the SUPER_ADMIN at start-up.
-- The dev profile gets one from db/testdata/V1000__sample_data.sql.
