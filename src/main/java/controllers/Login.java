package main.java.controllers;

import main.java.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.sql.SQLException;

public class Login {
    public Login() {
    }

    @FXML
    private Label error;

    @FXML
    private PasswordField password;

    public void userLogin() throws Exception {
        checkLogin();
    }

    private byte[] getSalt() throws Exception {
        final var query = """
                SELECT salt
                  FROM admin
                """;

        try (var rs = Main.conn.createStatement().executeQuery(query)) {
            return rs.getBytes("salt");
        } catch (SQLException e) {
            error.setText("Internal error: Failed to login!");
            throw new SQLException("Failed to fetch salt: " + e.getMessage());
        }
    }

    private byte[] getHash() throws Exception {
        final var query = """
                SELECT hash
                  FROM admin
                """;

        try (var rs = Main.conn.createStatement().executeQuery(query)) {
            return rs.getBytes("hash");
        } catch (SQLException e) {
            error.setText("Internal error: Failed to login!");
            throw new SQLException("Failed to fetch hash: " + e.getMessage());
        }
    }

    private void checkLogin() throws Exception {
        final var inputPwd = password.getText();

        if (inputPwd.isEmpty()) {
            error.setText("Missing password!");
            return;
        }

        final var dbSalt = getSalt();
        final var dbHash = getHash();
        final var engine = new HashClass(dbSalt, inputPwd);
        final var hash = engine.getHash();

        if (engine.compareHashes(hash, dbHash)) {
            Main.changeScene("../resources/view/AfterLogin.fxml");
        }

        error.setText("Incorrect password!");
    }
}