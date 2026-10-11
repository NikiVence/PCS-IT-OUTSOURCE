package ru.mirea.project.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.DatabaseException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.User;
import ru.mirea.project.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Экран «Пользователи»: таблица и CRUD через диалоговую форму.
 * Контроллер не содержит SQL и бизнес-правил — всё делегируется UserService.
 */
public class UsersController {

    private static final String FORM_FXML = "/ru/mirea/project/view/user-form.fxml";
    private static final String STYLESHEET = "/ru/mirea/project/view/main.css";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final UserService userService = new UserService();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> createdAtColumn;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Label countLabel;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        usernameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        fullNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        emailColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        roleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().toString()));
        createdAtColumn.setCellValueFactory(
                c -> new SimpleStringProperty(formatDate(c.getValue().getCreatedAt())));

        usersTable.setItems(users);
        // Колонки растягиваются по ширине таблицы — без горизонтальной прокрутки.
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        usersTable.setPlaceholder(new Label("Пользователей пока нет"));

        // Кнопки доступны только когда в таблице выбрана строка.
        editButton.disableProperty().bind(usersTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(usersTable.getSelectionModel().selectedItemProperty().isNull());

        // Двойной щелчок по строке открывает редактирование.
        usersTable.setRowFactory(table -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    onEdit();
                }
            });
            return row;
        });

        loadUsers();
    }

    @FXML
    private void onRefresh() {
        loadUsers();
    }

    @FXML
    private void onAdd() {
        if (showForm(null)) {
            loadUsers();
            usersTable.scrollTo(users.size() - 1);
            usersTable.getSelectionModel().selectLast();
        }
    }

    @FXML
    private void onEdit() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected != null && showForm(selected)) {
            loadUsers();
            selectById(selected.getId());
        }
    }

    @FXML
    private void onDelete() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        boolean confirmed = AlertHelper.showConfirmation(
                "Удаление пользователя",
                "Удалить пользователя «" + selected.getUsername() + "» (" + selected.getFullName() + ")?\n"
                        + "Это действие нельзя отменить.");
        if (!confirmed) {
            return;
        }

        try {
            userService.deleteUser(selected.getId());
            loadUsers();
        } catch (EntityNotFoundException | DatabaseException e) {
            AlertHelper.showError("Не удалось удалить пользователя", e.getMessage());
            loadUsers();
        }
    }

    private void loadUsers() {
        try {
            users.setAll(userService.getAllUsers());
            countLabel.setText("Всего: " + users.size());
        } catch (DatabaseException e) {
            users.clear();
            countLabel.setText("");
            AlertHelper.showError("Ошибка загрузки",
                    "Не удалось загрузить пользователей. Проверьте подключение к базе данных.");
        }
    }

    private void selectById(Integer id) {
        users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .ifPresent(user -> usersTable.getSelectionModel().select(user));
    }

    /**
     * Открывает диалог добавления (existing == null) или редактирования.
     * Диалог не закрывается, пока данные не сохранены успешно.
     *
     * @return true, если пользователь сохранён
     */
    private boolean showForm(User existing) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FORM_FXML));
        DialogPane pane;
        try {
            pane = loader.load();
        } catch (IOException e) {
            AlertHelper.showError("Ошибка", "Не удалось открыть форму пользователя");
            return false;
        }
        pane.getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());

        UserFormController form = loader.getController();
        if (existing != null) {
            form.setUser(existing);
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Новый пользователь" : "Редактирование пользователя");
        dialog.setDialogPane(pane);
        dialog.initOwner(usersTable.getScene().getWindow());

        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        okButton.setText("Сохранить");
        ((Button) pane.lookupButton(ButtonType.CANCEL)).setText("Отмена");

        // Перехватываем нажатие «Сохранить»: при ошибке оставляем диалог открытым.
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                save(existing, form);
            } catch (BusinessException | EntityNotFoundException | DatabaseException e) {
                form.showError(e.getMessage());
                event.consume();
            }
        });

        return dialog.showAndWait().filter(button -> button == ButtonType.OK).isPresent();
    }

    private void save(User existing, UserFormController form)
            throws BusinessException, EntityNotFoundException {
        if (existing == null) {
            userService.createUser(form.getUsername(), form.getFullName(),
                    form.getEmail(), form.getRole());
        } else {
            userService.updateUser(existing.getId(), form.getUsername(), form.getFullName(),
                    form.getEmail(), form.getRole());
        }
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "—" : DATE_FORMAT.format(date);
    }
}
