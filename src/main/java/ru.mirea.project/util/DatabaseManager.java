package ru.mirea.project.util;

import ru.mirea.project.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {

    private static DatabaseManager instance;

    private final String url;
    private final String username;
    private final String password;

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

    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Не удалось подключиться к PostgreSQL по URL: " + url, e
            );
        }
    }

}
