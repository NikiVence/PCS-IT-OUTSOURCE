package ru.mirea.project.ui;

import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.ServiceRequest;

import java.util.Locale;
import java.util.function.Predicate;

/**
 * Условие отбора заявок для таблицы: текстовый поиск и фильтры.
 * Пустой текст или null в фильтре означает «не ограничивать».
 */
public final class RequestFilter {

    private RequestFilter() {
    }

    public static Predicate<ServiceRequest> of(String searchText,
                                               RequestStatus status,
                                               RequestPriority priority,
                                               RequestCategory category) {
        String query = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);

        return request -> (query.isEmpty()
                        || contains(request.getTitle(), query)
                        || contains(request.getDescription(), query))
                && (status == null || request.getStatus() == status)
                && (priority == null || request.getPriority() == priority)
                && (category == null || request.getCategory() == category);
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
