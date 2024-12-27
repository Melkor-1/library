package main.java.controllers;

import main.java.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;

public class ManageBook {
    @FXML
    private TextField bookName, authorName, bookGenre;

    @FXML
    private Label error;

    final Connection conn = Main.conn;

    public void returnHome() throws IOException {
        Main.changeScene("../resources/view/AfterLogin.fxml");
    }

    private void insertBook(String name, String author, String genre) throws Exception {
        final var query = """
                INSERT INTO books (name, author, genre, total_count)
                VALUES (?, ?, ?, ?)
                """;

        try (var pstmt = Main.conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, author);
            pstmt.setString(3, genre);
            pstmt.setInt(4, 1);
            pstmt.executeUpdate();
        }
    }

    private void updateBookCount(String name, String author, String genre, int count) throws Exception {
        final var query = """
                UPDATE books
                   SET total_count = ?
                 WHERE name = ?
                   AND author = ?
                   AND genre = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, count);
            pstmt.setString(2, name);
            pstmt.setString(3, author);
            pstmt.setString(4, genre);
            pstmt.executeUpdate();
        }
    }

    public void addBook() throws Exception {
        final var name = bookName.getText().toLowerCase();
        final var author = authorName.getText().toLowerCase();
        final var genre = bookGenre.getText().toLowerCase();

        if (name.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            error.setText("Missing details!");
            return;
        }

        final var query = """
                SELECT name, author, genre, total_count
                  FROM books
                 WHERE name = ?
                   AND author = ?
                   AND genre = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, author);
            pstmt.setString(3, genre);

            try (var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    updateBookCount(name, author, genre, rs.getInt("total_count") + 1);
                } else {
                    insertBook(name, author, genre);
                }

                error.setText("");
                bookName.clear();
                authorName.clear();
                bookGenre.clear();
            } catch (SQLException e) {
                error.setText("Internal error: Failed to add book!");
                throw new SQLException("Failed to add book: " + e.getMessage());
            }
        }
    }

    private void deleteBook(String name, String author, String genre) throws Exception {
        final var query = """
                DELETE FROM books
                 WHERE name = ?
                   AND author = ?
                   AND genre = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, author);
            pstmt.setString(3, genre);
            pstmt.executeUpdate();
        }
    }

    public void removeBook() throws Exception {
        final var name = bookName.getText().toLowerCase();
        final var author = authorName.getText().toLowerCase();
        final var genre = bookGenre.getText().toLowerCase();

        if (name.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            error.setText("Missing details!");
            return;
        }


        final var query = """
                SELECT name, author, genre, total_count, total_alloc
                  FROM books
                 WHERE name = ?
                   AND author = ?
                   AND genre = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, author);
            pstmt.setString(3, genre);

            try (var rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    error.setText("Book does not exist!");
                    return;
                } else if (!(rs.getInt("total_count") > rs.getInt("total_alloc"))) {
                    error.setText("The book is currently issued!");
                    return;
                }

                final var total_count = rs.getInt("total_count") - 1;

                if (total_count == 0) {
                    deleteBook(name, author, genre);
                } else {
                    updateBookCount(name, author, genre, total_count);
                }

                error.setText("");
                bookName.clear();
                authorName.clear();
                bookGenre.clear();
            }
        } catch (SQLException e) {
            error.setText("Internal error: Failed to remove book!");
            throw new SQLException("Failed to remove book: " + e.getMessage());
        }
    }
}