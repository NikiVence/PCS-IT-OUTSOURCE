package ru.mirea.project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.*;
import ru.mirea.project.repository.CrudRepository;
import ru.mirea.project.repository.ServiceRequestRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRequestServiceTest {
    private final MemoryRequestRepository requests = new MemoryRequestRepository();
    private final MemoryRepository<User> users = new MemoryRepository<>(User::getId, User::setId);
    private final ServiceRequestService service = new ServiceRequestService(requests, users);
    private User client;

    @BeforeEach
    void prepareClient() {
        client = users.create(new User("client", "Клиент", "c@example.com", UserRole.CLIENT));
    }

    @Test
    void deletesExistingRequestAndReportsMissingId() throws Exception {
        ServiceRequest request = createRequest();
        service.deleteRequest(request.getId());
        assertTrue(service.getAllRequests().isEmpty());
        assertThrows(EntityNotFoundException.class, () -> service.deleteRequest(request.getId()));
    }

    @Test
    void validatesRequiredFieldsAndClientRole() {
        assertThrows(BusinessException.class, () -> service.createRequest(" ", "Описание",
                RequestCategory.HARDWARE, RequestPriority.HIGH, client.getId()));
        assertThrows(EntityNotFoundException.class, () -> service.createRequest("Заявка", "Описание",
                RequestCategory.HARDWARE, RequestPriority.HIGH, 999));
        client.setRole(UserRole.EXECUTOR);
        assertThrows(BusinessException.class, this::createRequest);
    }

    @Test
    void enforcesStatusTransitionsAndRatingRules() throws Exception {
        ServiceRequest request = createRequest();
        int id = request.getId();
        assertThrows(BusinessException.class, () -> service.changeStatus(id, RequestStatus.CLOSED));
        assertThrows(BusinessException.class, () -> service.rateRequest(id, 5));
        service.changeStatus(id, RequestStatus.IN_PROGRESS);
        assertNotNull(request.getTakenAt());
        service.changeStatus(id, RequestStatus.WAITING);
        service.changeStatus(id, RequestStatus.IN_PROGRESS);
        service.changeStatus(id, RequestStatus.RESOLVED);
        service.changeStatus(id, RequestStatus.CLOSED);
        assertNotNull(request.getClosedAt());
        assertThrows(BusinessException.class, () -> service.rateRequest(id, 6));
        service.rateRequest(id, 5);
        assertEquals(5, request.getRating());
        assertEquals(5.0, service.getStatistics().get("Средняя оценка"));
    }

    private ServiceRequest createRequest() throws Exception {
        return service.createRequest("Ноутбук", "Не включается", RequestCategory.HARDWARE,
                RequestPriority.HIGH, client.getId());
    }

    private static class MemoryRequestRepository implements ServiceRequestRepository {
        private final MemoryRepository<ServiceRequest> data =
                new MemoryRepository<>(ServiceRequest::getId, ServiceRequest::setId);

        public ServiceRequest create(ServiceRequest request) { return data.create(request); }
        public Optional<ServiceRequest> findById(Integer id) { return data.findById(id); }
        public List<ServiceRequest> findAll() { return data.findAll(); }
        public boolean update(ServiceRequest request) { return data.update(request); }
        public boolean deleteById(Integer id) { return data.deleteById(id); }

        public List<ServiceRequest> searchByTitle(String query) {
            return search(query, ServiceRequest::getTitle);
        }

        public List<ServiceRequest> searchByDescription(String query) {
            return search(query, ServiceRequest::getDescription);
        }

        private List<ServiceRequest> search(String query, Function<ServiceRequest, String> field) {
            String expected = query.toLowerCase(Locale.ROOT);
            return findAll().stream()
                    .filter(request -> field.apply(request) != null
                            && field.apply(request).toLowerCase(Locale.ROOT).contains(expected))
                    .toList();
        }

        public List<ServiceRequest> findByStatus(RequestStatus status) {
            return findAll().stream().filter(request -> request.getStatus() == status).toList();
        }

        public List<ServiceRequest> findByPriority(RequestPriority priority) {
            return findAll().stream().filter(request -> request.getPriority() == priority).toList();
        }

        public List<ServiceRequest> findAllNewestFirst() {
            return findAll().stream()
                    .sorted(Comparator.comparing(ServiceRequest::getCreatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        }

        public List<ServiceRequest> findAllByPriorityDescending() {
            return findAll().stream()
                    .sorted(Comparator.comparing(ServiceRequest::getPriority).reversed())
                    .toList();
        }

        public LinkedHashMap<String, Number> getStatistics() {
            double average = findAll().stream()
                    .filter(request -> request.getStatus() == RequestStatus.CLOSED)
                    .filter(request -> request.getRating() != null)
                    .mapToInt(ServiceRequest::getRating)
                    .average()
                    .orElse(0);
            LinkedHashMap<String, Number> result = new LinkedHashMap<>();
            result.put("Средняя оценка", average);
            return result;
        }
    }

    private static class MemoryRepository<T> implements CrudRepository<T> {
        private final Map<Integer, T> entries = new LinkedHashMap<>();
        private final Function<T, Integer> getId;
        private final BiConsumer<T, Integer> setId;
        private int nextId = 1;

        MemoryRepository(Function<T, Integer> getId, BiConsumer<T, Integer> setId) {
            this.getId = getId;
            this.setId = setId;
        }

        public T create(T entity) {
            setId.accept(entity, nextId++);
            entries.put(getId.apply(entity), entity);
            return entity;
        }
        public Optional<T> findById(Integer id) { return Optional.ofNullable(entries.get(id)); }
        public List<T> findAll() { return new ArrayList<>(entries.values()); }
        public boolean update(T entity) { return entries.replace(getId.apply(entity), entity) != null; }
        public boolean deleteById(Integer id) { return entries.remove(id) != null; }
    }
}
