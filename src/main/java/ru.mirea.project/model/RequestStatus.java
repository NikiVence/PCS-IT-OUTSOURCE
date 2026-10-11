package ru.mirea.project.model;

public enum RequestStatus {
    NEW("Новая"),
    IN_PROGRESS("В работе"),
    WAITING("Ожидает"),
    RESOLVED("Решена"),
    CLOSED("Закрыта");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
