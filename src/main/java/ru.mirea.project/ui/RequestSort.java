package ru.mirea.project.ui;

import ru.mirea.project.model.ServiceRequest;

import java.util.Comparator;

/** Варианты сортировки заявок в таблице. */
public enum RequestSort {
    BY_ID("По номеру",
            Comparator.comparing(ServiceRequest::getId, Comparator.nullsLast(Comparator.naturalOrder()))),
    NEWEST_FIRST("Сначала новые",
            Comparator.comparing(ServiceRequest::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))),
    OLDEST_FIRST("Сначала старые",
            Comparator.comparing(ServiceRequest::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))),
    PRIORITY_HIGH_FIRST("По приоритету: высокий → низкий",
            Comparator.comparing(ServiceRequest::getPriority, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(ServiceRequest::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))),
    PRIORITY_LOW_FIRST("По приоритету: низкий → высокий",
            Comparator.comparing(ServiceRequest::getPriority, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(ServiceRequest::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

    private final String displayName;
    private final Comparator<ServiceRequest> comparator;

    RequestSort(String displayName, Comparator<ServiceRequest> comparator) {
        this.displayName = displayName;
        this.comparator = comparator;
    }

    public Comparator<ServiceRequest> comparator() {
        return comparator;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
