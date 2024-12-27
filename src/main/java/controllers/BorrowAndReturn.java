package main.java.controllers;

import main.java.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;

public class BorrowAndReturn {
    @FXML
    private TextField bookName, authorName, bookGenre, contactNo;

    @FXML
    private Label error;

    final Connection conn = Main.conn;

    public void returnHome() throws IOException {
        Main.changeScene("../resources/view/AfterLogin.fxml");
    }

    private boolean bookIsFree(String book, String author, String genre) throws SQLException {
        final var query = """
                SELECT total_count, total_alloc
                  FROM books
                 WHERE name = ?
                   AND author = ?
                   AND genre = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, book);
            pstmt.setString(2, author);
            pstmt.setString(3, genre);

            try (var res = pstmt.executeQuery()) {
                return res.getInt("total_alloc") != res.getInt("total_count");
            }
        }
    }

    private boolean bookExists(String book, String author, String genre) throws SQLException {
        final var query = """
                SELECT *
                  FROM books
                 WHERE name = ?
                   AND author = ?
                   AND genre = ?;
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, book);
            pstmt.setString(2, author);
            pstmt.setString(3, genre);

            try (var rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean memberCanIssueBook(String phone_no) throws SQLException {
        final var query = """
                SELECT *
                  FROM members
                 WHERE phone_no = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phone_no);

            try (var res = pstmt.executeQuery()) {
                return res.next() && res.getObject("alloc_book_id") == null;
            }
        }
    }

    private void setMemberBookId(String phoneNo, int id) throws SQLException {
        final var query = """
                UPDATE members
                   SET alloc_book_id = ?
                 WHERE phone_no = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, phoneNo);
            pstmt.executeUpdate();
        }
    }

    private void incrementBookAllocCount(String name, String author, String genre) throws SQLException {
        final var query = """
                UPDATE books
                   SET total_alloc = ?
                 WHERE name = ?
                   AND author = ?
                   AND genre = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, getTotalAllocCount(name, author, genre) + 1);
            pstmt.setString(2, name);
            pstmt.setString(3, author);
            pstmt.setString(4, genre);
            pstmt.executeUpdate();
        }
    }

    public void issueBook() throws Exception {
        final var book = bookName.getText().toLowerCase();
        final var author = authorName.getText().toLowerCase();
        final var genre = bookGenre.getText().toLowerCase();
        final var phone_no = contactNo.getText();

        if (book.isEmpty() || author.isEmpty() || genre.isEmpty() || phone_no.isEmpty()) {
            error.setText("Missing details!");
            return;
        }

        try {
            if (!bookExists(book, author, genre)) {
                error.setText("The book doesn't exist in the database!");
                return;
            } else if (!bookIsFree(book, author, genre)) {
                error.setText("All the books have already been issued!");
                return;
            } else if (!memberCanIssueBook(phone_no)) {
                error.setText("The member does not exist or has already issued a book!");
                return;
            }

            setMemberBookId(phone_no, getBookId(book, author, genre));
            incrementBookAllocCount(book, author, genre);
            error.setText("");
            bookName.clear();
            authorName.clear();
            bookGenre.clear();
            contactNo.clear();
        } catch (SQLException e) {
            error.setText("Internal error: Failed to issue book!");
            throw new SQLException("Failed to issue book: " + e.getMessage());
        }
    }


    private int getBookId(String name, String author, String genre) throws SQLException {
        final var query = """
                SELECT id
                  FROM books
                 WHERE name = ?
                   AND author = ?
                   AND genre = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, author);
            pstmt.setString(3, genre);

            try (var res = pstmt.executeQuery()) {
                return res.getInt("id");
            }
        }
    }

    private boolean memberCanReturnBook(String phone_no, int bookId) throws SQLException {
        final var query = """
                SELECT *
                  FROM members
                 WHERE phone_no = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phone_no);

            try (var res = pstmt.executeQuery()) {
                return res.next() && res.getInt("alloc_book_id") == bookId;
            }
        }
    }

    private void resetMemberBookId(String phone_no) throws SQLException {
        final var query = """
                UPDATE members
                   SET alloc_book_id = NULL
                 WHERE phone_no = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phone_no);
            pstmt.executeUpdate();
        }
    }

    private int getTotalAllocCount(String name, String author, String genre) throws SQLException {
        final var query = """
                SELECT total_alloc
                  FROM books
                 WHERE name = ?
                   AND author = ?
                   AND genre = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, author);
            pstmt.setString(3, genre);

            try (var res = pstmt.executeQuery()) {
                return res.getInt("total_alloc");
            }
        }
    }

    private void decrementBookCount(String name, String author, String genre) throws SQLException {
        final var query = """
                UPDATE books
                   SET total_alloc = ?
                 WHERE name = ?
                   AND author = ?
                   AND genre = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, getTotalAllocCount(name, author, genre) - 1);
            pstmt.setString(2, name);
            pstmt.setString(3, author);
            pstmt.setString(4, genre);
            pstmt.executeUpdate();
        }
    }

    public void returnBook() throws Exception {
        final var book = bookName.getText().toLowerCase();
        final var author = authorName.getText().toLowerCase();
        final var genre = bookGenre.getText().toLowerCase();
        final var phone_no = contactNo.getText().toLowerCase();

        if (book.isEmpty() || author.isEmpty() || genre.isEmpty() || phone_no.isEmpty()) {
            error.setText("Missing details!");
            return;
        }

        try {
            if (!bookExists(book, author, genre)) {
                error.setText("The book doesn't exist in the database!");
                return;
            } else if (!memberCanReturnBook(phone_no, getBookId(book, author, genre))) {
                error.setText("The member does not exist or has not issued this book!");
                return;
            }

            resetMemberBookId(phone_no);
            decrementBookCount(book, author, genre);
            error.setText("");
            bookName.clear();
            authorName.clear();
            bookGenre.clear();
            contactNo.clear();
        } catch (SQLException e) {
            error.setText("Internal error: Failed to return book!");
            throw new SQLException("Failed to return book: " + e.getMessage());
        }
    }
}
