package ru.mirea.project.model;

public enum UserRole {
    CLIENT("Клиент"),
    EXECUTOR("Исполнитель"),
    ADMIN("Администратор");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
