package ru.mirea.project;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.*;
import ru.mirea.project.service.ServiceRequestService;
import ru.mirea.project.service.UserService;
import ru.mirea.project.ui.ConsoleUI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Запускается только явно, на отдельной БД со schema.sql и seed.sql. */
@EnabledIfSystemProperty(named = "it.database", matches = "true")
class DatabaseIntegrationTest {
    @Test
    void exercisesJdbcCrudSearchAndConsoleExport() throws Exception {
        UserService users = new UserService();
        ServiceRequestService requests = new ServiceRequestService();
        assertEquals(6, users.getAllUsers().size());
        assertEquals(12, requests.getAllRequests().size());
        assertEquals(3, requests.filterByStatus(RequestStatus.NEW).size());
        assertEquals(3, requests.filterByPriority(RequestPriority.HIGH).size());
        assertEquals(1, requests.searchByTitle("НОУТБУК").size());
        assertEquals(1, requests.searchByDescription("Чёрный экран").size());
        assertEquals(RequestPriority.HIGH, requests.sortByPriorityDescending().get(0).getPriority());
        assertEquals(3, requests.sortByCreatedAtNewest().get(0).getId());
        assertEquals(4.5, requests.getStatistics().get("Средняя оценка"));

        User client = users.createUser("integration_client", "test-password", "Тестовый клиент",
                "integration@example.com", UserRole.CLIENT);
        ServiceRequest request = null;
        try {
            request = requests.createRequest("Тест JDBC", "Проверка CRUD", RequestCategory.SOFTWARE,
                    RequestPriority.LOW, client.getId());
            request.setTitle("Изменённая заявка");
            requests.updateRequest(request, client.getId());
            assertEquals("Изменённая заявка", requests.getRequestById(request.getId()).getTitle());
            requests.assignExecutor(request.getId(), 4);
            requests.changeStatus(request.getId(), RequestStatus.IN_PROGRESS);
            requests.changeStatus(request.getId(), RequestStatus.RESOLVED);
            requests.changeStatus(request.getId(), RequestStatus.CLOSED);
            requests.rateRequest(request.getId(), 5);
            assertEquals(5, requests.getRequestById(request.getId()).getRating());
        } finally {
            if (request != null) {
                requests.deleteRequest(request.getId());
            }
            users.deleteUser(client.getId());
        }
        int deletedId = request.getId();
        assertThrows(EntityNotFoundException.class, () -> requests.getRequestById(deletedId));

        Path report = Path.of("target", "integration-report-" + System.nanoTime() + ".xlsx");
        String input = "abc\n6\n4\n5\n" + report + "\n5\n" + report + "\n0\n";
        var originalIn = System.in;
        var originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capture);
            new ConsoleUI(users, requests).run();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        String console = output.toString(StandardCharsets.UTF_8);
        assertTrue(console.contains("введите целое число"));
        assertTrue(console.contains("Таблица users"));
        assertTrue(console.contains("Таблица requests"));
        assertTrue(console.contains("Отчёт Excel сохранён"));
        assertTrue(console.contains("файл уже существует"));
        assertTrue(console.contains("Работа приложения завершена"));
        try (var stream = Files.newInputStream(report); var workbook = new XSSFWorkbook(stream)) {
            assertEquals(7, workbook.getSheet("Пользователи").getPhysicalNumberOfRows());
            assertEquals(13, workbook.getSheet("Заявки").getPhysicalNumberOfRows());
            assertEquals(8, workbook.getSheet("Статистика").getPhysicalNumberOfRows());
        }
    }
}
