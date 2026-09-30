package ru.mirea.project.repository;

import ru.mirea.project.exception.DatabaseException;
import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.ServiceRequest;
import ru.mirea.project.util.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class JdbcServiceRequestRepository implements ServiceRequestRepository {

    private final DatabaseManager databaseManager;

    public JdbcServiceRequestRepository() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    @Override
    public ServiceRequest create(ServiceRequest request) {
        String sql = "INSERT INTO requests (title, description, category, status, priority, " +
                "client_id, executor_id, taken_at, closed_at, rating) " +
                "VALUES (?, ?, ?::request_category, ?::request_status, ?::request_priority, " +
                "?, ?, ?, ?, ?) RETURNING *";

        try (var connection = databaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
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
        try (var connection = databaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
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
        return find("SELECT * FROM requests ORDER BY id");
    }

    @Override
    public List<ServiceRequest> searchByTitle(String query) {
        return find("SELECT * FROM requests " +
                "WHERE STRPOS(LOWER(title), LOWER(?)) > 0 ORDER BY id", query);
    }

    @Override
    public List<ServiceRequest> searchByDescription(String query) {
        return find("SELECT * FROM requests " +
                "WHERE STRPOS(LOWER(description), LOWER(?)) > 0 ORDER BY id", query);
    }

    @Override
    public List<ServiceRequest> findByStatus(RequestStatus status) {
        return find("SELECT * FROM requests WHERE status = ?::request_status ORDER BY id", status.name());
    }

    @Override
    public List<ServiceRequest> findByPriority(RequestPriority priority) {
        return find("SELECT * FROM requests WHERE priority = ?::request_priority ORDER BY id", priority.name());
    }

    @Override
    public List<ServiceRequest> findAllNewestFirst() {
        return find("SELECT * FROM requests ORDER BY created_at DESC, id");
    }

    @Override
    public List<ServiceRequest> findAllByPriorityDescending() {
        return find("SELECT * FROM requests ORDER BY priority DESC, id");
    }

    @Override
    public LinkedHashMap<String, Number> getStatistics() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM users) AS total_users,
                    COUNT(*) AS total_requests,
                    COUNT(*) FILTER (WHERE status = 'NEW') AS new_requests,
                    COUNT(*) FILTER (WHERE status IN ('IN_PROGRESS', 'WAITING')) AS active_requests,
                    COUNT(*) FILTER (WHERE status = 'CLOSED') AS closed_requests,
                    COUNT(*) FILTER (WHERE priority = 'HIGH') AS high_priority_requests,
                    COALESCE(AVG(rating) FILTER (WHERE status = 'CLOSED'), 0) AS average_rating
                FROM requests
                """;

        try (var connection = databaseManager.getConnection();
             var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {
            resultSet.next();
            LinkedHashMap<String, Number> statistics = new LinkedHashMap<>();
            statistics.put("Всего пользователей", resultSet.getLong("total_users"));
            statistics.put("Всего заявок", resultSet.getLong("total_requests"));
            statistics.put("Новых заявок", resultSet.getLong("new_requests"));
            statistics.put("Активных заявок", resultSet.getLong("active_requests"));
            statistics.put("Закрытых заявок", resultSet.getLong("closed_requests"));
            statistics.put("Заявок высокого приоритета", resultSet.getLong("high_priority_requests"));
            statistics.put("Средняя оценка", resultSet.getDouble("average_rating"));
            return statistics;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить статистику", e);
        }
    }

    @Override
    public boolean update(ServiceRequest request) {
        String sql = "UPDATE requests SET title = ?, description = ?, " +
                "category = ?::request_category, status = ?::request_status, " +
                "priority = ?::request_priority, client_id = ?, executor_id = ?, " +
                "taken_at = ?, closed_at = ?, rating = ? WHERE id = ?";

        try (var connection = databaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
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
        try (var connection = databaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось удалить заявку", e);
        }
    }

    private List<ServiceRequest> find(String sql) {
        return find(sql, null);
    }

    private List<ServiceRequest> find(String sql, String parameter) {
        List<ServiceRequest> requests = new ArrayList<>();
        try (var connection = databaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setString(1, parameter);
            }
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRow(resultSet));
                }
            }
            return requests;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить список заявок", e);
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
