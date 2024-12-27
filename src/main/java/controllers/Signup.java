package main.java.controllers;

import main.java.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.sql.SQLException;

public class Signup {
    public Signup() {
    }

    @FXML
    private PasswordField password;

    @FXML
    private Label error;

    private void insertPassword(String pwd) throws SQLException {
        final var query = """
                INSERT INTO admin (salt, hash) VALUES
                (?, ?)
                """;

        try (var pstmt = Main.conn.prepareStatement(query)) {
            final var hashEngine = new HashClass(pwd);
            pstmt.setBytes(1, hashEngine.getSalt());
            pstmt.setBytes(2, hashEngine.getHash());
            pstmt.executeUpdate();
        }
    }

    public void userSignup() throws Exception {
        final var pwd = password.getText();

        if (pwd.isEmpty()) {
            error.setText("Missing password!");
            return;
        }

        try {
            insertPassword(pwd);
        } catch (SQLException e) {
            error.setText("Failed to sign up!");
            throw new SQLException("Failed to sign up: " + e.getMessage());
        }

        Main.changeScene("../resources/view/Login.fxml");
    }
}