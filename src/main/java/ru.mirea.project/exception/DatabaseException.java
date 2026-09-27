package ru.mirea.project.exception;

/**
 * Обёртка над SQLException и другими ошибками JDBC.
 *
 * Unchecked (RuntimeException) — потому что это техническая проблема,
 * а не бизнес-логика. UI не обязан её обрабатывать каждым методом.
 *
 * Примеры причин:
 * <ul>
 *     <li>PostgreSQL не запущен</li>
 *     <li>Неверный логин/пароль в db.properties</li>
 *     <li>SQL-синтаксическая ошибка</li>
 *     <li>Нарушение FOREIGN KEY / UNIQUE / CHECK на уровне БД</li>
 * </ul>
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}