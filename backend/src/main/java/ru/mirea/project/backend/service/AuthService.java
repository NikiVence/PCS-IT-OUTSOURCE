package ru.mirea.project.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mirea.project.backend.domain.UserAccount;
import ru.mirea.project.backend.repository.UserAccountRepository;

@Service
public class AuthService {

    private final UserAccountRepository userAccounts;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userAccounts,
                       PasswordEncoder passwordEncoder) {
        this.userAccounts = userAccounts;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean credentialsAreValid(String username, String password) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return false;
        }

        UserAccount account = userAccounts.findByUsername(username).orElse(null);
        if (account == null || !account.isActive()) {
            return false;
        }

        String hash = account.getPasswordHash();
        return hash != null && passwordEncoder.matches(password, hash);
    }
}