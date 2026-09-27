package ru.mirea.project;

import ru.mirea.project.exception.DatabaseException;
import ru.mirea.project.service.ServiceRequestService;
import ru.mirea.project.service.UserService;
import ru.mirea.project.ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        try {
            UserService userService = new UserService();
            ServiceRequestService requestService = new ServiceRequestService();
            new ConsoleUI(userService, requestService).run();
        } catch (DatabaseException e) {
            System.err.println("Не удалось запустить приложение: ошибка базы данных. " + e.getMessage());
        }
    }
}