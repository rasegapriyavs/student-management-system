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

    public String toJson() {
        return String.format("{\"id\":%d,\"name\":\"%s\",\"marks\":%.2f,\"type\":\"%s\"}",
                id,
                escapeJson(name),
                marks,
                getType());
    }

    public static Student fromJson(String json) {
        String normalized = json.replaceAll("[\\n\\r]", "");
        int id = parseIntValue(normalized, "id");
        String name = parseStringValue(normalized, "name");
        double marks = parseDoubleValue(normalized, "marks");
        String type = parseStringValue(normalized, "type");

        return switch (type) {
            case "Undergraduate" -> new UndergraduateStudent(id, name, marks, parseStringValue(normalized, "major"));
            case "Graduate" -> new GraduateStudent(id, name, marks, parseStringValue(normalized, "thesisTitle"));
            default -> new Student(id, name, marks);
        };
    }

    private static String parseStringValue(String json, String key) {
        String search = "\"" + key + "\"";
        int start = json.indexOf(search);
        if (start < 0) {
            return "";
        }
        int colon = json.indexOf(':', start);
        if (colon < 0) {
            return "";
        }
        int quoteStart = json.indexOf('"', colon);
        if (quoteStart < 0) {
            return "";
        }
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd < 0) {
            return "";
        }
        return json.substring(quoteStart + 1, quoteEnd).replace("\\\"", "\"");
    }

    private static int parseIntValue(String json, String key) {
        String raw = parseRawValue(json, key);
        return raw.isBlank() ? 0 : Integer.parseInt(raw);
    }

    private static double parseDoubleValue(String json, String key) {
        String raw = parseRawValue(json, key);
        return raw.isBlank() ? 0 : Double.parseDouble(raw);
    }

    private static String parseRawValue(String json, String key) {
        String search = "\"" + key + "\"";
        int start = json.indexOf(search);
        if (start < 0) {
            return "";
        }
        int colon = json.indexOf(':', start);
        if (colon < 0) {
            return "";
        }
        int valueStart = colon + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        int valueEnd = valueStart;
        while (valueEnd < json.length() && ",}]".indexOf(json.charAt(valueEnd)) < 0) {
            valueEnd++;
        }
        return json.substring(valueStart, valueEnd).trim();
    }

    protected static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
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
