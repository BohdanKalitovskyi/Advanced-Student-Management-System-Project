-- ============================================================================
-- Student Management System - Sample Data
-- ============================================================================

-- Insert Courses
INSERT OR IGNORE INTO courses (course_code, course_name, credits) VALUES 
('CS101', 'Introduction to Programming', 4),
('MATH201', 'Advanced Calculus', 3),
('PHYS101', 'General Physics I', 4),
('ENG101', 'College Composition', 3),
('CS202', 'Data Structures and Algorithms', 4);

-- Insert Students
INSERT OR IGNORE INTO students (student_id, name, age, grade, enrollment_date) VALUES 
('S1001', 'Alice Johnson', 20, 92.5, '2023-09-01'),
('S1002', 'Bob Smith', 22, 85.0, '2023-09-01'),
('S1003', 'Charlie Davis', 19, 78.5, '2023-10-15'),
('S1004', 'Diana Prince', 21, 95.0, '2024-01-10');

-- Insert Enrollments
INSERT OR IGNORE INTO enrollments (student_id, course_code) VALUES 
('S1001', 'CS101'),
('S1001', 'MATH201'),
('S1002', 'CS101'),
('S1002', 'CS202'),
('S1003', 'ENG101'),
('S1004', 'PHYS101'),
('S1004', 'MATH201');
