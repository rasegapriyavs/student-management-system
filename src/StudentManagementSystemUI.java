import model.GraduateStudent;
import model.Student;
import model.UndergraduateStudent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentManagementSystemUI extends JFrame {
    private final StudentManagementSystem service;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;

    public StudentManagementSystemUI() {
        service = new StudentManagementSystem();

        setTitle("Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[] { "ID", "Name", "Marks", "Grade", "Type", "Details" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(createButton("Add Student", () -> showStudentDialog(null)));
        controlPanel.add(createButton("Update Student", this::updateSelectedStudent));
        controlPanel.add(createButton("Delete Student", this::deleteSelectedStudent));
        controlPanel.add(createButton("Refresh", this::refreshTable));
        controlPanel.add(new JLabel("Search ID:"));
        searchField = new JTextField(8);
        controlPanel.add(searchField);
        controlPanel.add(createButton("Search", this::searchStudentById));

        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTable();
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Student> students = service.getStudents();

        for (Student student : students) {
            tableModel.addRow(new Object[] {
                    student.getId(),
                    student.getName(),
                    String.format("%.2f", student.getMarks()),
                    student.getGrade(),
                    student.getType(),
                    detailsOf(student)
            });
        }
    }

    private String detailsOf(Student student) {
        if (student instanceof UndergraduateStudent undergraduate) {
            return "Major: " + undergraduate.getMajor();
        } else if (student instanceof GraduateStudent graduate) {
            return "Thesis: " + graduate.getThesisTitle();
        }
        return "";
    }

    private void showStudentDialog(Student existingStudent) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(10);
        JTextField nameField = new JTextField(20);
        JTextField marksField = new JTextField(10);
        JComboBox<String> typeCombo = new JComboBox<>(new String[] { "Regular", "Undergraduate", "Graduate" });
        JTextField extraField = new JTextField(20);
        JLabel extraLabel = new JLabel("Major / Thesis:");

        if (existingStudent != null) {
            idField.setText(String.valueOf(existingStudent.getId()));
            idField.setEnabled(false);
            nameField.setText(existingStudent.getName());
            marksField.setText(String.valueOf(existingStudent.getMarks()));

            if (existingStudent instanceof UndergraduateStudent undergraduate) {
                typeCombo.setSelectedItem("Undergraduate");
                extraField.setText(undergraduate.getMajor());
            } else if (existingStudent instanceof GraduateStudent graduate) {
                typeCombo.setSelectedItem("Graduate");
                extraField.setText(graduate.getThesisTitle());
            } else {
                typeCombo.setSelectedItem("Regular");
                extraField.setText("");
            }
        }

        typeCombo.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            if ("Undergraduate".equals(type)) {
                extraLabel.setText("Major:");
                extraField.setEnabled(true);
            } else if ("Graduate".equals(type)) {
                extraLabel.setText("Thesis Title:");
                extraField.setEnabled(true);
            } else {
                extraLabel.setText("Major / Thesis:");
                extraField.setEnabled(false);
                extraField.setText("");
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1;
        panel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Marks:"), gbc);
        gbc.gridx = 1;
        panel.add(marksField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Student Type:"), gbc);
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(extraLabel, gbc);
        gbc.gridx = 1;
        panel.add(extraField, gbc);

        if (existingStudent == null) {
            typeCombo.setSelectedItem("Regular");
            extraField.setEnabled(false);
        }

        int option = JOptionPane.showConfirmDialog(this, panel,
                existingStudent == null ? "Add Student" : "Update Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            double marks = Double.parseDouble(marksField.getText().trim());
            String type = (String) typeCombo.getSelectedItem();
            String extra = extraField.getText().trim();

            if (name.isBlank()) {
                throw new IllegalArgumentException("Name cannot be blank.");
            }

            Student student;
            if ("Undergraduate".equals(type)) {
                student = new UndergraduateStudent(id, name, marks, extra);
            } else if ("Graduate".equals(type)) {
                student = new GraduateStudent(id, name, marks, extra);
            } else {
                student = new Student(id, name, marks);
            }

            boolean success;
            if (existingStudent == null) {
                success = service.addStudent(student);
                if (!success) {
                    JOptionPane.showMessageDialog(this,
                            "A student with that ID already exists.",
                            "Duplicate ID",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this, "Student added successfully.");
            } else {
                success = service.replaceStudent(student);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Student updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Unable to update the student record.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numeric values for ID and marks.",
                    "Invalid input",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Invalid input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student from the table to update.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        Student student = service.findStudentById(id);
        if (student != null) {
            showStudentDialog(student);
        }
    }

    private void deleteSelectedStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student from the table to delete.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete student with ID " + id + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = service.deleteStudentById(id);
            if (success) {
                JOptionPane.showMessageDialog(this, "Student deleted successfully.");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Student not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchStudentById() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a student ID to search.",
                    "Missing ID",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(text);
            Student student = service.findStudentById(id);
            if (student == null) {
                JOptionPane.showMessageDialog(this,
                        "No student found with ID " + id + ".",
                        "Not found",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        student.toString(),
                        "Student found",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid integer ID.",
                    "Invalid ID",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentManagementSystemUI ui = new StudentManagementSystemUI();
            ui.setVisible(true);
        });
    }
}
