-- ============================================================================
-- Student Management System - Backup and Restore Procedures
-- ============================================================================

/*
1. BACKUP DATABASE
Run this command in the terminal to create a backup of the database.
sqlite3 students.db ".backup 'students_backup.db'"

2. RESTORE DATABASE
To restore from a backup file:
cp students_backup.db students.db

3. EXPORT TO SQL DUMP
To export the entire database to an SQL script:
sqlite3 students.db .dump > database_dump.sql

4. RESTORE FROM SQL DUMP
To recreate the database from a dump:
sqlite3 students.db < database_dump.sql
*/
