package main.java.controllers;

import main.java.Main;

import java.io.IOException;

public class AfterLogin {
    public void manageMember() throws IOException {
        Main.changeScene("../resources/view/ManageMember.fxml");
    }

    public void manageBook() throws IOException {
        Main.changeScene("../resources/view/ManageBook.fxml");
    }

    public void borrowAndReturn() throws IOException {
        Main.changeScene("../resources/view/BorrowAndReturn.fxml");
    }

    public void userLogout() throws IOException {
        Main.changeScene("../resources/view/Login.fxml");
    }
}