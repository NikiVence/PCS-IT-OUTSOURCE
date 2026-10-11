package ru.mirea.project.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.User;

import java.util.List;

public class RequestCreateDialogController {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<RequestCategory> categoryBox;

    @FXML
    private ComboBox<RequestPriority> priorityBox;

    @FXML
    private ComboBox<User> clientBox;

    @FXML
    private void initialize() {
        categoryBox.setItems(FXCollections.observableArrayList(RequestCategory.values()));
        priorityBox.setItems(FXCollections.observableArrayList(RequestPriority.values()));
        priorityBox.setValue(RequestPriority.MEDIUM);
        clientBox.setConverter(new UserStringConverter());
    }

    public void setClients(List<User> clients) {
        clientBox.setItems(FXCollections.observableArrayList(clients));
        if (!clients.isEmpty()) {
            clientBox.setValue(clients.getFirst());
        }
    }

    public String validateInput() {
        String error = RequestFormValidator.validate(
                titleField.getText(),
                descriptionArea.getText(),
                categoryBox.getValue(),
                priorityBox.getValue());
        if (error != null) {
            return error;
        }
        if (clientBox.getValue() == null) {
            return "Выберите клиента";
        }
        return null;
    }

    public String getTitle() {
        return titleField.getText();
    }

    public String getDescription() {
        return descriptionArea.getText();
    }

    public RequestCategory getCategory() {
        return categoryBox.getValue();
    }

    public RequestPriority getPriority() {
        return priorityBox.getValue();
    }

    public User getClient() {
        return clientBox.getValue();
    }
}
