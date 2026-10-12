package ru.mirea.project.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import ru.mirea.project.ItOutsourceApplication;
import ru.mirea.project.exception.DatabaseException;
import ru.mirea.project.model.ServiceRequest;
import ru.mirea.project.model.User;
import ru.mirea.project.service.ExcelExportService;
import ru.mirea.project.service.ServiceRequestService;
import ru.mirea.project.service.UserService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Требует графическую сессию: mvn -Dit.gui=true test. Данные БД по умолчанию заменены. */
@EnabledIfSystemProperty(named = "it.gui", matches = "true")
class StatisticsControllerTest {
    @TempDir Path directory;

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            started.countDown();
        });
        assertTrue(started.await(15, TimeUnit.SECONDS));
    }

    @Test
    void loadsFxmlAndDisplaysSevenServiceMetrics() throws Exception {
        Harness view = open(new RequestsStub(), new UsersStub());
        awaitIdle(view);
        fx(() -> {
            assertEquals(7, view.table().getItems().size());
            assertEquals("Всего пользователей", view.table().getItems().getFirst().getKey());
            assertEquals("4,50", view.table().getColumns().get(1).getCellData(6));
            return null;
        });
    }

    @Test
    void exportsDisplayedMetricsToReadableWorkbook() throws Exception {
        Harness view = open(new RequestsStub(), new UsersStub());
        awaitIdle(view);
        Path file = directory.resolve("отчёт.xlsx");
        fx(() -> { view.controller.destination = file.toFile(); view.export().fire(); return null; });
        awaitIdle(view);
        assertNotNull(view.controller.success);
        assertNull(view.controller.error);
        try (var input = Files.newInputStream(file); var workbook = new XSSFWorkbook(input)) {
            assertEquals(3, workbook.getNumberOfSheets());
            var sheet = workbook.getSheet("Статистика");
            assertEquals(8, sheet.getPhysicalNumberOfRows());
            int row = 1;
            for (var metric : metrics().entrySet()) {
                assertEquals(metric.getKey(), sheet.getRow(row).getCell(0).getStringCellValue());
                assertEquals(metric.getValue().doubleValue(), sheet.getRow(row++).getCell(1).getNumericCellValue());
            }
        }
    }

    @Test
    void cancellationDoesNotFetchDataOrCreateReport() throws Exception {
        RequestsStub requests = new RequestsStub();
        Harness view = open(requests, new UsersStub());
        awaitIdle(view);
        fx(() -> { view.export().fire(); return null; });
        assertEquals(0, requests.listCalls);
        assertNull(view.controller.success);
        assertNull(view.controller.error);
        try (var files = Files.list(directory)) { assertEquals(0, files.count()); }
    }

    @Test
    void existingReportIsPreservedAndErrorIsShown() throws Exception {
        Path file = directory.resolve("existing.xlsx");
        Files.writeString(file, "original");
        Harness view = open(new RequestsStub(), new UsersStub());
        awaitIdle(view);
        fx(() -> { view.controller.destination = file.toFile(); view.export().fire(); return null; });
        awaitIdle(view);
        assertEquals("original", Files.readString(file));
        assertTrue(view.controller.error.contains("Файл уже существует"));
        assertNull(view.controller.success);
    }

    @Test
    void failedRefreshClearsStaleMetricsAndCanBeRetried() throws Exception {
        RequestsStub requests = new RequestsStub();
        Harness view = open(requests, new UsersStub());
        awaitIdle(view);
        requests.fail = true;
        fx(() -> { view.refresh().fire(); return null; });
        awaitIdle(view);
        assertTrue(view.controller.error.contains("БД недоступна"));
        assertEquals(0, fx(() -> view.table().getItems().size()));
        requests.fail = false;
        fx(() -> { view.refresh().fire(); return null; });
        awaitIdle(view);
        assertEquals(7, fx(() -> view.table().getItems().size()));
    }

    @Test
    void databaseFailureDuringExportDoesNotCreateFile() throws Exception {
        RequestsStub requests = new RequestsStub();
        Harness view = open(requests, new UsersStub());
        awaitIdle(view);
        requests.fail = true;
        Path file = directory.resolve("failed.xlsx");
        fx(() -> { view.controller.destination = file.toFile(); view.export().fire(); return null; });
        awaitIdle(view);
        assertFalse(Files.exists(file));
        assertNotNull(view.controller.error);
        assertNull(view.controller.success);
    }

    /** Только чтение настоящей БД; FileChooser заменён выбранным временным путём. */
    @Test
    @EnabledIfSystemProperty(named = "it.gui.database", matches = "true")
    void startsApplicationNavigatesToStatisticsAndExportsRealDatabase() throws Exception {
        Stage stage = fx(() -> {
            Stage window = new Stage();
            new ItOutsourceApplication().start(window);
            ((Button) window.getScene().lookup("#statisticsButton")).fire();
            return window;
        });
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
            while (fx(() -> ((Button) stage.getScene().lookup("#refreshButton")).isDisabled())) {
                assertTrue(System.nanoTime() < deadline, "Statistics did not load");
                Thread.sleep(25);
            }
            assertEquals(7, fx(() -> ((TableView<?>) stage.getScene().lookup("#statisticsTable")).getItems().size()));
            Harness view = open(new ServiceRequestService(), new UserService());
            awaitIdle(view);
            Path file = directory.resolve("database-report.xlsx");
            fx(() -> { view.controller.destination = file.toFile(); view.export().fire(); return null; });
            awaitIdle(view);
            assertNull(view.controller.error);
            assertNotNull(view.controller.success);
            try (var input = Files.newInputStream(file); var workbook = new XSSFWorkbook(input)) {
                assertEquals(new UserService().getAllUsers().size() + 1,
                        workbook.getSheet("Пользователи").getPhysicalNumberOfRows());
                assertEquals(new ServiceRequestService().getAllRequests().size() + 1,
                        workbook.getSheet("Заявки").getPhysicalNumberOfRows());
                assertEquals(8, workbook.getSheet("Статистика").getPhysicalNumberOfRows());
            }
        } finally {
            fx(() -> { stage.close(); return null; });
        }
    }

    private Harness open(ServiceRequestService requests, UserService users) throws Exception {
        return fx(() -> {
            TestController controller = new TestController();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/mirea/project/view/statistics-view.fxml"));
            loader.setControllerFactory(type -> controller);
            Parent root = loader.load();
            new Scene(root, 550, 420);
            controller.setServices(requests, users, new ExcelExportService());
            return new Harness(root, controller);
        });
    }

    private static void awaitIdle(Harness view) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (fx(() -> view.refresh().isDisabled())) {
            assertTrue(System.nanoTime() < deadline, "Background operation did not finish");
            Thread.sleep(25);
        }
    }

    private static <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(30, TimeUnit.SECONDS);
    }

    private record Harness(Parent root, TestController controller) {
        Button refresh() { return (Button) root.lookup("#refreshButton"); }
        Button export() { return (Button) root.lookup("#exportButton"); }
        @SuppressWarnings("unchecked")
        TableView<Map.Entry<String, Number>> table() {
            return (TableView<Map.Entry<String, Number>>) root.lookup("#statisticsTable");
        }
    }

    private static class TestController extends StatisticsController {
        File destination;
        volatile String success;
        volatile String error;
        @Override protected File chooseDestination() { return destination; }
        @Override protected void showSuccess(String title, String message) { success = message; }
        @Override protected void showError(String title, String message) { error = message; }
    }

    private static class RequestsStub extends ServiceRequestService {
        volatile boolean fail;
        volatile int listCalls;
        RequestsStub() { super(null, null); }
        @Override public LinkedHashMap<String, Number> getStatistics() {
            if (fail) throw new DatabaseException("БД недоступна");
            return metrics();
        }
        @Override public List<ServiceRequest> getAllRequests() { listCalls++; return List.of(); }
    }

    private static class UsersStub extends UserService {
        UsersStub() { super(null); }
        @Override public List<User> getAllUsers() { return List.of(); }
    }

    private static LinkedHashMap<String, Number> metrics() {
        LinkedHashMap<String, Number> values = new LinkedHashMap<>();
        values.put("Всего пользователей", 6L);
        values.put("Всего заявок", 12L);
        values.put("Новых заявок", 3L);
        values.put("Активных заявок", 5L);
        values.put("Закрытых заявок", 2L);
        values.put("Заявок высокого приоритета", 3L);
        values.put("Средняя оценка", 4.5);
        return values;
    }
}
