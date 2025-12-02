
package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;

public class StudentAttendanceApp extends Application {

    private Connection connection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        connectDatabase();

        primaryStage.setTitle("Student Attendance Management System");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        // Student input fields
        Label idLabel = new Label("Student ID:");
        TextField idInput = new TextField();
        GridPane.setConstraints(idLabel, 0, 0);
        GridPane.setConstraints(idInput, 1, 0);

        Label nameLabel = new Label("Student Name:");
        TextField nameInput = new TextField();
        GridPane.setConstraints(nameLabel, 0, 1);
        GridPane.setConstraints(nameInput, 1, 1);

        Label classLabel = new Label("Student Class:");
        TextField classInput = new TextField();
        GridPane.setConstraints(classLabel, 0, 2);
        GridPane.setConstraints(classInput, 1, 2);

        Button addButton = new Button("Add Student");
        GridPane.setConstraints(addButton, 1, 3);

        addButton.setOnAction(e -> {
            String id = idInput.getText();
            String name = nameInput.getText();
            String clas = classInput.getText();
            try {
                PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO students (id, name, class) VALUES (?, ?, ?)"
                );
                stmt.setString(1, id);
                stmt.setString(2, name);
                stmt.setString(3, clas);
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student added successfully!");
                idInput.clear();
                nameInput.clear();
                classInput.clear();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
            }
        });

        // Attendance section
        Label attIdLabel = new Label("Attendance Student ID:");
        TextField attIdInput = new TextField();
        GridPane.setConstraints(attIdLabel, 0, 4);
        GridPane.setConstraints(attIdInput, 1, 4);

        Button presentButton = new Button("Mark Present");
        GridPane.setConstraints(presentButton, 1, 5);

        Button absentButton = new Button("Mark Absent");
        GridPane.setConstraints(absentButton, 1, 6);

        presentButton.setOnAction(e -> markAttendance(attIdInput.getText(), true));
        absentButton.setOnAction(e -> markAttendance(attIdInput.getText(), false));

        // Display attendance
        Button displayButton = new Button("Display Attendance");
        GridPane.setConstraints(displayButton, 1, 7);

        displayButton.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            try {
                String query = "SELECT s.id, s.name, s.class, a.present, a.date FROM students s JOIN attendance a ON s.id = a.student_id ORDER BY s.id, a.date";
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                String lastId = "";
                int day = 1;

                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");

                    if (!id.equals(lastId)) {
                        sb.append("\nStudent ID: ").append(id)
                          .append(" | Name: ").append(name).append("\n");
                        lastId = id;
                        day = 1;
                    }

                    sb.append("Day ").append(day++).append(": ")
                      .append(rs.getBoolean("present") ? "Present" : "Absent")
                      .append(" (").append(rs.getTimestamp("date")).append(")").append("\n");
                }

                showAlert(Alert.AlertType.INFORMATION, "Attendance Report", sb.length() > 0 ? sb.toString() : "No records found.");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });

        // Add all UI components to the grid
        grid.getChildren().addAll(
                idLabel, idInput,
                nameLabel, nameInput,
                classLabel, classInput,
                addButton,
                attIdLabel, attIdInput,
                presentButton, absentButton,
                displayButton
        );

        Scene scene = new Scene(grid, 500, 450);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // MARK ATTENDANCE METHOD
    private void markAttendance(String studentId, boolean isPresent) {
        try {
            PreparedStatement check = connection.prepareStatement("SELECT * FROM students WHERE id = ?");
            check.setString(1, studentId);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO attendance (student_id, present) VALUES (?, ?)");
                stmt.setString(1, studentId);
                stmt.setBoolean(2, isPresent);
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Attendance marked as " + (isPresent ? "Present" : "Absent"));
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Student ID not found.");
            }
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    // CONNECT DATABASE
    private void connectDatabase() {
        String url = "jdbc:mysql://localhost:3306/attendance_db";
        String user = "root";
        String password = "kanishka";

        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Connection Failed", "Could not connect to database:\n" + e.getMessage());
        }
    }

    // ALERT UTILITY
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
