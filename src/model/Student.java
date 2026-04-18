package model;

public class Student {
    private int id;
    private String name;
    private double marks;

    public Student(int id, String name, double marks) {
        this.id = id;
        this.name = name;
        this.marks = marks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    public String getType() {
        return "Regular";
    }

    public String getGrade() {
        if (marks >= 90) {
            return "A";
        } else if (marks >= 75) {
            return "B";
        } else if (marks >= 60) {
            return "C";
        } else if (marks >= 50) {
            return "D";
        } else {
            return "F";
        }
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %d\nName: %s\nMarks: %.2f\nGrade: %s\nType: %s",
                id, name, marks, getGrade(), getType());
    }

    public String toDataString() {
        return String.format("REGULAR|%d|%s|%.2f", id, name, marks);
    }

    public static Student fromDataString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid student data: " + line);
        }

        String recordType = parts[0];
        int id = Integer.parseInt(parts[1]);
        String name = parts[2];
        double marks = Double.parseDouble(parts[3]);

        return switch (recordType) {
            case "UNDERGRAD" -> {
                String major = parts.length >= 5 ? parts[4] : "";
                yield new UndergraduateStudent(id, name, marks, major);
            }
            case "GRAD" -> {
                String thesisTitle = parts.length >= 5 ? parts[4] : "";
                yield new GraduateStudent(id, name, marks, thesisTitle);
            }
            default -> new Student(id, name, marks);
        };
    }
}
