-- ============================================================================
-- Student Management System - Database Schema
-- ============================================================================

PRAGMA foreign_keys = ON;

-- 1. Students Table
CREATE TABLE IF NOT EXISTS students (
    student_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    age INTEGER CHECK(age >= 18 AND age <= 100),
    grade REAL CHECK(grade >= 0.0 AND grade <= 100.0),
    enrollment_date TEXT NOT NULL
);

-- 2. Courses Table
CREATE TABLE IF NOT EXISTS courses (
    course_code TEXT PRIMARY KEY,
    course_name TEXT NOT NULL,
    credits INTEGER DEFAULT 3
);

-- 3. Enrollments Table (Many-to-Many)
CREATE TABLE IF NOT EXISTS enrollments (
    student_id TEXT,
    course_code TEXT,
    PRIMARY KEY (student_id, course_code),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_student_name ON students(name);
CREATE INDEX IF NOT EXISTS idx_course_code ON enrollments(course_code);
