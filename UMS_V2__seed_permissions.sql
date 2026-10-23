-- =====================================================================
-- V2__seed_permissions.sql  -  98 permission codes for all 20 modules
-- Safe to re-run: existing codes are updated, new codes are added.
-- (Can be renamed to R__seed_permissions.sql to keep it as a repeatable list.)
-- Codes must match the constants in each module's *Permissions.java
-- =====================================================================

INSERT INTO permissions (code, module, action, description) VALUES
    -- SECURITY
    ('USER_VIEW',                 'SECURITY',     'VIEW',    'View user accounts'),
    ('USER_CREATE',               'SECURITY',     'CREATE',  'Create user accounts'),
    ('USER_UPDATE',               'SECURITY',     'UPDATE',  'Edit user accounts and reset passwords'),
    ('USER_LOCK',                 'SECURITY',     'UPDATE',  'Lock and unlock user accounts'),
    ('ROLE_VIEW',                 'SECURITY',     'VIEW',    'View roles and their permissions'),
    ('ROLE_MANAGE',               'SECURITY',     'MANAGE',  'Create, edit and delete roles and assign permissions'),
    ('AUDIT_VIEW',                'SECURITY',     'VIEW',    'View the audit log'),
    -- ORGANIZATION
    ('FACULTY_MANAGE',            'ORGANIZATION', 'MANAGE',  'Manage faculties'),
    ('DEPARTMENT_MANAGE',         'ORGANIZATION', 'MANAGE',  'Manage departments'),
    ('PROGRAM_MANAGE',            'ORGANIZATION', 'MANAGE',  'Manage programs'),
    ('ROOM_MANAGE',               'ORGANIZATION', 'MANAGE',  'Manage buildings and rooms'),
    -- PEOPLE
    ('STUDENT_VIEW',              'PEOPLE',       'VIEW',    'View student records'),
    ('STUDENT_CREATE',            'PEOPLE',       'CREATE',  'Create student records'),
    ('STUDENT_UPDATE',            'PEOPLE',       'UPDATE',  'Edit student records and status'),
    ('STAFF_VIEW',                'PEOPLE',       'VIEW',    'View staff records'),
    ('STAFF_MANAGE',              'PEOPLE',       'MANAGE',  'Create and edit staff records'),
    ('GUARDIAN_MANAGE',           'PEOPLE',       'MANAGE',  'Manage guardians and their students'),
    ('GEO_AREA_MANAGE',           'PEOPLE',       'MANAGE',  'Add and edit provinces, districts, communes and villages'),
    -- ACADEMIC
    ('COURSE_MANAGE',             'ACADEMIC',     'MANAGE',  'Manage courses and prerequisites'),
    ('CURRICULUM_MANAGE',         'ACADEMIC',     'MANAGE',  'Manage curriculums'),
    ('SEMESTER_MANAGE',           'ACADEMIC',     'MANAGE',  'Manage academic years and semesters'),
    ('CLASS_SECTION_MANAGE',      'ACADEMIC',     'MANAGE',  'Open class sections and assign lecturers'),
    ('SCHEDULE_MANAGE',           'ACADEMIC',     'MANAGE',  'Manage class schedules'),
    -- ADMISSION
    ('FORM_SALE_CREATE',          'ADMISSION',    'CREATE',  'Sell application forms'),
    ('FORM_SALE_VOID',            'ADMISSION',    'UPDATE',  'Void or refund an application form sale'),
    ('APPLICATION_VIEW',          'ADMISSION',    'VIEW',    'View applications'),
    ('APPLICATION_REVIEW',        'ADMISSION',    'APPROVE', 'Review applications and verify documents'),
    ('APPLICATION_DECIDE',        'ADMISSION',    'APPROVE', 'Accept, waitlist or reject applications'),
    ('APPLICANT_ENROLL',          'ADMISSION',    'CREATE',  'Convert an accepted applicant into a student'),
    -- ENROLLMENT
    ('ENROLLMENT_MANAGE',         'ENROLLMENT',   'MANAGE',  'Enroll and drop students in class sections'),
    ('ENROLLMENT_SELF',           'ENROLLMENT',   'CREATE',  'Register for own classes'),
    ('ATTENDANCE_TAKE',           'ENROLLMENT',   'UPDATE',  'Take attendance for own classes'),
    ('ATTENDANCE_VIEW',           'ENROLLMENT',   'VIEW',    'View attendance'),
    ('SCORE_ENTER',               'ENROLLMENT',   'UPDATE',  'Enter assessment scores'),
    ('GRADE_SUBMIT',              'ENROLLMENT',   'UPDATE',  'Submit final grades for approval'),
    ('GRADE_APPROVE',             'ENROLLMENT',   'APPROVE', 'Approve submitted grades'),
    ('GRADE_PUBLISH',             'ENROLLMENT',   'APPROVE', 'Publish approved grades to students'),
    ('GRADE_VIEW_OWN',            'ENROLLMENT',   'VIEW',    'View own grades and GPA'),
    -- EXAM
    ('EXAM_SCHEDULE_MANAGE',      'EXAM',         'MANAGE',  'Manage exam schedules and invigilators'),
    ('EXAM_SEAT_MANAGE',          'EXAM',         'MANAGE',  'Assign exam seats and eligibility'),
    ('GRADE_APPEAL_CREATE',       'EXAM',         'CREATE',  'Submit a grade appeal'),
    ('GRADE_APPEAL_REVIEW',       'EXAM',         'APPROVE', 'Review grade appeals'),
    -- REQUEST
    ('LEAVE_REQUEST_CREATE',      'REQUEST',      'CREATE',  'Request leave (permission) from classes'),
    ('LEAVE_REQUEST_APPROVE',     'REQUEST',      'APPROVE', 'Approve leave requests for own classes'),
    ('LEAVE_REQUEST_VIEW_ALL',    'REQUEST',      'VIEW',    'View all leave requests'),
    ('STUDENT_REQUEST_CREATE',    'REQUEST',      'CREATE',  'Submit student requests (add/drop, transfer, withdrawal)'),
    ('STUDENT_REQUEST_APPROVE',   'REQUEST',      'APPROVE', 'Approve student requests at own approval step'),
    -- GRADUATION
    ('GRADUATION_VERIFY',         'GRADUATION',   'APPROVE', 'Verify graduation requirements'),
    ('GRADUATION_APPROVE',        'GRADUATION',   'APPROVE', 'Approve graduation'),
    ('TRANSCRIPT_ISSUE',          'GRADUATION',   'CREATE',  'Issue transcripts'),
    ('CERTIFICATE_ISSUE',         'GRADUATION',   'CREATE',  'Issue certificates'),
    -- FINANCE
    ('FEE_MANAGE',                'FINANCE',      'MANAGE',  'Manage fee types and fee structures'),
    ('INVOICE_VIEW',              'FINANCE',      'VIEW',    'View invoices'),
    ('INVOICE_CREATE',            'FINANCE',      'CREATE',  'Create and cancel invoices'),
    ('PAYMENT_RECEIVE',           'FINANCE',      'CREATE',  'Receive payments'),
    ('PAYMENT_REVERSE',           'FINANCE',      'UPDATE',  'Reverse a payment'),
    ('SCHOLARSHIP_MANAGE',        'FINANCE',      'MANAGE',  'Manage scholarships and award them to students'),
    ('FINANCE_REPORT_VIEW',       'FINANCE',      'VIEW',    'View finance and collection reports'),
    -- CHAT
    ('CHAT_SEND',                 'CHAT',         'CREATE',  'Send chat messages'),
    ('CHAT_CREATE_GROUP',         'CHAT',         'CREATE',  'Create group chats'),
    -- SUPPORT
    ('ANNOUNCEMENT_PUBLISH',      'SUPPORT',      'APPROVE', 'Publish announcements'),
    ('SETTINGS_MANAGE',           'SUPPORT',      'MANAGE',  'Manage system settings'),
    ('REPORT_EXPORT',             'SUPPORT',      'EXPORT',  'Export reports'),
    -- HR
    ('CONTRACT_MANAGE',           'HR',           'MANAGE',  'Manage employment contracts'),
    ('SALARY_COMPONENT_MANAGE',   'HR',           'MANAGE',  'Manage allowances and deductions'),
    ('STAFF_LEAVE_CREATE',        'HR',           'CREATE',  'Request staff leave'),
    ('STAFF_LEAVE_APPROVE',       'HR',           'APPROVE', 'Approve staff leave'),
    ('TEACHING_LOAD_MANAGE',      'HR',           'MANAGE',  'Manage lecturer teaching loads'),
    ('PAYROLL_PROCESS',           'HR',           'MANAGE',  'Process payroll'),
    ('PAYROLL_APPROVE',           'HR',           'APPROVE', 'Approve payroll'),
    ('PAYSLIP_VIEW_OWN',          'HR',           'VIEW',    'View own payslips'),
    -- LIBRARY
    ('BOOK_MANAGE',               'LIBRARY',      'MANAGE',  'Manage books, copies and categories'),
    ('LOAN_ISSUE',                'LIBRARY',      'CREATE',  'Issue books on loan'),
    ('LOAN_RETURN',               'LIBRARY',      'UPDATE',  'Return books'),
    ('RESERVATION_CREATE',        'LIBRARY',      'CREATE',  'Reserve books'),
    ('FINE_WAIVE',                'LIBRARY',      'APPROVE', 'Waive library fines'),
    ('LIBRARY_POLICY_MANAGE',     'LIBRARY',      'MANAGE',  'Change borrowing rules: book limits, loan days, fines'),
    -- HOSTEL
    ('HOSTEL_MANAGE',             'HOSTEL',       'MANAGE',  'Manage hostels, rooms and beds'),
    ('HOSTEL_ALLOCATE',           'HOSTEL',       'MANAGE',  'Allocate beds to students'),
    ('HOSTEL_MAINTENANCE_MANAGE', 'HOSTEL',       'MANAGE',  'Manage hostel maintenance requests'),
    -- TRANSPORT
    ('TRANSPORT_MANAGE',          'TRANSPORT',    'MANAGE',  'Manage vehicles, routes and stops'),
    ('TRANSPORT_SUBSCRIBE',       'TRANSPORT',    'CREATE',  'Subscribe students to routes'),
    -- LMS
    ('MATERIAL_MANAGE',           'LMS',          'MANAGE',  'Manage course materials'),
    ('ASSIGNMENT_SUBMIT',         'LMS',          'CREATE',  'Submit assignments'),
    ('QUIZ_MANAGE',               'LMS',          'MANAGE',  'Create and manage quizzes'),
    ('QUIZ_TAKE',                 'LMS',          'CREATE',  'Take quizzes'),
    -- TIMETABLE
    ('AVAILABILITY_MANAGE',       'TIMETABLE',    'MANAGE',  'Manage lecturer availability'),
    ('TIMETABLE_GENERATE',        'TIMETABLE',    'MANAGE',  'Generate timetables'),
    ('TIMETABLE_APPROVE',         'TIMETABLE',    'APPROVE', 'Approve a generated timetable'),
    -- ENGAGEMENT
    ('ALUMNI_MANAGE',             'ENGAGEMENT',   'MANAGE',  'Manage alumni profiles'),
    ('EVENT_MANAGE',              'ENGAGEMENT',   'MANAGE',  'Manage events'),
    ('EVENT_REGISTER',            'ENGAGEMENT',   'CREATE',  'Register for events'),
    ('SURVEY_MANAGE',             'ENGAGEMENT',   'MANAGE',  'Create and manage surveys'),
    ('SURVEY_RESPOND',            'ENGAGEMENT',   'CREATE',  'Answer surveys'),
    ('SURVEY_RESULT_VIEW',        'ENGAGEMENT',   'VIEW',    'View survey results'),
    -- ASSET
    ('ASSET_MANAGE',              'ASSET',        'MANAGE',  'Manage assets and categories'),
    ('ASSET_ASSIGN',              'ASSET',        'MANAGE',  'Assign assets to staff'),
    ('ASSET_MAINTENANCE_MANAGE',  'ASSET',        'MANAGE',  'Manage asset maintenance')
ON CONFLICT (code) DO UPDATE
    SET module      = EXCLUDED.module,
        action      = EXCLUDED.action,
        description = EXCLUDED.description;
