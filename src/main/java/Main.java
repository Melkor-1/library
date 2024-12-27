package main.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.io.IOException;

public class Main extends Application {
    private static Stage stg;
    private static final String URL = "jdbc:sqlite:src/main/java/library.db";
    public static Connection conn = null;

    private boolean checkDatabaseTables() throws SQLException {
        final var query = """
                SELECT name
                  FROM sqlite_master
                 WHERE type = 'table'
                   AND name NOT LIKE 'sqlite_%'
                 LIMIT 1
                """;

        try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(query)) {
            return rs.next();
        }
    }

    private void populateDatabase() throws SQLException {
        final var query1 = """
                    CREATE TABLE books (
                      id INTEGER PRIMARY KEY,
                      name TEXT NOT NULL,
                      author TEXT NOT NULL,
                      genre TEXT NOT NULL,
                      total_count INTEGER NOT NULL,
                      total_alloc INTEGER NOT NULL DEFAULT 0
                  )
                """;

        final var query2 = """
                CREATE TABLE members (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    address TEXT NOT NULL,
                    phone_no TEXT NOT NULL,
                    alloc_book_id INTEGER REFERENCES books(id)
                )
                """;

        final var query3 = """
                CREATE TABLE admin (
                    id INTEGER PRIMARY KEY CHECK (id = 1), -- Ensure only one entry
                    salt BLOB NOT NULL,
                    hash BLOB NOT NULL
                );
                """;

        try (var stmt = conn.createStatement()) {
            stmt.execute(query1);
            stmt.execute(query2);
            stmt.execute(query3);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new SQLException("Failed to establish connection with database: " + e.getMessage());
        }

        var startingStage = "../resources/view/Login.fxml";

        if (!checkDatabaseTables()) {
            populateDatabase();
            startingStage = "../resources/view/Signup.fxml";
        }

        stg = primaryStage;
        primaryStage.setResizable(false);
        stg.initStyle(StageStyle.UTILITY);
        Parent root = FXMLLoader.load(Main.class.getResource(startingStage));
        primaryStage.setTitle("Library Management System");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop(); // Call the superclass method to handle any additional cleanup

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Failed to close the database connection: " + e.getMessage());
            }
        }
    }

    public static void changeScene(String fxml) throws IOException {
        final Parent pane = FXMLLoader.load(Main.class.getResource(fxml));
        stg.getScene().setRoot(pane);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
