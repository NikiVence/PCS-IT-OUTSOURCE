package ru.mirea.project.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Заявка на IT-обслуживание — основная сущность системы.
 * Соответствует строке таблицы {@code requests} в PostgreSQL.
 *
 * Связана с двумя пользователями:
 * <ul>
 *     <li>{@link #clientId}   — кто создал заявку (обязательно)</li>
 *     <li>{@link #executorId} — кто взял в работу (может быть null, если ещё не назначен)</li>
 * </ul>
 */
public class ServiceRequest {

    // ============================================================
    // Поля — соответствуют колонкам таблицы requests в БД
    // ============================================================
    private Integer id;
    private String title;
    private String description;
    private RequestCategory category;
    private RequestStatus status;
    private RequestPriority priority;

    private Integer clientId;               // FK → users.id, NOT NULL
    private Integer executorId;             // FK → users.id, NULL пока не назначен

    private LocalDateTime createdAt;
    private LocalDateTime takenAt;          // когда исполнитель взял в работу
    private LocalDateTime closedAt;         // когда заявка закрыта
    private Integer rating;                 // 1..5, только для CLOSED

    // ============================================================
    // Конструкторы
    // ============================================================

    public ServiceRequest() {
    }

    /**
     * Конструктор для создания новой заявки клиентом.
     * status по умолчанию = NEW, executorId = null.
     */
    public ServiceRequest(String title, String description,
                          RequestCategory category, RequestPriority priority,
                          Integer clientId) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.clientId = clientId;
        this.status = RequestStatus.NEW;
    }

    /**
     * Полный конструктор — для чтения из БД.
     */
    public ServiceRequest(Integer id, String title, String description,
                          RequestCategory category, RequestStatus status,
                          RequestPriority priority, Integer clientId, Integer executorId,
                          LocalDateTime createdAt, LocalDateTime takenAt,
                          LocalDateTime closedAt, Integer rating) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.clientId = clientId;
        this.executorId = executorId;
        this.createdAt = createdAt;
        this.takenAt = takenAt;
        this.closedAt = closedAt;
        this.rating = rating;
    }

    // ============================================================
    // Геттеры и сеттеры
    // ============================================================

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RequestCategory getCategory() { return category; }
    public void setCategory(RequestCategory category) { this.category = category; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public RequestPriority getPriority() { return priority; }
    public void setPriority(RequestPriority priority) { this.priority = priority; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public Integer getExecutorId() { return executorId; }
    public void setExecutorId(Integer executorId) { this.executorId = executorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    // ============================================================
    // equals, hashCode, toString
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceRequest that = (ServiceRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", status=" + status +
                ", priority=" + priority +
                ", clientId=" + clientId +
                ", executorId=" + executorId +
                ", createdAt=" + createdAt +
                ", rating=" + rating +
                '}';
    }
}