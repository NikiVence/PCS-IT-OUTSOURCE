package ru.mirea.project.repository;

import ru.mirea.project.exception.DatabaseException;
import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.ServiceRequest;
import ru.mirea.project.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceRequestRepository implements CrudRepository<ServiceRequest> {

    private final DatabaseManager databaseManager;

    public ServiceRequestRepository() {
        this(DatabaseManager.getInstance());
    }

    ServiceRequestRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public ServiceRequest create(ServiceRequest request) {
        String sql = "INSERT INTO requests (title, description, category, status, priority, " +
                "client_id, executor_id, taken_at, closed_at, rating) " +
                "VALUES (?, ?, ?::request_category, ?::request_status, ?::request_priority, " +
                "?, ?, ?, ?, ?) RETURNING *";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getTitle());
            statement.setString(2, request.getDescription());
            statement.setString(3, request.getCategory().name());
            statement.setString(4, request.getStatus().name());
            statement.setString(5, request.getPriority().name());
            statement.setObject(6, request.getClientId());
            setNullableInteger(statement, 7, request.getExecutorId());
            setTimestamp(statement, 8, request.getTakenAt());
            setTimestamp(statement, 9, request.getClosedAt());
            setNullableInteger(statement, 10, request.getRating());

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return mapRow(resultSet);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось создать заявку", e);
        }
    }

    @Override
    public Optional<ServiceRequest> findById(Integer id) {
        String sql = "SELECT * FROM requests WHERE id = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить заявку по ID", e);
        }
    }

    @Override
    public List<ServiceRequest> findAll() {
        String sql = "SELECT * FROM requests ORDER BY id";
        List<ServiceRequest> requests = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                requests.add(mapRow(resultSet));
            }
            return requests;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить список заявок", e);
        }
    }

    public List<ServiceRequest> findByClientId(Integer clientId) {
        return findAllByUserId("SELECT * FROM requests WHERE client_id = ? ORDER BY id", clientId);
    }

    public List<ServiceRequest> findByExecutorId(Integer executorId) {
        return findAllByUserId("SELECT * FROM requests WHERE executor_id = ? ORDER BY id", executorId);
    }

    @Override
    public boolean update(ServiceRequest request) {
        String sql = "UPDATE requests SET title = ?, description = ?, " +
                "category = ?::request_category, status = ?::request_status, " +
                "priority = ?::request_priority, client_id = ?, executor_id = ?, " +
                "taken_at = ?, closed_at = ?, " +
                "rating = ? WHERE id = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getTitle());
            statement.setString(2, request.getDescription());
            statement.setString(3, request.getCategory().name());
            statement.setString(4, request.getStatus().name());
            statement.setString(5, request.getPriority().name());
            statement.setObject(6, request.getClientId());
            setNullableInteger(statement, 7, request.getExecutorId());
            setTimestamp(statement, 8, request.getTakenAt());
            setTimestamp(statement, 9, request.getClosedAt());
            setNullableInteger(statement, 10, request.getRating());
            statement.setObject(11, request.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось обновить заявку", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM requests WHERE id = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось удалить заявку", e);
        }
    }

    private List<ServiceRequest> findAllByUserId(String sql, Integer userId) {
        List<ServiceRequest> requests = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRow(resultSet));
                }
            }
            return requests;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить заявки пользователя", e);
        }
    }

    private ServiceRequest mapRow(ResultSet resultSet) throws SQLException {
        return new ServiceRequest(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                RequestCategory.valueOf(resultSet.getString("category")),
                RequestStatus.valueOf(resultSet.getString("status")),
                RequestPriority.valueOf(resultSet.getString("priority")),
                resultSet.getInt("client_id"),
                getNullableInteger(resultSet, "executor_id"),
                toLocalDateTime(resultSet.getTimestamp("created_at")),
                toLocalDateTime(resultSet.getTimestamp("taken_at")),
                toLocalDateTime(resultSet.getTimestamp("closed_at")),
                getNullableInteger(resultSet, "rating")
        );
    }

    private Integer getNullableInteger(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void setTimestamp(PreparedStatement statement, int index, LocalDateTime value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
