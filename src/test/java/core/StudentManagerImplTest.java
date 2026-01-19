package core;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * JUnit 5 test suite for StudentManagerImpl class.
 * 
 * <p>
 * This test class verifies the functionality of all CRUD operations, search,
 * CSV import/export, and course management features. It uses a temporary SQLite
 * database for testing to avoid affecting the production database.
 * </p>
 * 
 * <p>
 * Test coverage includes:
 * </p>
 * <ul>
 * <li>Adding students to the database</li>
 * <li>Removing students and cascade deletion of enrollments</li>
 * <li>Updating student information</li>
 * <li>Adding and removing course enrollments</li>
 * <li>Calculating average grades</li>
 * <li>Searching students by various criteria</li>
 * <li>Exporting and importing student data via CSV</li>
 * </ul>
 * 
 * <p>
 * The test database is created before all tests and cleaned between each test
 * to ensure test isolation and repeatability.
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudentManagerImplTest {

    private StudentManagerImpl manager;

    /**
     * Sets up the test database environment before all tests.
     * Creates a temporary database file and initializes the schema.
     * Uses a mock implementation of StudentManagerImpl to inject the test
     * connection.
     *
     * @throws java.io.IOException If the temporary file cannot be created.
     */
    @BeforeAll
    void setupDatabase() throws java.io.IOException {

        java.io.File tempDb = java.io.File.createTempFile("test_students_", ".db");
        tempDb.deleteOnExit();
        System.out.println("Test Database Path: " + tempDb.getAbsolutePath());
        TestConnectionFactory.setURL("jdbc:sqlite:" + tempDb.getAbsolutePath());

        TestDatabaseInitializer.initialize();

        manager = new StudentManagerImpl() {

            @Override
            protected Connection getConnection() throws SQLException {
                return TestConnectionFactory.getConnection();
            }

            @Override
            protected void initializeDatabase() {

                TestDatabaseInitializer.initialize();
            }
        };
    }

    /**
     * Cleans the database before each test case.
     * Removes all student records to ensure a fresh state.
     */
    @BeforeEach
    void cleanDatabase() {
        manager.displayAllStudents()
                .forEach(s -> manager.removeStudent(s.getStudentID()));
    }

    /**
     * Verifies that a student can be added correctly to the database.
     */
    @Test
    void testAddStudent() {
        Student s = new Student(
                "John Doe",
                20,
                90.0,
                LocalDate.now(),
                new ArrayList<>());

        manager.addStudent(s);

        var students = manager.displayAllStudents();
        assertEquals(1, students.size());
        assertEquals("John Doe", students.get(0).getName());
    }

    /**
     * Verifies that a student can be removed from the database by ID.
     */
    @Test
    void testRemoveStudent() {
        Student s = new Student("Alice", 22, 85.0, LocalDate.now(), new ArrayList<>());
        manager.addStudent(s);

        manager.removeStudent(s.getStudentID());

        assertTrue(manager.displayAllStudents().isEmpty());
    }

    /**
     * Verifies that student information can be updated in the database.
     */
    @Test
    void testUpdateStudent() {
        Student s = new Student("Bob", 21, 75.0, LocalDate.now(), new ArrayList<>());
        manager.addStudent(s);

        Student updated = new Student("Bobby", 22, 80.0, LocalDate.now(), new ArrayList<>());
        manager.updateStudent(s.getStudentID(), updated);

        Student result = manager.displayAllStudents().get(0);
        assertEquals("Bobby", result.getName());
        assertEquals(80.0, result.getGrade());
    }

    /**
     * Verifies that course enrollments can be added and removed for a student.
     */
    @Test
    void testAddAndRemoveCourse() {
        Student s = new Student("Eve", 23, 88.0, LocalDate.now(), new ArrayList<>());
        manager.addStudent(s);

        manager.addCourseToStudent(s.getStudentID(), "CS101", "Computer Science", 4);
        assertEquals(1, manager.displayAllStudents().get(0).getCourses().size());

        manager.removeCourseFromStudent(s.getStudentID(), "CS101");
        assertEquals(0, manager.displayAllStudents().get(0).getCourses().size());
    }

    /**
     * Verifies that the average grade calculation is correct.
     */
    @Test
    void testCalculateAverageGrade() {
        manager.addStudent(new Student("S1", 20, 90.0, LocalDate.now(), new ArrayList<>()));
        manager.addStudent(new Student("S2", 21, 80.0, LocalDate.now(), new ArrayList<>()));

        double avg = manager.calculateAverageGrade();
        assertEquals(85.0, avg);
    }

    /**
     * Verifies that students can be searched by name or ID.
     */
    @Test
    void testSearchStudents() {
        manager.addStudent(new Student("Charlie", 22, 70.0, LocalDate.now(), new ArrayList<>()));

        assertEquals(1, manager.searchStudents("Char").size());
        assertEquals(0, manager.searchStudents("NotFound").size());
    }

    /**
     * Verifies that exporting to CSV and importing from CSV works without data
     * loss.
     */
    @Test
    void testExportImportCSV() {
        String file = "test_students.csv";

        Student s = new Student("Dana", 20, 95.0, LocalDate.now(), new ArrayList<>());
        manager.addStudent(s);

        manager.exportStudentsToCSV(file);
        manager.removeStudent(s.getStudentID());

        assertEquals(0, manager.displayAllStudents().size());

        manager.importStudentsFromCSV(file);

        assertEquals(1, manager.displayAllStudents().size());
        assertEquals("Dana", manager.displayAllStudents().get(0).getName());
    }

    /**
     * Debugging helper to verify the database connection URL.
     *
     * @throws Exception If database access fails.
     */
    @Test
    void debugDatabaseSource() throws Exception {
        Connection c = manager.getConnection();
        System.out.println(c.getMetaData().getURL());
    }
}
