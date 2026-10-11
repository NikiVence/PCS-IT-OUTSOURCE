package ru.mirea.project.ui;

import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;

final class RequestFormValidator {

    private RequestFormValidator() {
    }

    static String validate(String title, String description,
                           RequestCategory category, RequestPriority priority) {
        String trimmedTitle = title.trim();
        if (trimmedTitle.length() < 3) {
            return "Заголовок должен содержать не менее 3 символов";
        }
        if (trimmedTitle.length() > 150) {
            return "Заголовок должен содержать не более 150 символов";
        }
        if (description.isBlank()) {
            return "Описание не должно быть пустым";
        }
        if (category == null) {
            return "Выберите категорию";
        }
        if (priority == null) {
            return "Выберите приоритет";
        }
        return null;
    }
}
