package ru.mirea.project.repository;

import ru.mirea.project.exception.DatabaseException;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository implements CrudRepository<User> {

    private final DatabaseManager databaseManager;

    public UserRepository() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (username, full_name, email, role) " +
                "VALUES (?, ?, ?, ?::user_role) RETURNING *";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getFullName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getRole().name());

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return mapRow(resultSet);
            }
        } catch (SQLException e) {
            throw translate(e, "Не удалось создать пользователя");
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить пользователя по ID", e);
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить пользователя по username", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить список пользователей", e);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, full_name = ?, " +
                "email = ?, role = ?::user_role WHERE id = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getFullName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getRole().name());
            statement.setObject(5, user.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw translate(e, "Не удалось обновить пользователя");
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw translate(e, "Не удалось удалить пользователя");
        }
    }

    /**
     * Переводит ошибки ограничений PostgreSQL в понятные пользователю сообщения.
     * Коды SQLState: 23505 — нарушение UNIQUE, 23503 — нарушение внешнего ключа,
     * 23514 — нарушение CHECK.
     */
    private DatabaseException translate(SQLException e, String defaultMessage) {
        String state = e.getSQLState();
        String details = e.getMessage() == null ? "" : e.getMessage();

        if ("23505".equals(state) && details.contains("users_username_key")) {
            return new DatabaseException("Пользователь с таким логином уже существует", e);
        }
        if ("23505".equals(state) && details.contains("users_email_key")) {
            return new DatabaseException("Пользователь с таким email уже существует", e);
        }
        if ("23503".equals(state)) {
            return new DatabaseException(
                    "Нельзя удалить пользователя: он является клиентом в существующих заявках", e);
        }
        if ("23514".equals(state) && details.contains("chk_users_username_len")) {
            return new DatabaseException("Логин должен содержать не менее 3 символов", e);
        }
        return new DatabaseException(defaultMessage, e);
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                UserRole.valueOf(resultSet.getString("role")),
                createdAt == null ? null : createdAt.toLocalDateTime()
        );
    }
}
