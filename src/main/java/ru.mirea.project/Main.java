package ru.mirea.project;

import ru.mirea.project.model.ServiceRequest;
import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;

public class Main {
    public static void main(String[] args) {
        User user = new User("admin", "hash123", "Иванов Иван", "admin@mail.ru", UserRole.ADMIN);
        System.out.println(user);

        ServiceRequest req = new ServiceRequest(
                "Не включается ноутбук",
                "Чёрный экран после обновления",
                RequestCategory.HARDWARE,
                RequestPriority.HIGH,
                1
        );
        System.out.println(req);
        System.out.println("Статус по умолчанию: " + req.getStatus());
    }
}