package ru.mirea.project.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

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
        selectButton(usersButton);
        loadView("/ru/mirea/project/view/users-view.fxml");
    }

    @FXML
    private void showStatistics() {
        showSection(statisticsButton, "Статистика");
    }

    private void selectButton(Button selectedButton) {
        requestsButton.getStyleClass().remove("selected");
        usersButton.getStyleClass().remove("selected");
        statisticsButton.getStyleClass().remove("selected");
        selectedButton.getStyleClass().add("selected");
    }

    /** Загружает экран из FXML и показывает его в области содержимого. */
    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            showContent(view);
        } catch (IOException e) {
            AlertHelper.showError("Ошибка", "Не удалось открыть экран: " + fxmlPath);
        }
    }

    private void showSection(Button selectedButton, String sectionName) {
        selectButton(selectedButton);

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
