package core;

import java.time.LocalDate;
import java.util.ArrayList;
import core.exceptions.*;
import java.io.IOException;

class Main {
        public static void main(String[] args) {
                Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                        System.out.println("Unhandled exception: " + e.getMessage());
                        e.printStackTrace();
                });

                Student student1 = new Student("John Doe", 20, 88.75);

                student1.addCourse("CS1");
                student1.addCourse("MATH2");
                student1.addCourse("ENG3");

                System.out.println("Student 1 Info");
                System.out.println(student1.displayInfo());
                System.out.println("GPA: " + student1.calculateGPA());
                System.out.println();

                // ----
                ArrayList<String> courses = new ArrayList<>();
                courses.add("BIO101");
                courses.add("CHEM102");

                Student student2 = new Student(
                                "Alice Smith",
                                22,
                                92.5,
                                LocalDate.of(2024, 9, 1),
                                courses);

                student2.removeCourse("BIO101");
                student2.addCourse("PHYS201");

                System.out.println("Student 2 Info");
                System.out.println(student2.displayInfo());
                System.out.println("GPA: " + student2.calculateGPA());
                System.out.println();

                System.out.println("Are student1 and student2 the same? " + student1.equals(student2));

                System.out.println("Student 1 ID: " + student1.getStudentID());
                System.out.println("Student 2 ID: " + student2.getStudentID());

                StudentManagerImpl manager = StudentManagerImpl.getInstance();

                Student s1 = new Student("John Doe", 20, 85.5,
                                LocalDate.of(2024, 9, 1), new ArrayList<>());

                Student s2 = new Student("Alice Smith", 22, 92.0,
                                LocalDate.of(2023, 9, 1), new ArrayList<>());

                manager.addStudent(s1);
                manager.addStudent(s2);

                System.out.println("=== ALL STUDENTS ===");
                for (Student s : manager.displayAllStudents()) {
                        System.out.println(s.displayInfo());
                        System.out.println("-------------------");
                }

                double avg = manager.calculateAverageGrade();
                System.out.println("Average grade: " + avg);

                System.out.println("=== SEARCH 'Alice' ===");
                manager.searchStudents("Alice")
                                .forEach(st -> System.out.println(st.getName()));

                Student updated = new Student(
                                "Alice Johnson",
                                23,
                                95.0,
                                LocalDate.of(2023, 9, 1),
                                new ArrayList<>());

                manager.updateStudent(s2.getStudentID(), updated);

                System.out.println("=== AFTER UPDATE ===");
                manager.displayAllStudents()
                                .forEach(st -> System.out.println(st.getName() + " " + st.getGrade()));

                manager.removeStudent(s1.getStudentID());

                System.out.println("=== AFTER DELETE ===");
                manager.displayAllStudents()
                                .forEach(st -> System.out.println(st.getName()));

                manager.exportStudentsToCSV("students.csv");

                // manager.importStudentsFromCSV("students.csv");

                manager.addCourseToStudent(s2.getStudentID(), "CS101", "Computer Science 101", 4);

                System.out.println("=== ALL STUDENTS ===");
                for (Student s : manager.displayAllStudents()) {
                        System.out.println(s.displayInfo());
                        System.out.println("-------------------");
                }

                manager.removeCourseFromStudent(s2.getStudentID(), "CS101");
                System.out.println("=== ALL STUDENTS ===");
                for (Student s : manager.displayAllStudents()) {
                        System.out.println(s.displayInfo());
                        System.out.println("-------------------");
                }

        }

}