package ru.mirea.project.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Пользователь системы IT-OUTSOURCE.
 * Соответствует строке таблицы {@code users} в PostgreSQL.
 *
 * Роли: {@link UserRole#CLIENT}, {@link UserRole#EXECUTOR}, {@link UserRole#ADMIN}.
 */
public class User {

    // ============================================================
    // Поля — соответствуют колонкам таблицы users в БД
    // ============================================================
    private Integer id;                     // SERIAL PRIMARY KEY, может быть null до сохранения
    private String username;                // VARCHAR(50) UNIQUE NOT NULL
    private String passwordHash;            // VARCHAR(255) NOT NULL (BCrypt-хэш)
    private String fullName;                // VARCHAR(100) NOT NULL
    private String email;                   // VARCHAR(100) UNIQUE NOT NULL
    private UserRole role;                  // ENUM user_role NOT NULL
    private LocalDateTime createdAt;        // TIMESTAMP NOT NULL DEFAULT NOW()

    // ============================================================
    // Конструкторы
    // ============================================================

    /**
     * Пустой конструктор — нужен для случаев, когда объект
     * заполняется через сеттеры (например, при чтении из БД в Repository).
     */
    public User() {
    }

    /**
     * Полный конструктор — для создания нового объекта с данными сразу.
     * id не передаём: он появится только после INSERT в БД.
     */
    public User(String username, String passwordHash, String fullName,
                String email, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    /**
     * Конструктор со всеми полями — используется в Repository
     * при чтении строки из ResultSet (id и createdAt уже есть).
     */
    public User(Integer id, String username, String passwordHash, String fullName,
                String email, UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    // ============================================================
    // Геттеры и сеттеры — доступ к private-полям
    // ============================================================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ============================================================
    // equals, hashCode, toString
    // ============================================================

    /**
     * Сравнение пользователей по id.
     * Если id null (объект ещё не сохранён в БД) — используется ссылочное сравнение.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Красивый вывод для консоли и логов.
     * Пароль НЕ выводим — даже хэш.
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}