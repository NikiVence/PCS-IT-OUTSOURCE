package ru.mirea.project.ui;

import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.DatabaseException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.ServiceRequest;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.service.ServiceRequestService;
import ru.mirea.project.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RequestsController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    private TableView<ServiceRequest> requestsTable;

    @FXML
    private TableColumn<ServiceRequest, Integer> idColumn;

    @FXML
    private TableColumn<ServiceRequest, String> titleColumn;

    @FXML
    private TableColumn<ServiceRequest, String> descriptionColumn;

    @FXML
    private TableColumn<ServiceRequest, String> categoryColumn;

    @FXML
    private TableColumn<ServiceRequest, String> statusColumn;

    @FXML
    private TableColumn<ServiceRequest, String> priorityColumn;

    @FXML
    private TableColumn<ServiceRequest, String> clientColumn;

    @FXML
    private TableColumn<ServiceRequest, String> executorColumn;

    @FXML
    private TableColumn<ServiceRequest, String> createdAtColumn;

    @FXML
    private TableColumn<ServiceRequest, String> takenAtColumn;

    @FXML
    private TableColumn<ServiceRequest, String> closedAtColumn;

    @FXML
    private TableColumn<ServiceRequest, String> ratingColumn;

    @FXML
    private Label selectionLabel;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button assignButton;

    @FXML
    private Button statusButton;

    @FXML
    private Button rateButton;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<RequestStatus> statusFilter;

    @FXML
    private ComboBox<RequestPriority> priorityFilter;

    @FXML
    private ComboBox<RequestCategory> categoryFilter;

    @FXML
    private ComboBox<RequestSort> sortComboBox;

    @FXML
    private Label resultLabel;

    private final ObservableList<ServiceRequest> requests = FXCollections.observableArrayList();
    // Цепочка: все заявки -> отфильтрованные -> отсортированные -> таблица.
    private final FilteredList<ServiceRequest> filteredRequests = new FilteredList<>(requests);
    private final SortedList<ServiceRequest> sortedRequests = new SortedList<>(filteredRequests);
    private final List<User> users = new ArrayList<>();
    private final Map<Integer, User> usersById = new HashMap<>();

    private ServiceRequestService requestService;
    private UserService userService;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        titleColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getTitle()));
        descriptionColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getDescription()));
        categoryColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getCategory().toString()));
        statusColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getStatus().toString()));
        priorityColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getPriority().toString()));
        clientColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(userName(cell.getValue().getClientId())));
        executorColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(userName(cell.getValue().getExecutorId())));
        createdAtColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(formatDate(cell.getValue().getCreatedAt())));
        takenAtColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(formatDate(cell.getValue().getTakenAt())));
        closedAtColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(formatDate(cell.getValue().getClosedAt())));
        ratingColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(formatNumber(cell.getValue().getRating())));

        requestsTable.setItems(sortedRequests);
        requestsTable.setPlaceholder(new Label("Заявки не найдены"));
        setupSearchAndFilters();

        for (Button button : List.of(
                editButton, deleteButton, assignButton, statusButton, rateButton)) {
            button.disableProperty().bind(
                    requestsTable.getSelectionModel().selectedItemProperty().isNull());
        }
        requestsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldRequest, selectedRequest) -> updateSelectionLabel(selectedRequest));
    }

    private void setupSearchAndFilters() {
        setupFilter(statusFilter, RequestStatus.values(), "Все статусы");
        setupFilter(priorityFilter, RequestPriority.values(), "Все приоритеты");
        setupFilter(categoryFilter, RequestCategory.values(), "Все категории");

        sortComboBox.getItems().setAll(RequestSort.values());
        sortComboBox.setValue(RequestSort.BY_ID);

        // Любое изменение поиска или фильтра пересчитывает условие отбора.
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        priorityFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Сортировка: щелчок по заголовку колонки важнее выбора в списке.
        // При выборе в списке сортировка по колонкам сбрасывается.
        requestsTable.setSortPolicy(table -> true);
        sortedRequests.comparatorProperty().bind(Bindings.createObjectBinding(
                () -> requestsTable.getComparator() != null
                        ? requestsTable.getComparator()
                        : sortComboBox.getValue().comparator(),
                requestsTable.comparatorProperty(), sortComboBox.valueProperty()));
        sortComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> requestsTable.getSortOrder().clear());

        // Даты и приоритет показаны строками, поэтому сортируются только через список «Сортировка».
        for (TableColumn<ServiceRequest, ?> column : List.of(
                priorityColumn, createdAtColumn, takenAtColumn, closedAtColumn, ratingColumn)) {
            column.setSortable(false);
        }

        resultLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "Показано: " + filteredRequests.size() + " из " + requests.size(),
                filteredRequests, requests));
    }

    private void applyFilters() {
        filteredRequests.setPredicate(RequestFilter.of(
                searchField.getText(),
                statusFilter.getValue(),
                priorityFilter.getValue(),
                categoryFilter.getValue()));
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        statusFilter.setValue(null);
        priorityFilter.setValue(null);
        categoryFilter.setValue(null);
        sortComboBox.setValue(RequestSort.BY_ID);
        requestsTable.getSortOrder().clear();
    }

    /** Список значений перечисления с пунктом «все» (null) в начале. */
    private <T> void setupFilter(ComboBox<T> comboBox, T[] values, String allText) {
        List<T> items = new ArrayList<>();
        items.add(null);
        items.addAll(Arrays.asList(values));
        comboBox.getItems().setAll(items);
        comboBox.setCellFactory(listView -> new FilterCell<>(allText));
        comboBox.setButtonCell(new FilterCell<>(allText));
        comboBox.setValue(null);
    }

    private static class FilterCell<T> extends ListCell<T> {
        private final String allText;

        FilterCell(String allText) {
            this.allText = allText;
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? allText : item.toString());
        }
    }

    public void setServices(ServiceRequestService requestService, UserService userService) {
        this.requestService = requestService;
        this.userService = userService;
        refreshData();
    }

    @FXML
    private void refreshData() {
        try {
            List<User> loadedUsers = userService.getAllUsers();
            List<ServiceRequest> loadedRequests = requestService.getAllRequests();

            users.clear();
            users.addAll(loadedUsers);

            usersById.clear();
            for (User user : loadedUsers) {
                usersById.put(user.getId(), user);
            }

            requests.setAll(loadedRequests);
        } catch (DatabaseException e) {
            AlertHelper.showError("Ошибка базы данных", e.getMessage());
        }
    }

    @FXML
    private void createRequest() {
        List<User> clients = usersWithRole(UserRole.CLIENT);
        if (clients.isEmpty()) {
            AlertHelper.showWarning("Создание заявки", "В базе нет клиентов");
            return;
        }

        try {
            FXMLLoader loader = loadForm("request-create-dialog.fxml");
            Parent content = loader.load();
            RequestCreateDialogController controller = loader.getController();
            controller.setClients(clients);

            ButtonType createButton = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
            Dialog<ButtonType> dialog = createDialog("Новая заявка", content, createButton);
            keepDialogOpenOnInvalidInput(dialog, createButton, controller::validateInput);

            if (dialog.showAndWait().orElse(ButtonType.CANCEL) != createButton) {
                return;
            }

            requestService.createRequest(
                    controller.getTitle(),
                    controller.getDescription(),
                    controller.getCategory(),
                    controller.getPriority(),
                    controller.getClient().getId());

            refreshData();
            AlertHelper.showSuccess("Создание заявки", "Заявка создана");
        } catch (IOException e) {
            AlertHelper.showError("Ошибка", "Не удалось открыть форму создания");
        } catch (BusinessException | EntityNotFoundException | DatabaseException e) {
            AlertHelper.showError("Не удалось создать заявку", e.getMessage());
        }
    }

    @FXML
    private void editRequest() {
        ServiceRequest selected = selectedRequest();
        if (selected == null) {
            return;
        }

        try {
            FXMLLoader loader = loadForm("request-edit-dialog.fxml");
            Parent content = loader.load();
            RequestEditDialogController controller = loader.getController();
            controller.setData(selected, users);

            ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
            Dialog<ButtonType> dialog = createDialog("Редактирование заявки", content, saveButton);
            keepDialogOpenOnInvalidInput(dialog, saveButton, controller::validateInput);

            if (dialog.showAndWait().orElse(ButtonType.CANCEL) != saveButton) {
                return;
            }

            requestService.updateRequest(
                    selected.getId(),
                    controller.getActingUser().getId(),
                    controller.getTitle(),
                    controller.getDescription(),
                    controller.getCategory(),
                    controller.getPriority());

            refreshData();
            AlertHelper.showSuccess("Редактирование заявки", "Заявка обновлена");
        } catch (IOException e) {
            AlertHelper.showError("Ошибка", "Не удалось открыть форму редактирования");
        } catch (BusinessException | EntityNotFoundException | DatabaseException e) {
            AlertHelper.showError("Не удалось изменить заявку", e.getMessage());
        }
    }

    @FXML
    private void deleteRequest() {
        ServiceRequest selected = selectedRequest();
        if (selected == null) {
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Удалить заявку №" + selected.getId() + "?",
                ButtonType.YES,
                ButtonType.NO);
        confirmation.setTitle("Удаление заявки");
        confirmation.setHeaderText(null);
        confirmation.initOwner(requestsTable.getScene().getWindow());

        if (confirmation.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {
            requestService.deleteRequest(selected.getId());
            refreshData();
            AlertHelper.showSuccess("Удаление заявки", "Заявка удалена");
        } catch (EntityNotFoundException | DatabaseException e) {
            AlertHelper.showError("Не удалось удалить заявку", e.getMessage());
        }
    }

    @FXML
    private void assignExecutor() {
        ServiceRequest selected = selectedRequest();
        if (selected == null) {
            return;
        }

        List<User> executors = usersWithRole(UserRole.EXECUTOR);
        if (executors.isEmpty()) {
            AlertHelper.showWarning("Назначение исполнителя", "В базе нет исполнителей");
            return;
        }

        ComboBox<User> executorBox = new ComboBox<>(FXCollections.observableArrayList(executors));
        executorBox.setConverter(new UserStringConverter());
        executorBox.setMaxWidth(Double.MAX_VALUE);
        executorBox.setValue(findUser(selected.getExecutorId()));
        if (executorBox.getValue() == null) {
            executorBox.setValue(executors.getFirst());
        }

        VBox content = new VBox(8, new Label("Исполнитель"), executorBox);
        ButtonType assignButton = new ButtonType("Назначить", ButtonBar.ButtonData.OK_DONE);
        Dialog<ButtonType> dialog = createDialog("Назначение исполнителя", content, assignButton);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != assignButton) {
            return;
        }

        try {
            requestService.assignExecutor(selected.getId(), executorBox.getValue().getId());
            refreshData();
            AlertHelper.showSuccess("Назначение исполнителя", "Исполнитель назначен");
        } catch (BusinessException | EntityNotFoundException | DatabaseException e) {
            AlertHelper.showError("Не удалось назначить исполнителя", e.getMessage());
        }
    }

    @FXML
    private void changeStatus() {
        ServiceRequest selected = selectedRequest();
        if (selected == null) {
            return;
        }

        ChoiceDialog<RequestStatus> dialog = new ChoiceDialog<>(
                selected.getStatus(), RequestStatus.values());
        dialog.setTitle("Изменение статуса");
        dialog.setHeaderText("Заявка №" + selected.getId());
        dialog.setContentText("Новый статус:");
        dialog.initOwner(requestsTable.getScene().getWindow());

        Optional<RequestStatus> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            requestService.changeStatus(selected.getId(), result.get());
            refreshData();
            AlertHelper.showSuccess("Изменение статуса", "Статус изменён");
        } catch (BusinessException | EntityNotFoundException | DatabaseException e) {
            AlertHelper.showError("Не удалось изменить статус", e.getMessage());
        }
    }

    @FXML
    private void rateRequest() {
        ServiceRequest selected = selectedRequest();
        if (selected == null) {
            return;
        }

        Integer initialRating = selected.getRating() == null ? 5 : selected.getRating();
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(initialRating, 1, 2, 3, 4, 5);
        dialog.setTitle("Оценка заявки");
        dialog.setHeaderText("Заявка №" + selected.getId());
        dialog.setContentText("Оценка:");
        dialog.initOwner(requestsTable.getScene().getWindow());

        Optional<Integer> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            requestService.rateRequest(selected.getId(), result.get());
            refreshData();
            AlertHelper.showSuccess("Оценка заявки", "Оценка сохранена");
        } catch (BusinessException | EntityNotFoundException | DatabaseException e) {
            AlertHelper.showError("Не удалось оценить заявку", e.getMessage());
        }
    }

    private ServiceRequest selectedRequest() {
        ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Заявки", "Сначала выберите заявку в таблице");
        }
        return selected;
    }

    private void updateSelectionLabel(ServiceRequest selected) {
        if (selected == null) {
            selectionLabel.setText("Выберите строку в таблице, чтобы работать с заявкой");
        } else {
            selectionLabel.setText("Выбрана заявка №" + selected.getId()
                    + ": " + selected.getTitle());
        }
    }

    private List<User> usersWithRole(UserRole role) {
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (user.getRole() == role) {
                result.add(user);
            }
        }
        return result;
    }

    private User findUser(Integer id) {
        return id == null ? null : usersById.get(id);
    }

    private String userName(Integer id) {
        User user = findUser(id);
        return user == null ? "—" : user.getFullName();
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "—" : DATE_FORMAT.format(date);
    }

    private String formatNumber(Integer number) {
        return number == null ? "—" : number.toString();
    }

    private FXMLLoader loadForm(String fileName) {
        return new FXMLLoader(getClass().getResource(
                "/ru/mirea/project/view/" + fileName));
    }

    private Dialog<ButtonType> createDialog(String title, Parent content, ButtonType actionButton) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(requestsTable.getScene().getWindow());
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(actionButton, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource(
                "/ru/mirea/project/view/main.css").toExternalForm());
        return dialog;
    }

    private void keepDialogOpenOnInvalidInput(Dialog<ButtonType> dialog,
                                               ButtonType actionButton,
                                               InputValidator validator) {
        Button button = (Button) dialog.getDialogPane().lookupButton(actionButton);
        button.addEventFilter(ActionEvent.ACTION, event -> {
            String error = validator.validate();
            if (error != null) {
                AlertHelper.showWarning("Проверьте данные", error);
                event.consume();
            }
        });
    }

    @FunctionalInterface
    private interface InputValidator {
        String validate();
    }
}
