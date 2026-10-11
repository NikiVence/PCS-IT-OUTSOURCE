package ru.mirea.project.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.ServiceRequest;
import ru.mirea.project.model.User;

import java.util.List;

public class RequestEditDialogController {

    @FXML
    private ComboBox<User> actingUserBox;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<RequestCategory> categoryBox;

    @FXML
    private ComboBox<RequestPriority> priorityBox;

    @FXML
    private void initialize() {
        categoryBox.setItems(FXCollections.observableArrayList(RequestCategory.values()));
        priorityBox.setItems(FXCollections.observableArrayList(RequestPriority.values()));
        actingUserBox.setConverter(new UserStringConverter());
    }

    public void setData(ServiceRequest request, List<User> users) {
        actingUserBox.setItems(FXCollections.observableArrayList(users));
        actingUserBox.setValue(findUser(users, request.getClientId()));
        titleField.setText(request.getTitle());
        descriptionArea.setText(request.getDescription());
        categoryBox.setValue(request.getCategory());
        priorityBox.setValue(request.getPriority());
    }

    public String validateInput() {
        if (actingUserBox.getValue() == null) {
            return "Выберите пользователя, от имени которого выполняется изменение";
        }
        return RequestFormValidator.validate(
                titleField.getText(),
                descriptionArea.getText(),
                categoryBox.getValue(),
                priorityBox.getValue());
    }

    private User findUser(List<User> users, Integer id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    public User getActingUser() {
        return actingUserBox.getValue();
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
}
