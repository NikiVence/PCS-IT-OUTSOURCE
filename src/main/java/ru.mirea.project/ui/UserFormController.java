package ru.mirea.project.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;

/**
 * Контроллер формы пользователя (диалог добавления/редактирования).
 * Только собирает введённые значения; проверку правил выполняет UserService.
 */
public class UserFormController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<UserRole> roleComboBox;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll(UserRole.values());
        roleComboBox.setConverter(UiFormat.roleConverter());
    }

    /** Предзаполняет форму данными выбранного пользователя (режим редактирования). */
    public void setUser(User user) {
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        roleComboBox.setValue(user.getRole());
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getFullName() {
        return fullNameField.getText();
    }

    public String getEmail() {
        return emailField.getText();
    }

    public UserRole getRole() {
        return roleComboBox.getValue();
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        errorLabel.getScene().getWindow().sizeToScene();
    }
}
