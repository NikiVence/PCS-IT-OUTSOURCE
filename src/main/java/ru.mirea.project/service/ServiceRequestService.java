package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.ServiceRequest;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.repository.JdbcServiceRequestRepository;
import ru.mirea.project.repository.ServiceRequestRepository;
import ru.mirea.project.repository.UserRepository;
import ru.mirea.project.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

public class ServiceRequestService {

    private final ServiceRequestRepository requestRepository;
    private final CrudRepository<User> userRepository;

    public ServiceRequestService() {
        this(new JdbcServiceRequestRepository(), new UserRepository());
    }

    public ServiceRequestService(ServiceRequestRepository requestRepository,
                                 CrudRepository<User> userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    public ServiceRequest createRequest(String title, String description,
                                        RequestCategory category, RequestPriority priority,
                                        Integer clientId)
            throws BusinessException, EntityNotFoundException {
        validateRequestText(title, description);
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
                .orElseThrow(() -> new EntityNotFoundException(
                        "Заявка с ID=" + id + " не найдена"));
    }

    public List<ServiceRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public List<ServiceRequest> searchByTitle(String query) throws BusinessException {
        requireText(query, "Строка поиска");
        return requestRepository.searchByTitle(query.trim());
    }

    public List<ServiceRequest> searchByDescription(String query) throws BusinessException {
        requireText(query, "Строка поиска");
        return requestRepository.searchByDescription(query.trim());
    }

    public List<ServiceRequest> filterByStatus(RequestStatus status) {
        return requestRepository.findByStatus(status);
    }

    public List<ServiceRequest> filterByPriority(RequestPriority priority) {
        return requestRepository.findByPriority(priority);
    }

    public List<ServiceRequest> sortByCreatedAtNewest() {
        return requestRepository.findAllNewestFirst();
    }

    public List<ServiceRequest> sortByPriorityDescending() {
        return requestRepository.findAllByPriorityDescending();
    }

    public LinkedHashMap<String, Number> getStatistics() {
        return requestRepository.getStatistics();
    }

    public ServiceRequest updateRequest(Integer requestId, Integer actingUserId,
                                        String title, String description,
                                        RequestCategory category, RequestPriority priority)
            throws EntityNotFoundException, BusinessException {
        ServiceRequest existing = getRequestById(requestId);
        User actingUser = getUserById(actingUserId);

        if (actingUser.getRole() == UserRole.CLIENT) {
            if (!actingUserId.equals(existing.getClientId())) {
                throw new BusinessException("Клиент может изменять только собственную заявку");
            }
            if (existing.getStatus() != RequestStatus.NEW) {
                throw new BusinessException("Клиент может изменять только новую заявку");
            }
        }

        validateRequestText(title, description);
        if (category == null || priority == null) {
            throw new BusinessException("Категория и приоритет заявки обязательны");
        }

        existing.setTitle(title.trim());
        existing.setDescription(description.trim());
        existing.setCategory(category);
        existing.setPriority(priority);

        if (!requestRepository.update(existing)) {
            throw new EntityNotFoundException("Заявка с ID=" + requestId + " не найдена");
        }
        return existing;
    }

    public void deleteRequest(Integer id) throws EntityNotFoundException {
        if (!requestRepository.deleteById(id)) {
            throw new EntityNotFoundException("Заявка с ID=" + id + " не найдена");
        }
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
            throw new EntityNotFoundException("Заявка с ID=" + requestId + " не найдена");
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
            throw new EntityNotFoundException("Заявка с ID=" + requestId + " не найдена");
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
            throw new EntityNotFoundException("Заявка с ID=" + requestId + " не найдена");
        }
        return request;
    }

    private User getUserById(Integer id) throws EntityNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Пользователь с ID=" + id + " не найден"));
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
