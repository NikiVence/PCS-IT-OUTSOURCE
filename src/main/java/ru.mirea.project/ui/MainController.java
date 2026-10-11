package ru.mirea.project.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button requestsButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button statisticsButton;

    @FXML
    public void initialize() {
        showRequests();
    }

    @FXML
    private void showRequests() {
        showSection(requestsButton, "Заявки");
    }

    @FXML
    private void showUsers() {
        showSection(usersButton, "Пользователи");
    }

    @FXML
    private void showStatistics() {
        showSection(statisticsButton, "Статистика");
    }

    private void showSection(Button selectedButton, String sectionName) {
        requestsButton.getStyleClass().remove("selected");
        usersButton.getStyleClass().remove("selected");
        statisticsButton.getStyleClass().remove("selected");
        selectedButton.getStyleClass().add("selected");

        Label title = new Label(sectionName);
        title.getStyleClass().add("section-title");

        Label message = new Label("Раздел находится в разработке");
        message.getStyleClass().add("body-text");

        VBox placeholder = new VBox(12, title, message);
        showContent(placeholder);
    }

    public void showContent(Node content) {
        contentArea.getChildren().setAll(content);
    }
}
