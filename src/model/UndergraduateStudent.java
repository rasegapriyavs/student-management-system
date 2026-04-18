package model;

public class UndergraduateStudent extends Student {
    private String major;

    public UndergraduateStudent(int id, String name, double marks, String major) {
        super(id, name, marks);
        this.major = major;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    @Override
    public String getType() {
        return "Undergraduate";
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %d\nName: %s\nMarks: %.2f\nGrade: %s\nType: %s\nMajor: %s",
                getId(), getName(), getMarks(), getGrade(), getType(), major);
    }

    @Override
    public String toDataString() {
        return String.format("UNDERGRAD|%d|%s|%.2f|%s", getId(), getName(), getMarks(), major);
    }

    @Override
    public String toJson() {
        return String.format("{\"id\":%d,\"name\":\"%s\",\"marks\":%.2f,\"type\":\"%s\",\"major\":\"%s\"}",
                getId(),
                escapeJson(getName()),
                getMarks(),
                getType(),
                escapeJson(major));
    }
}
