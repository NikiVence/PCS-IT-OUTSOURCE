package ru.mirea.project.repository;

import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.ServiceRequest;

import java.util.LinkedHashMap;
import java.util.List;

public interface ServiceRequestRepository extends CrudRepository<ServiceRequest> {
    List<ServiceRequest> searchByTitle(String query);
    List<ServiceRequest> searchByDescription(String query);
    List<ServiceRequest> findByStatus(RequestStatus status);
    List<ServiceRequest> findByPriority(RequestPriority priority);
    List<ServiceRequest> findAllNewestFirst();
    List<ServiceRequest> findAllByPriorityDescending();
    LinkedHashMap<String, Number> getStatistics();
}
