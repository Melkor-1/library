package main.java.controllers;

import main.java.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.SQLException;

import java.io.IOException;

import java.util.regex.Pattern;

public class ManageMember {
    @FXML
    private TextField memberName, houseAddress, contactAdd, contactRemove;

    @FXML
    private Label addLabel, removeLabel;

    final Connection conn = Main.conn;

    public void returnHome() throws IOException {
        Main.changeScene("../resources/view/AfterLogin.fxml");
    }

    private boolean checkMemberExists(String phoneNo) throws Exception {
        final var query = """
                SELECT name
                  FROM members
                 WHERE phone_no = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phoneNo);

            try (var rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean checkPhoneNo(String phoneNo) {
        /* Regular expression taken from: https://github.com/fWd82/Pakistan-Mobile-Number-Validator */
        final var mobNo = Pattern.compile("^((\\+92)?(0092)?(92)?(0)?)(3)([0-9]{9})$");
        final var matcher = mobNo.matcher(phoneNo);
        return matcher.find();
    }

    private void insertMember(String name, String address, String phoneNo) throws SQLException {
        final var query = """
                INSERT INTO members (name, address, phone_no) VALUES
                (?, ?, ?)
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setString(3, phoneNo);
            pstmt.executeUpdate();

        }
    }

    public void addMember() throws Exception {
        removeLabel.setText("");
        final var name = memberName.getText().toLowerCase();
        final var address = houseAddress.getText().toLowerCase();
        final var phoneNo = contactAdd.getText().toLowerCase();

        if (name.isEmpty() || address.isEmpty() || phoneNo.isEmpty()) {
            addLabel.setText("Missing details!");
            return;
        } else if (!checkPhoneNo(phoneNo)) {
            addLabel.setText("Invalid contact number format!");
            return;
        }

        try {
            if (checkMemberExists(phoneNo)) {
                addLabel.setText("Member already exists!");
                return;
            }

            insertMember(name, address, phoneNo);
            addLabel.setText("");
            memberName.clear();
            houseAddress.clear();
            contactAdd.clear();
        } catch (SQLException e) {
            addLabel.setText("Internal error: Failed to add member!");
            throw new SQLException("Failed to add member: " + e.getMessage());
        }
    }

    private void deleteMember(String phoneNo) throws SQLException {
        final var query = """
                DELETE FROM members
                 WHERE phone_no = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phoneNo);
            pstmt.executeUpdate();
        }
    }

    private boolean memberExists(String phoneNo) throws SQLException {
        final var query = """
                SELECT alloc_book_id
                  FROM members
                 WHERE phone_no = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phoneNo);

            try (var rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean memberHasIssuedBook(String phoneNo) throws SQLException {
        final var query = """
                SELECT alloc_book_id
                  FROM members
                 WHERE phone_no = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phoneNo);

            try (var res = pstmt.executeQuery()) {
                return res.getObject("alloc_book_id") != null;
            }
        }
    }

    private int getMemberBookId(String phoneNo) throws SQLException {
        final var query = """
                SELECT alloc_book_id
                  FROM members
                 WHERE phone_no = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phoneNo);

            try (var res = pstmt.executeQuery()) {
                return res.getInt("alloc_book_id");
            }
        }
    }

    private void decrementTotalAllocCount(int id) throws SQLException {
        var query = """
                SELECT total_alloc
                  FROM books
                 WHERE id = ?
                """;

        int totalAlloc;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);

            try (var res = pstmt.executeQuery()) {
                boolean exists = res.next();
                assert exists : "Member has issued a book that doesn't exist!";
                totalAlloc = res.getInt("total_alloc") - 1;
            }
        }

        query = """
                UPDATE books
                   SET total_alloc = ?
                 WHERE id = ?
                """;

        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, totalAlloc);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    public void removeMember() throws SQLException {
        addLabel.setText("");
        final var phoneNo = contactRemove.getText();

        if (phoneNo.isEmpty()) {
            removeLabel.setText("Missing field!");
            return;
        }

        try {
            if (!memberExists(phoneNo)) {
                removeLabel.setText("The member does not exist!");
                return;
            }

            if (memberHasIssuedBook(phoneNo)) {
                decrementTotalAllocCount(getMemberBookId(phoneNo));
            }

            deleteMember(phoneNo);
            removeLabel.setText("");
            contactRemove.clear();
        } catch (SQLException e) {
            removeLabel.setText("Internal error: Failed to remove book!");
            throw new SQLException("Failed to remove member: " + e.getMessage());
        }
    }
}