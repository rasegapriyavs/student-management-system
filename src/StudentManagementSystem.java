import model.GraduateStudent;
import model.Student;
import model.UndergraduateStudent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StudentManagementSystem {
    private static final String DATA_FILE = "students.txt";
    private final List<Student> students;
    private final Scanner scanner;

    public StudentManagementSystem() {
        this.students = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        loadData();
    }

    public static void main(String[] args) {
        StudentManagementSystem app = new StudentManagementSystem();
        app.run();
    }

    private void run() {
        while (true) {
            showMenu();
            int choice = readInteger("Enter your choice: ");
            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewAllStudents();
                case 3 -> searchStudent();
                case 4 -> updateStudent();
                case 5 -> deleteStudent();
                case 6 -> {
                    saveData();
                    System.out.println("Exiting the system. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
            System.out.println();
        }
    }

    private void showMenu() {
        System.out.println("=== Student Management System ===");
        System.out.println("1. Add new student");
        System.out.println("2. View all students");
        System.out.println("3. Search student by ID");
        System.out.println("4. Update student record");
        System.out.println("5. Delete student record");
        System.out.println("6. Exit");
    }

    private void addStudent() {
        int id = readInteger("Enter student ID: ");
        if (studentExists(id)) {
            System.out.println("Student ID already exists!");
            return;
        }
        String name = readString("Enter student name: ");
        double marks = readDouble("Enter student marks: ");

        System.out.println("Select student type:");
        System.out.println("1. Regular student");
        System.out.println("2. Undergraduate student");
        System.out.println("3. Graduate student");
        int type = readInteger("Type: ");

        Student student;
        switch (type) {
            case 2 -> {
                String major = readString("Enter major: ");
                student = new UndergraduateStudent(id, name, marks, major);
            }
            case 3 -> {
                String thesisTitle = readString("Enter thesis title: ");
                student = new GraduateStudent(id, name, marks, thesisTitle);
            }
            default -> student = new Student(id, name, marks);
        }

        students.add(student);
        saveData();
        System.out.println("Student added successfully.");
    }

    private void viewAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }
        System.out.println("All student records:");
        for (Student student : students) {
            System.out.println(student);
            System.out.println("-----------------------------");
        }
    }

    private void searchStudent() {
        int id = readInteger("Enter student ID to search: ");
        Student student = findStudentById(id);
        if (student == null) {
            System.out.println("Student not found.");
        } else {
            System.out.println("Found student:\n" + student);
        }
    }

    private void updateStudent() {
        int id = readInteger("Enter student ID to update: ");
        Student student = findStudentById(id);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        String newName = readString("Enter new name (leave blank to keep current): ");
        if (!newName.isBlank()) {
            student.setName(newName);
        }

        String marksInput = readString("Enter new marks (leave blank to keep current): ");
        if (!marksInput.isBlank()) {
            try {
                double newMarks = Double.parseDouble(marksInput);
                student.setMarks(newMarks);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid marks entry. Skipping marks update.");
            }
        }

        if (student instanceof UndergraduateStudent undergraduate) {
            String major = readString("Enter new major (leave blank to keep current): ");
            if (!major.isBlank()) {
                undergraduate.setMajor(major);
            }
        } else if (student instanceof GraduateStudent graduate) {
            String thesisTitle = readString("Enter new thesis title (leave blank to keep current): ");
            if (!thesisTitle.isBlank()) {
                graduate.setThesisTitle(thesisTitle);
            }
        }

        saveData();
        System.out.println("Student updated successfully.");
    }

    private void deleteStudent() {
        int id = readInteger("Enter student ID to delete: ");
        Student student = findStudentById(id);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }
        students.remove(student);
        saveData();
        System.out.println("Student deleted successfully.");
    }

    private boolean studentExists(int id) {
        return findStudentById(id) != null;
    }

    private Student findStudentById(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    Student student = Student.fromDataString(line);
                    students.add(student);
                } catch (Exception ex) {
                    System.out.println("Skipping invalid record: " + line);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to read student data file: " + ex.getMessage());
        }
    }

    private void saveData() {
        try (PrintWriter writer = new PrintWriter(DATA_FILE)) {
            for (Student student : students) {
                writer.println(student.toDataString());
            }
        } catch (IOException ex) {
            System.out.println("Error saving student data: " + ex.getMessage());
        }
    }

    private int readInteger(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid number for marks.");
            }
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
