package ru.mirea.project.model;

import java.time.LocalDateTime;

public class ServiceRequest {

    private Integer id;
    private String title;
    private String description;
    private RequestCategory category;
    private RequestStatus status;
    private RequestPriority priority;

    private Integer clientId;
    private Integer executorId;

    private LocalDateTime createdAt;
    private LocalDateTime takenAt;
    private LocalDateTime closedAt;
    private Integer rating;

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

    public Integer getExecutorId() { return executorId; }
    public void setExecutorId(Integer executorId) { this.executorId = executorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

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
