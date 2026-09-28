package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.ServiceRequest;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.repository.ServiceRequestRepository;
import ru.mirea.project.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class ServiceRequestService {

    private final ServiceRequestRepository requestRepository;
    private final UserRepository userRepository;

    public ServiceRequestService() {
        this(new ServiceRequestRepository(), new UserRepository());
    }

    public ServiceRequestService(ServiceRequestRepository requestRepository,
                                 UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    public ServiceRequest createRequest(String title, String description,
                                        RequestCategory category, RequestPriority priority,
                                        Integer clientId)
            throws BusinessException, EntityNotFoundException {
        requireText(title, "Заголовок");
        if (title.trim().length() < 3) {
            throw new BusinessException("Заголовок должен содержать не менее 3 символов");
        }
        requireText(description, "Описание");
        if (category == null) {
            throw new BusinessException("Категория заявки обязательна");
        }
        if (priority == null) {
            throw new BusinessException("Приоритет заявки обязателен");
        }

        User client = getUserById(clientId);
        if (client.getRole() != UserRole.CLIENT) {
            throw new BusinessException("Создать заявку может только пользователь с ролью CLIENT");
        }

        ServiceRequest request = new ServiceRequest(
                title.trim(), description.trim(), category, priority, clientId);
        return requestRepository.create(request);
    }

    public ServiceRequest getRequestById(Integer id) throws EntityNotFoundException {
        return requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ServiceRequest", id));
    }

    public List<ServiceRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public List<ServiceRequest> searchByTitle(String query) throws BusinessException {
        requireText(query, "Строка поиска");
        String queryLowerCase = query.trim().toLowerCase(Locale.ROOT);

        return requestRepository.findAll().stream()
                .filter(request -> request.getTitle() != null
                        && request.getTitle().toLowerCase(Locale.ROOT).contains(queryLowerCase))
                .toList();
    }

    public List<ServiceRequest> searchByDescription(String query) throws BusinessException {
        requireText(query, "Строка поиска");
        String queryLowerCase = query.trim().toLowerCase(Locale.ROOT);

        return requestRepository.findAll().stream()
                .filter(request -> request.getDescription() != null
                        && request.getDescription().toLowerCase(Locale.ROOT).contains(queryLowerCase))
                .toList();
    }

    public List<ServiceRequest> filterByStatus(RequestStatus status) {
        return requestRepository.findAll().stream()
                .filter(request -> request.getStatus() == status)
                .toList();
    }

    public List<ServiceRequest> filterByPriority(RequestPriority priority) {
        return requestRepository.findAll().stream()
                .filter(request -> request.getPriority() == priority)
                .toList();
    }

    public List<ServiceRequest> sortByCreatedAtNewest() {
        return requestRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        ServiceRequest::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<ServiceRequest> sortByPriorityDescending() {
        return requestRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        ServiceRequest::getPriority,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public LinkedHashMap<String, Number> getStatistics() {
        List<User> users = userRepository.findAll();
        List<ServiceRequest> requests = requestRepository.findAll();

        long newRequests = requests.stream()
                .filter(request -> request.getStatus() == RequestStatus.NEW)
                .count();
        long activeRequests = requests.stream()
                .filter(request -> request.getStatus() == RequestStatus.IN_PROGRESS
                        || request.getStatus() == RequestStatus.WAITING)
                .count();
        long closedRequests = requests.stream()
                .filter(request -> request.getStatus() == RequestStatus.CLOSED)
                .count();
        long highPriorityRequests = requests.stream()
                .filter(request -> request.getPriority() == RequestPriority.HIGH)
                .count();
        double averageRating = requests.stream()
                .filter(request -> request.getStatus() == RequestStatus.CLOSED)
                .filter(request -> request.getRating() != null && request.getRating() != 0)
                .mapToInt(ServiceRequest::getRating)
                .average()
                .orElse(0.0);

        LinkedHashMap<String, Number> statistics = new LinkedHashMap<>();
        statistics.put("Всего пользователей", (long) users.size());
        statistics.put("Всего заявок", (long) requests.size());
        statistics.put("Новых заявок", newRequests);
        statistics.put("Активных заявок", activeRequests);
        statistics.put("Закрытых заявок", closedRequests);
        statistics.put("Заявок высокого приоритета", highPriorityRequests);
        statistics.put("Средняя оценка", averageRating);
        return statistics;
    }

    public ServiceRequest updateRequest(ServiceRequest changes, Integer actingUserId)
            throws EntityNotFoundException, BusinessException {
        if (changes == null || changes.getId() == null) {
            throw new BusinessException("Для изменения заявки необходим её ID");
        }

        ServiceRequest existing = getRequestById(changes.getId());
        User actingUser = getUserById(actingUserId);

        if (actingUser.getRole() == UserRole.CLIENT) {
            if (!actingUserId.equals(existing.getClientId())) {
                throw new BusinessException("Клиент может изменять только собственную заявку");
            }
            if (existing.getStatus() != RequestStatus.NEW) {
                throw new BusinessException("Клиент может изменять только новую заявку");
            }
        }

        validateRequestText(changes.getTitle(), changes.getDescription());
        if (changes.getCategory() == null || changes.getPriority() == null) {
            throw new BusinessException("Категория и приоритет заявки обязательны");
        }

        existing.setTitle(changes.getTitle().trim());
        existing.setDescription(changes.getDescription().trim());
        existing.setCategory(changes.getCategory());
        existing.setPriority(changes.getPriority());

        if (!requestRepository.update(existing)) {
            throw new EntityNotFoundException("ServiceRequest", changes.getId());
        }
        return existing;
    }

    public void deleteRequest(Integer id) throws EntityNotFoundException, BusinessException {
        getRequestById(id);
        throw new BusinessException("Заявку нельзя удалить; её можно только закрыть");
    }

    public ServiceRequest assignExecutor(Integer requestId, Integer executorId)
            throws EntityNotFoundException, BusinessException {
        ServiceRequest request = getRequestById(requestId);
        User executor = getUserById(executorId);
        if (executor.getRole() != UserRole.EXECUTOR) {
            throw new BusinessException("Исполнителем может быть только пользователь с ролью EXECUTOR");
        }

        request.setExecutorId(executorId);
        if (!requestRepository.update(request)) {
            throw new EntityNotFoundException("ServiceRequest", requestId);
        }
        return request;
    }

    public ServiceRequest changeStatus(Integer requestId, RequestStatus newStatus)
            throws EntityNotFoundException, BusinessException {
        ServiceRequest request = getRequestById(requestId);
        if (newStatus == null || !isAllowedTransition(request.getStatus(), newStatus)) {
            throw new BusinessException("Недопустимый переход статуса заявки");
        }

        if (newStatus == RequestStatus.IN_PROGRESS && request.getTakenAt() == null) {
            request.setTakenAt(LocalDateTime.now());
        }
        if (newStatus == RequestStatus.CLOSED) {
            request.setClosedAt(LocalDateTime.now());
        }
        request.setStatus(newStatus);

        if (!requestRepository.update(request)) {
            throw new EntityNotFoundException("ServiceRequest", requestId);
        }
        return request;
    }

    public ServiceRequest rateRequest(Integer requestId, Integer rating)
            throws EntityNotFoundException, BusinessException {
        ServiceRequest request = getRequestById(requestId);
        if (request.getStatus() != RequestStatus.CLOSED) {
            throw new BusinessException("Оценить можно только закрытую заявку");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("Оценка должна быть от 1 до 5");
        }

        request.setRating(rating);
        if (!requestRepository.update(request)) {
            throw new EntityNotFoundException("ServiceRequest", requestId);
        }
        return request;
    }

    private User getUserById(Integer id) throws EntityNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    private void validateRequestText(String title, String description) throws BusinessException {
        requireText(title, "Заголовок");
        if (title.trim().length() < 3) {
            throw new BusinessException("Заголовок должен содержать не менее 3 символов");
        }
        requireText(description, "Описание");
    }

    private void requireText(String value, String fieldName) throws BusinessException {
        if (value == null || value.isBlank()) {
            throw new BusinessException(fieldName + " не должно быть пустым");
        }
    }

    private boolean isAllowedTransition(RequestStatus current, RequestStatus next) {
        return (current == RequestStatus.NEW && next == RequestStatus.IN_PROGRESS)
                || (current == RequestStatus.IN_PROGRESS && next == RequestStatus.WAITING)
                || (current == RequestStatus.WAITING && next == RequestStatus.IN_PROGRESS)
                || (current == RequestStatus.IN_PROGRESS && next == RequestStatus.RESOLVED)
                || (current == RequestStatus.RESOLVED && next == RequestStatus.CLOSED);
    }
}
