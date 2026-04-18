package model;

public class GraduateStudent extends Student {
    private String thesisTitle;

    public GraduateStudent(int id, String name, double marks, String thesisTitle) {
        super(id, name, marks);
        this.thesisTitle = thesisTitle;
    }

    public String getThesisTitle() {
        return thesisTitle;
    }

    public void setThesisTitle(String thesisTitle) {
        this.thesisTitle = thesisTitle;
    }

    @Override
    public String getType() {
        return "Graduate";
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %d\nName: %s\nMarks: %.2f\nGrade: %s\nType: %s\nThesis Title: %s",
                getId(), getName(), getMarks(), getGrade(), getType(), thesisTitle);
    }

    @Override
    public String toDataString() {
        return String.format("GRAD|%d|%s|%.2f|%s", getId(), getName(), getMarks(), thesisTitle);
    }
}
