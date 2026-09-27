package ru.mirea.project.util;

import ru.mirea.project.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Управляет подключением к PostgreSQL.
 *
 * Singleton: единственный экземпляр на всё приложение.
 * При первом вызове {@link #getInstance()} читает db.properties,
 * загружает JDBC-драйвер и держит наготове параметры подключения.
 *
 * Пример использования в Repository:
 * <pre>
 *     try (Connection conn = DatabaseManager.getInstance().getConnection();
 *          PreparedStatement stmt = conn.prepareStatement(sql)) {
 *         ...
 *     }
 * </pre>
 */
public class DatabaseManager {

    // ============================================================
    // Singleton
    // ============================================================
    private static DatabaseManager instance;

    // ============================================================
    // Параметры подключения (читаются из db.properties)
    // ============================================================
    private final String url;
    private final String username;
    private final String password;

    /**
     * Приватный конструктор — никто снаружи не может сделать new DatabaseManager().
     * Читает db.properties из classpath.
     */
    private DatabaseManager() {
        Properties props = new Properties();
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new DatabaseException(
                        "Файл db.properties не найден в src/main/resources/. " +
                                "Скопируй db.properties.example и впиши свои данные."
                );
            }
            props.load(input);

        } catch (IOException e) {
            throw new DatabaseException("Не удалось прочитать db.properties", e);
        }

        this.url = props.getProperty("db.url");
        this.username = props.getProperty("db.user");
        this.password = props.getProperty("db.password");

        if (url == null || username == null || password == null) {
            throw new DatabaseException(
                    "В db.properties должны быть ключи: db.url, db.user, db.password"
            );
        }

        // Явно загружаем JDBC-драйвер PostgreSQL (не обязательно с JDBC 4+, но наглядно)
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException(
                    "PostgreSQL JDBC драйвер не найден. Проверь зависимость в pom.xml.", e
            );
        }
    }

    /**
     * Возвращает единственный экземпляр менеджера.
     * При первом вызове создаёт его (lazy initialization).
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ============================================================
    // Получение соединения
    // ============================================================

    /**
     * Открывает новое соединение с PostgreSQL.
     * Вызывающий код ОБЯЗАН закрыть его через try-with-resources.
     *
     * @return Connection
     * @throws DatabaseException если подключиться не удалось
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Не удалось подключиться к PostgreSQL по URL: " + url, e
            );
        }
    }

    // ============================================================
    // Геттеры (для отладки и логов)
    // ============================================================
    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }
}