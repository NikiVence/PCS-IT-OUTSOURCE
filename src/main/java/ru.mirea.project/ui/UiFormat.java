package ru.mirea.project.ui;

import javafx.util.StringConverter;
import ru.mirea.project.model.UserRole;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Единое форматирование значений для отображения в интерфейсе:
 * даты, null-значения и перечисления.
 */
public final class UiFormat {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private UiFormat() {
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "—" : value.format(DATE_TIME);
    }

    public static String role(UserRole role) {
        if (role == null) {
            return "—";
        }
        return switch (role) {
            case CLIENT -> "Клиент";
            case EXECUTOR -> "Исполнитель";
            case ADMIN -> "Администратор";
        };
    }

    public static StringConverter<UserRole> roleConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(UserRole role) {
                return role == null ? "" : role(role);
            }

            @Override
            public UserRole fromString(String text) {
                return null;
            }
        };
    }
}
