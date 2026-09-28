package ru.mirea.project.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.project.model.ServiceRequest;
import ru.mirea.project.model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Экспорт заявок и сводной статистики в книгу Excel без подключения к БД. */
public class ExcelExportService {

    private static final String[] REQUEST_HEADERS = {
            "ID", "Заголовок", "Описание", "Категория", "Статус", "Приоритет",
            "ID клиента", "ID исполнителя", "Создана", "Взята в работу", "Закрыта", "Оценка"
    };

    /** Создаёт новый файл; существующий отчёт не перезаписывается. */
    public Path export(Path destination, List<User> users, List<ServiceRequest> requests,
                       Map<String, Number> statistics) throws IOException {
        Path file = destination.toAbsolutePath().normalize();
        if (file.getFileName() == null
                || !file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new IllegalArgumentException("Укажите имя файла с расширением .xlsx");
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd.mm.yyyy hh:mm:ss"));
            CellStyle decimalStyle = workbook.createCellStyle();
            decimalStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setWrapText(true);

            Sheet userSheet = workbook.createSheet("Пользователи");
            String[] userHeaders = {"ID", "Логин", "ФИО", "Email", "Роль", "Создан"};
            writeHeader(userSheet, userHeaders, headerStyle);
            for (User user : users) {
                Row row = userSheet.createRow(userSheet.getLastRowNum() + 1);
                Object[] values = {user.getId(), user.getUsername(), user.getFullName(),
                        user.getEmail(), user.getRole(), user.getCreatedAt()};
                for (int column = 0; column < values.length; column++) {
                    writeCell(row.createCell(column), values[column], dateStyle);
                }
            }
            userSheet.setAutoFilter(new CellRangeAddress(0, userSheet.getLastRowNum(), 0, 5));
            int[] userWidths = {10, 24, 40, 35, 18, 22};
            for (int column = 0; column < userWidths.length; column++) {
                userSheet.setColumnWidth(column, userWidths[column] * 256);
            }

            Sheet requestSheet = workbook.createSheet("Заявки");
            writeHeader(requestSheet, REQUEST_HEADERS, headerStyle);
            for (ServiceRequest request : requests) {
                Row row = requestSheet.createRow(requestSheet.getLastRowNum() + 1);
                Object[] values = {
                        request.getId(), request.getTitle(), request.getDescription(),
                        request.getCategory(), request.getStatus(), request.getPriority(),
                        request.getClientId(), request.getExecutorId(), request.getCreatedAt(),
                        request.getTakenAt(), request.getClosedAt(), request.getRating()
                };
                for (int column = 0; column < values.length; column++) {
                    writeCell(row.createCell(column), values[column], dateStyle);
                }
                row.getCell(1).setCellStyle(textStyle);
                row.getCell(2).setCellStyle(textStyle);
            }
            requestSheet.setAutoFilter(new CellRangeAddress(
                    0, requestSheet.getLastRowNum(), 0, REQUEST_HEADERS.length - 1));
            int[] widths = {10, 40, 70, 20, 18, 15, 15, 18, 22, 22, 22, 10};
            for (int column = 0; column < widths.length; column++) {
                requestSheet.setColumnWidth(column, widths[column] * 256);
            }

            Sheet statisticsSheet = workbook.createSheet("Статистика");
            writeHeader(statisticsSheet, new String[]{"Показатель", "Значение"}, headerStyle);
            for (Map.Entry<String, Number> entry : statistics.entrySet()) {
                Row row = statisticsSheet.createRow(statisticsSheet.getLastRowNum() + 1);
                row.createCell(0).setCellValue(entry.getKey());
                Cell value = row.createCell(1);
                writeCell(value, entry.getValue(), dateStyle);
                if (entry.getValue() instanceof Double || entry.getValue() instanceof Float) {
                    value.setCellStyle(decimalStyle);
                }
            }
            statisticsSheet.setColumnWidth(0, 40 * 256);
            statisticsSheet.setColumnWidth(1, 18 * 256);

            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                workbook.write(output);
            }
        }
        return file;
    }

    private void writeHeader(Sheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int column = 0; column < headers.length; column++) {
            Cell cell = row.createCell(column);
            cell.setCellValue(headers[column]);
            cell.setCellStyle(style);
        }
        sheet.createFreezePane(0, 1);
    }

    private void writeCell(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof LocalDateTime date) {
            cell.setCellValue(date);
            cell.setCellStyle(dateStyle);
        } else {
            // Текст, в том числе начинающийся с '=', сохраняется именно как строка.
            cell.setCellValue(value.toString());
        }
    }
}
