package ru.mirea.project.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    @Column(name = "full_name")
    private String fullName;

    private String email;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", columnDefinition = "user_role")
    private UserRole role;

    @Column(name = "password_hash")
    private String passwordHash;

    private boolean active;

    @Column(name = "must_change_password")
    private boolean mustChangePassword;

    protected UserAccount() {
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public static UserAccount createAdmin(String username, String fullName,
                                          String email, String passwordHash) {
        UserAccount account = new UserAccount();
        account.username = username;
        account.fullName = fullName;
        account.email = email;
        account.passwordHash = passwordHash;
        account.role = UserRole.ADMIN;
        account.active = true;
        account.mustChangePassword = false;
        return account;
    }
}