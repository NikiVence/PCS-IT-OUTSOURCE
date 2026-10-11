package ru.mirea.project.model;

public enum RequestCategory {
    HARDWARE("Оборудование"),
    SOFTWARE("Программное обеспечение"),
    NETWORK("Сеть"),
    CONSULTATION("Консультация");

    private final String displayName;

    RequestCategory(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
