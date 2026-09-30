package ru.mirea.project.ui;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.DatabaseException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.ServiceRequest;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.service.ServiceRequestService;
import ru.mirea.project.service.UserService;
import ru.mirea.project.service.ExcelExportService;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUI {

    private final UserService userService;
    private final ServiceRequestService requestService;
    private final Scanner scanner;

    public ConsoleUI(UserService userService, ServiceRequestService requestService) {
        this.userService = userService;
        this.requestService = requestService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        try {
            while (true) {
                printMainMenu();
                int choice = readChoice("Выберите действие: ", 0, 6,
                        "Ошибка: выберите существующий пункт меню.");
                switch (choice) {
                    case 1 -> userMenu();
                    case 2 -> requestMenu();
                    case 3 -> requestSearchMenu();
                    case 4 -> {
                        try {
                            showStatistics();
                        } catch (DatabaseException e) {
                            printDatabaseError(e);
                        }
                    }
                    case 5 -> exportExcel();
                    case 6 -> {
                        try {
                            showDatabaseTables();
                        } catch (DatabaseException e) {
                            printDatabaseError(e);
                        }
                    }
                    case 0 -> {
                        System.out.println("Работа приложения завершена.");
                        return;
                    }
                    default -> throw new IllegalStateException("Недопустимый пункт главного меню");
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nВвод завершён. Приложение закрыто.");
        }
    }

    private void userMenu() {
        while (true) {
            printUserMenu();
            try {
                int choice = readChoice("Выберите действие: ", 0, 6,
                        "Ошибка: выберите существующий пункт меню.");
                switch (choice) {
                    case 1 -> createUser();
                    case 2 -> showAllUsers();
                    case 3 -> showUserById();
                    case 4 -> showUserByUsername();
                    case 5 -> updateUser();
                    case 6 -> deleteUser();
                    case 0 -> { return; }
                    default -> throw new IllegalStateException("Недопустимый пункт меню пользователей");
                }
            } catch (BusinessException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (EntityNotFoundException e) {
                printNotFoundError(e);
            } catch (DatabaseException e) {
                printDatabaseError(e);
            }
        }
    }

    private void requestMenu() {
        while (true) {
            printRequestMenu();
            try {
                int choice = readChoice("Выберите действие: ", 0, 8,
                        "Ошибка: выберите существующий пункт меню.");
                switch (choice) {
                    case 1 -> createRequest();
                    case 2 -> showAllRequests();
                    case 3 -> showRequestById();
                    case 4 -> updateRequest();
                    case 5 -> assignExecutor();
                    case 6 -> changeStatus();
                    case 7 -> rateRequest();
                    case 8 -> deleteRequest();
                    case 0 -> { return; }
                    default -> throw new IllegalStateException("Недопустимый пункт меню заявок");
                }
            } catch (BusinessException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (EntityNotFoundException e) {
                printNotFoundError(e);
            } catch (DatabaseException e) {
                printDatabaseError(e);
            }
        }
    }

    private void requestSearchMenu() {
        while (true) {
            printRequestSearchMenu();
            try {
                int choice = readChoice("Выберите действие: ", 0, 6,
                        "Ошибка: выберите существующий пункт меню.");
                switch (choice) {
                    case 1 -> searchRequestsByTitle();
                    case 2 -> searchRequestsByDescription();
                    case 3 -> printRequests(requestService.filterByStatus(readStatus()));
                    case 4 -> printRequests(requestService.filterByPriority(readPriority()));
                    case 5 -> printRequests(requestService.sortByCreatedAtNewest());
                    case 6 -> printRequests(requestService.sortByPriorityDescending());
                    case 0 -> { return; }
                    default -> throw new IllegalStateException("Недопустимый пункт меню поиска");
                }
            } catch (BusinessException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DatabaseException e) {
                printDatabaseError(e);
            }
        }
    }

    private void createUser() throws BusinessException {
        System.out.println("\n--- Создание пользователя ---");
        String username = readRequiredText("Введите username: ");
        String fullName = readRequiredText("Введите ФИО: ");
        String email = readRequiredText("Введите email: ");
        UserRole role = readUserRole(false, null);

        User user = userService.createUser(username, fullName, email, role);
        System.out.println("Пользователь успешно создан. ID: " + user.getId());
    }

    private void showAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Пользователи не найдены.");
            return;
        }

        System.out.printf("%-5s | %-20s | %-28s | %-30s | %-10s%n",
                "ID", "Username", "ФИО", "Email", "Role");
        System.out.println("-----------------------------------------------------------------------------------------------");
        for (User user : users) {
            System.out.printf("%-5d | %-20.20s | %-28.28s | %-30.30s | %-10s%n",
                    user.getId(), user.getUsername(), user.getFullName(),
                    user.getEmail(), user.getRole());
        }
    }

    private void showUserById() throws EntityNotFoundException {
        int id = readPositiveInt("Введите ID пользователя: ");
        printUser(userService.getUserById(id));
    }

    private void showUserByUsername() {
        String username = readRequiredText("Введите username: ");
        Optional<User> result = userService.findByUsername(username);
        if (result.isPresent()) {
            printUser(result.get());
        } else {
            System.out.println("Пользователь с таким username не найден.");
        }
    }

    private void updateUser() throws EntityNotFoundException, BusinessException {
        int id = readPositiveInt("Введите ID пользователя: ");
        User current = userService.getUserById(id);
        System.out.println("Оставьте поле пустым, чтобы не менять его.");

        String username = readLine("Username [" + current.getUsername() + "]: ");
        String fullName = readLine("ФИО [" + current.getFullName() + "]: ");
        String email = readLine("Email [" + current.getEmail() + "]: ");
        UserRole role = readUserRole(true, current.getRole());

        userService.updateUser(id,
                username.isBlank() ? current.getUsername() : username,
                fullName.isBlank() ? current.getFullName() : fullName,
                email.isBlank() ? current.getEmail() : email,
                role);
        System.out.println("Данные пользователя обновлены.");
    }

    private void deleteUser() throws EntityNotFoundException {
        int id = readPositiveInt("Введите ID пользователя: ");
        if (!confirm("Вы действительно хотите удалить пользователя? (y/n): ")) {
            System.out.println("Удаление отменено.");
            return;
        }
        userService.deleteUser(id);
        System.out.println("Пользователь удалён.");
    }

    private void createRequest() throws BusinessException, EntityNotFoundException {
        System.out.println("\n--- Создание заявки ---");
        String title = readRequiredText("Введите title: ");
        String description = readRequiredText("Введите description: ");
        RequestCategory category = readCategory();
        RequestPriority priority = readPriority();
        int clientId = readPositiveInt("Введите ID клиента: ");

        ServiceRequest request = requestService.createRequest(
                title, description, category, priority, clientId);
        System.out.println("Заявка успешно создана. ID: " + request.getId());
    }

    private void showAllRequests() {
        printRequests(requestService.getAllRequests());
    }

    private void searchRequestsByTitle() throws BusinessException {
        String query = readRequiredText("Введите часть заголовка: ");
        printRequests(requestService.searchByTitle(query));
    }

    private void searchRequestsByDescription() throws BusinessException {
        String query = readRequiredText("Введите часть описания: ");
        printRequests(requestService.searchByDescription(query));
    }

    private void showStatistics() {
        Map<String, Number> statistics = requestService.getStatistics();

        System.out.println("\n========================================");
        System.out.println("              СТАТИСТИКА");
        System.out.println("========================================");
        for (Map.Entry<String, Number> entry : statistics.entrySet()) {
            Number value = entry.getValue();
            if (value instanceof Double) {
                System.out.printf(Locale.US, "%s: %.2f%n", entry.getKey(), value.doubleValue());
            } else {
                System.out.printf("%s: %d%n", entry.getKey(), value.longValue());
            }
        }
    }

    private void exportExcel() {
        String destination = readRequiredText("Путь к новому файлу .xlsx: ").trim();
        try {
            Path file = new ExcelExportService().export(Path.of(destination),
                    userService.getAllUsers(), requestService.getAllRequests(), requestService.getStatistics());
            System.out.println("Отчёт Excel сохранён: " + file);
        } catch (FileAlreadyExistsException e) {
            System.out.println("Ошибка: файл уже существует. Укажите другое имя.");
        } catch (IOException e) {
            System.out.println("Не удалось сохранить отчёт Excel: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка экспорта: " + e.getMessage());
        } catch (DatabaseException e) {
            printDatabaseError(e);
        }
    }

    private void showDatabaseTables() {
        System.out.println("\n--- Таблица users ---");
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Пользователи не найдены.");
        }
        for (User user : users) {
            printUser(user);
            System.out.println();
        }
        System.out.println("\n--- Таблица requests ---");
        List<ServiceRequest> requests = requestService.getAllRequests();
        if (requests.isEmpty()) {
            System.out.println("Заявки не найдены.");
        }
        for (ServiceRequest request : requests) {
            printRequestDetails(request);
            System.out.println();
        }
    }

    private void printRequests(List<ServiceRequest> requests) {
        if (requests.isEmpty()) {
            System.out.println("Заявки не найдены.");
            return;
        }

        System.out.printf("%-5s | %-24s | %-14s | %-12s | %-8s | %-9s | %-12s | %-19s | %-6s%n",
                "ID", "Title", "Category", "Status", "Priority", "Client ID",
                "Executor ID", "Created At", "Rating");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        for (ServiceRequest request : requests) {
            System.out.printf("%-5d | %-24.24s | %-14s | %-12s | %-8s | %-9d | %-12s | %-19s | %-6s%n",
                    request.getId(), request.getTitle(), request.getCategory(), request.getStatus(),
                    request.getPriority(), request.getClientId(), displayValue(request.getExecutorId()),
                    displayValue(request.getCreatedAt()), displayValue(request.getRating()));
        }
    }

    private void showRequestById() throws EntityNotFoundException {
        int id = readPositiveInt("Введите ID заявки: ");
        printRequestDetails(requestService.getRequestById(id));
    }

    private void updateRequest() throws EntityNotFoundException, BusinessException {
        int id = readPositiveInt("Введите ID заявки: ");
        int actingUserId = readPositiveInt("Введите ID пользователя, выполняющего изменение: ");
        String title = readRequiredText("Введите новый title: ");
        String description = readRequiredText("Введите новое description: ");
        RequestCategory category = readCategory();
        RequestPriority priority = readPriority();

        requestService.updateRequest(id, actingUserId, title, description, category, priority);
        System.out.println("Заявка обновлена.");
    }

    private void assignExecutor() throws EntityNotFoundException, BusinessException {
        int requestId = readPositiveInt("Введите ID заявки: ");
        int executorId = readPositiveInt("Введите ID исполнителя: ");
        requestService.assignExecutor(requestId, executorId);
        System.out.println("Исполнитель назначен.");
    }

    private void changeStatus() throws EntityNotFoundException, BusinessException {
        int requestId = readPositiveInt("Введите ID заявки: ");
        RequestStatus status = readStatus();
        requestService.changeStatus(requestId, status);
        System.out.println("Статус заявки изменён.");
    }

    private void rateRequest() throws EntityNotFoundException, BusinessException {
        int requestId = readPositiveInt("Введите ID заявки: ");
        int rating = readInteger("Введите оценку от 1 до 5: ");
        requestService.rateRequest(requestId, rating);
        System.out.println("Оценка сохранена.");
    }

    private void deleteRequest() throws EntityNotFoundException {
        int id = readPositiveInt("Введите ID заявки: ");
        if (!confirm("Вы действительно хотите удалить заявку? (y/n): ")) {
            System.out.println("Удаление отменено.");
            return;
        }
        requestService.deleteRequest(id);
        System.out.println("Заявка удалена.");
    }

    private UserRole readUserRole(boolean allowCurrent, UserRole current) {
        System.out.println("Роль:");
        if (allowCurrent) {
            System.out.println("0. Оставить текущую (" + current + ")");
        }
        System.out.println("1. CLIENT\n2. EXECUTOR\n3. ADMIN");
        int choice = readChoice("Выберите роль: ", allowCurrent ? 0 : 1, 3,
                "Ошибка: выберите существующую роль.");
        return switch (choice) {
            case 0 -> current;
            case 1 -> UserRole.CLIENT;
            case 2 -> UserRole.EXECUTOR;
            case 3 -> UserRole.ADMIN;
            default -> throw new IllegalStateException("Недопустимая роль");
        };
    }

    private RequestCategory readCategory() {
        System.out.println("Категория:\n1. HARDWARE\n2. SOFTWARE\n3. NETWORK\n4. CONSULTATION");
        return switch (readChoice("Выберите категорию: ", 1, 4,
                "Ошибка: выберите существующую категорию.")) {
            case 1 -> RequestCategory.HARDWARE;
            case 2 -> RequestCategory.SOFTWARE;
            case 3 -> RequestCategory.NETWORK;
            case 4 -> RequestCategory.CONSULTATION;
            default -> throw new IllegalStateException("Недопустимая категория");
        };
    }

    private RequestPriority readPriority() {
        System.out.println("Приоритет:\n1. LOW\n2. MEDIUM\n3. HIGH");
        return switch (readChoice("Выберите приоритет: ", 1, 3,
                "Ошибка: выберите существующий приоритет.")) {
            case 1 -> RequestPriority.LOW;
            case 2 -> RequestPriority.MEDIUM;
            case 3 -> RequestPriority.HIGH;
            default -> throw new IllegalStateException("Недопустимый приоритет");
        };
    }

    private RequestStatus readStatus() {
        System.out.println("Статус:\n1. NEW\n2. IN_PROGRESS\n3. WAITING\n4. RESOLVED\n5. CLOSED");
        return switch (readChoice("Выберите статус: ", 1, 5,
                "Ошибка: выберите существующий статус.")) {
            case 1 -> RequestStatus.NEW;
            case 2 -> RequestStatus.IN_PROGRESS;
            case 3 -> RequestStatus.WAITING;
            case 4 -> RequestStatus.RESOLVED;
            case 5 -> RequestStatus.CLOSED;
            default -> throw new IllegalStateException("Недопустимый статус");
        };
    }

    private void printUser(User user) {
        System.out.println("ID: " + user.getId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("ФИО: " + user.getFullName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Role: " + user.getRole());
        System.out.println("Created at: " + displayValue(user.getCreatedAt()));
    }

    private void printRequestDetails(ServiceRequest request) {
        System.out.println("ID: " + request.getId());
        System.out.println("Title: " + request.getTitle());
        System.out.println("Description: " + displayValue(request.getDescription()));
        System.out.println("Category: " + request.getCategory());
        System.out.println("Status: " + request.getStatus());
        System.out.println("Priority: " + request.getPriority());
        System.out.println("Client ID: " + request.getClientId());
        System.out.println("Executor ID: " + displayValue(request.getExecutorId()));
        System.out.println("Created at: " + displayValue(request.getCreatedAt()));
        System.out.println("Taken at: " + displayValue(request.getTakenAt()));
        System.out.println("Closed at: " + displayValue(request.getClosedAt()));
        System.out.println("Rating: " + displayValue(request.getRating()));
    }

    private void printNotFoundError(EntityNotFoundException e) {
        System.out.println("Ошибка: " + e.getMessage());
    }

    private void printDatabaseError(DatabaseException e) {
        System.out.println("Ошибка базы данных: " + e.getMessage());
    }

    private boolean confirm(String prompt) {
        while (true) {
            String answer = readLine(prompt).trim();
            if (answer.equalsIgnoreCase("y")) {
                return true;
            }
            if (answer.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("Введите y или n.");
        }
    }

    private String readRequiredText(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isBlank()) {
                return value;
            }
            System.out.println("Поле не должно быть пустым.");
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInteger(prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Ошибка: ID должен быть положительным числом.");
        }
    }

    private int readChoice(String prompt, int min, int max, String errorMessage) {
        while (true) {
            int value = readInteger(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println(errorMessage);
        }
    }

    private int readInteger(String prompt) {
        while (true) {
            String value = readLine(prompt).trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private String displayValue(Object value) {
        return value == null ? "-" : value.toString();
    }

    private void printMainMenu() {
        System.out.println("\n========================================");
        System.out.println("              IT-OUTSOURCE");
        System.out.println("       Система технической поддержки");
        System.out.println("========================================");
        System.out.println("1. Пользователи");
        System.out.println("2. Заявки");
        System.out.println("3. Поиск, фильтрация и сортировка");
        System.out.println("4. Статистика");
        System.out.println("5. Экспорт заявок и статистики в Excel");
        System.out.println("6. Вывести таблицы базы данных");
        System.out.println("0. Выход");
    }

    private void printUserMenu() {
        System.out.println("\n========================================");
        System.out.println("             ПОЛЬЗОВАТЕЛИ");
        System.out.println("========================================");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Показать всех пользователей");
        System.out.println("3. Найти пользователя по ID");
        System.out.println("4. Найти пользователя по username");
        System.out.println("5. Изменить пользователя");
        System.out.println("6. Удалить пользователя");
        System.out.println("0. Назад");
    }

    private void printRequestMenu() {
        System.out.println("\n========================================");
        System.out.println("                ЗАЯВКИ");
        System.out.println("========================================");
        System.out.println("1. Создать заявку");
        System.out.println("2. Показать все заявки");
        System.out.println("3. Найти заявку по ID");
        System.out.println("4. Изменить заявку");
        System.out.println("5. Назначить исполнителя");
        System.out.println("6. Изменить статус");
        System.out.println("7. Поставить оценку");
        System.out.println("8. Удалить заявку");
        System.out.println("0. Назад");
    }

    private void printRequestSearchMenu() {
        System.out.println("\n========================================");
        System.out.println("   ПОИСК, ФИЛЬТРАЦИЯ И СОРТИРОВКА");
        System.out.println("========================================");
        System.out.println("1. Поиск по части заголовка");
        System.out.println("2. Поиск по части описания");
        System.out.println("3. Фильтр по статусу");
        System.out.println("4. Фильтр по приоритету");
        System.out.println("5. Сортировка по дате (сначала новые)");
        System.out.println("6. Сортировка по приоритету (HIGH, MEDIUM, LOW)");
        System.out.println("0. Назад");
    }
}
