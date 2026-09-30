package ru.mirea.project.service;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.mirea.project.model.*;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExcelExportServiceTest {
    @TempDir Path directory;
    private final ExcelExportService exporter = new ExcelExportService();

    @Test
    void exportsReadableWorkbookWithTypedCells() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 9, 15, 9, 30);
        User user = new User(2, "client1", "Иван Иванов",
                "client@example.com", UserRole.CLIENT, created);
        ServiceRequest request = new ServiceRequest(1, "=1+1", "Диагностика\nноутбука",
                RequestCategory.HARDWARE, RequestStatus.NEW, RequestPriority.HIGH,
                2, null, created, null, null, null);
        Map<String, Number> statistics = new LinkedHashMap<>();
        statistics.put("Всего заявок", 1L);
        statistics.put("Средняя оценка", 4.5);
        Path file = exporter.export(directory.resolve("nested/отчёт.xlsx"),
                List.of(user), List.of(request), statistics);

        try (var input = Files.newInputStream(file); var workbook = new XSSFWorkbook(input)) {
            assertEquals(3, workbook.getNumberOfSheets());
            Sheet users = workbook.getSheet("Пользователи");
            assertEquals(6, users.getRow(1).getLastCellNum());
            assertEquals("Иван Иванов", users.getRow(1).getCell(2).getStringCellValue());
            var row = workbook.getSheet("Заявки").getRow(1);
            assertEquals(CellType.NUMERIC, row.getCell(0).getCellType());
            assertEquals(CellType.STRING, row.getCell(1).getCellType());
            assertEquals("=1+1", row.getCell(1).getStringCellValue());
            assertEquals("Диагностика\nноутбука", row.getCell(2).getStringCellValue());
            assertEquals(CellType.BLANK, row.getCell(7).getCellType());
            assertEquals(CellType.BLANK, row.getCell(11).getCellType());
            assertTrue(DateUtil.isCellDateFormatted(row.getCell(8)));
            assertEquals(created, row.getCell(8).getLocalDateTimeCellValue());
            assertEquals(4.5, workbook.getSheet("Статистика").getRow(2).getCell(1).getNumericCellValue());
        }
    }

    @Test
    void supportsEmptyDatabase() throws Exception {
        Path file = exporter.export(directory.resolve("empty.xlsx"), List.of(), List.of(), Map.of());
        try (var input = Files.newInputStream(file); var workbook = new XSSFWorkbook(input)) {
            for (var sheet : workbook) {
                assertEquals(1, sheet.getPhysicalNumberOfRows());
            }
        }
    }

    @Test
    void refusesToOverwriteExistingFile() throws Exception {
        Path file = directory.resolve("existing.xlsx");
        Files.writeString(file, "existing report");
        assertThrows(FileAlreadyExistsException.class,
                () -> exporter.export(file, List.of(), List.of(), Map.of()));
        assertEquals("existing report", Files.readString(file));
    }

    @Test
    void rejectsWrongExtensionWithoutCreatingFile() {
        Path file = directory.resolve("report.csv");
        assertThrows(IllegalArgumentException.class,
                () -> exporter.export(file, List.of(), List.of(), Map.of()));
        assertFalse(Files.exists(file));
    }
}
