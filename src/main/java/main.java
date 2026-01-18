import java.time.LocalDate;
import java.util.ArrayList;

class Main{
    public static void main(String[] args){
        Student student1 = new Student("John Doe", 20, 88.75);

        student1.addCourse("CS1");
        student1.addCourse("MATH2");
        student1.addCourse("ENG3");

        System.out.println("Student 1 Info");
        System.out.println(student1.displayInfo());
        System.out.println("GPA: " + student1.calculateGPA());
        System.out.println();

        //----
        ArrayList<String> courses = new ArrayList<>();
        courses.add("BIO101");
        courses.add("CHEM102");

        Student student2 = new Student(
                "Alice Smith",
                22,
                92.5,
                LocalDate.of(2024, 9, 1),
                courses
        );

        student2.removeCourse("BIO101");
        student2.addCourse("PHYS201");

        System.out.println("Student 2 Info");
        System.out.println(student2.displayInfo());
        System.out.println("GPA: " + student2.calculateGPA());
        System.out.println();

        System.out.println("Are student1 and student2 the same? " + student1.equals(student2));

        System.out.println("Student 1 ID: " + student1.getStudentID());
        System.out.println("Student 2 ID: " + student2.getStudentID());



        StudentManager manager = StudentManagerImpl.getInstance();

        Student s = new Student("John Doe", 20, 85.5);
        manager.addStudent(s);

        System.out.println("Student added successfully.");
        System.out.println("DB location: " + new java.io.File("students.db").getAbsolutePath());

    }
}