package ru.mirea.project.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.mirea.project.service.ServiceRequestService;
import ru.mirea.project.service.UserService;

import java.io.IOException;

public class MainController {

    private final UserService userService = new UserService();
    private final ServiceRequestService requestService = new ServiceRequestService();

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
        selectButton(requestsButton);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/ru/mirea/project/view/requests-view.fxml"));
            Node view = loader.load();

            RequestsController controller = loader.getController();
            controller.setServices(requestService, userService);
            showContent(view);
        } catch (IOException e) {
            AlertHelper.showError("Ошибка", "Не удалось открыть экран заявок");
        }
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
        selectButton(selectedButton);

        Label title = new Label(sectionName);
        title.getStyleClass().add("section-title");

        Label message = new Label("Раздел находится в разработке");
        message.getStyleClass().add("body-text");

        VBox placeholder = new VBox(12, title, message);
        showContent(placeholder);
    }

    private void selectButton(Button selectedButton) {
        requestsButton.getStyleClass().remove("selected");
        usersButton.getStyleClass().remove("selected");
        statisticsButton.getStyleClass().remove("selected");
        selectedButton.getStyleClass().add("selected");
    }

    public void showContent(Node content) {
        contentArea.getChildren().setAll(content);
    }
}
