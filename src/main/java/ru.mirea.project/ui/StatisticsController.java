package ru.mirea.project.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import ru.mirea.project.service.ExcelExportService;
import ru.mirea.project.service.ServiceRequestService;
import ru.mirea.project.service.UserService;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Статистика и экспорт через существующие сервисы; запросы выполняются вне UI-потока. */
public class StatisticsController {
    @FXML private TableView<Map.Entry<String, Number>> statisticsTable;
    @FXML private TableColumn<Map.Entry<String, Number>, String> metricColumn;
    @FXML private TableColumn<Map.Entry<String, Number>, String> valueColumn;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Label statusLabel;

    private ServiceRequestService requestService;
    private UserService userService;
    private ExcelExportService exportService;
    private boolean busy;

    @FXML
    private void initialize() {
        metricColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getKey()));
        valueColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(formatValue(cell.getValue().getValue())));
        statisticsTable.setPlaceholder(new Label("Статистика не загружена"));
        refreshButton.setDisable(true);
        exportButton.setDisable(true);
    }

    public void setServices(ServiceRequestService requests, UserService users, ExcelExportService exporter) {
        requestService = requests;
        userService = users;
        exportService = exporter;
        refreshData();
    }

    @FXML
    private void refreshData() {
        if (busy) return;
        runTask("Загрузка статистики…", requestService::getStatistics, statistics -> {
            displayStatistics(statistics);
            statusLabel.setText("Статистика обновлена");
        }, true);
    }

    @FXML
    private void exportReport() {
        if (busy) return;
        File destination = chooseDestination();
        if (destination == null) return;
        runTask("Сохранение отчёта…", () -> {
            var users = userService.getAllUsers();
            var requests = requestService.getAllRequests();
            var statistics = requestService.getStatistics();
            Path file = exportService.export(destination.toPath(), users, requests, statistics);
            return new ExportResult(file, statistics);
        }, result -> {
            displayStatistics(result.statistics());
            statusLabel.setText("Отчёт сохранён: " + result.file());
            showSuccess("Экспорт завершён", "Отчёт Excel сохранён:\n" + result.file());
        }, false);
    }

    protected File chooseDestination() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить отчёт Excel — выберите новое имя файла");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Книга Excel (*.xlsx)", "*.xlsx"));
        chooser.setInitialFileName("it-outsource-" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".xlsx");
        return chooser.showSaveDialog(statisticsTable.getScene().getWindow());
    }

    private void displayStatistics(Map<String, Number> statistics) {
        statisticsTable.getItems().setAll(new LinkedHashMap<>(statistics).entrySet());
    }

    private <T> void runTask(String message, Callable<T> operation, Consumer<T> onSuccess,
                             boolean clearOnFailure) {
        setBusy(true);
        statusLabel.setText(message);
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return operation.call(); }
        };
        task.setOnSucceeded(event -> {
            setBusy(false);
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            setBusy(false);
            if (clearOnFailure) statisticsTable.getItems().clear();
            String error = task.getException() instanceof FileAlreadyExistsException
                    ? "Файл уже существует. Выберите другое имя: существующие отчёты не перезаписываются."
                    : "Не удалось выполнить операцию. " + task.getException().getMessage();
            statusLabel.setText(clearOnFailure ? "Не удалось загрузить статистику" : "Отчёт не сохранён");
            showError("Ошибка", error);
        });
        Thread worker = new Thread(task, "statistics-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private void setBusy(boolean value) {
        busy = value;
        refreshButton.setDisable(value);
        exportButton.setDisable(value);
    }

    static String formatValue(Number value) {
        if (value == null) return "—";
        NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru-RU"));
        format.setGroupingUsed(false);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(value instanceof Float || value instanceof Double ? 2 : 0);
        return format.format(value);
    }

    protected void showSuccess(String title, String message) { AlertHelper.showSuccess(title, message); }
    protected void showError(String title, String message) { AlertHelper.showError(title, message); }

    private record ExportResult(Path file, LinkedHashMap<String, Number> statistics) { }
}
