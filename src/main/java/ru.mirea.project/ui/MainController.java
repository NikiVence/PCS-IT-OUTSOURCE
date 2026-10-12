package ru.mirea.project.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import ru.mirea.project.service.ExcelExportService;
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
        selectButton(usersButton);
        loadView("/ru/mirea/project/view/users-view.fxml");
    }

    @FXML
    private void showStatistics() {
        selectButton(statisticsButton);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/ru/mirea/project/view/statistics-view.fxml"));
            Node view = loader.load();
            StatisticsController controller = loader.getController();
            controller.setServices(requestService, userService, new ExcelExportService());
            showContent(view);
        } catch (IOException e) {
            AlertHelper.showError("Ошибка", "Не удалось открыть экран статистики");
        }
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
