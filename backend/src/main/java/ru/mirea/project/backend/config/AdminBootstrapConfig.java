package ru.mirea.project.backend.config;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.mirea.project.backend.domain.UserAccount;
import ru.mirea.project.backend.repository.UserAccountRepository;

@Configuration
@ConditionalOnProperty(name = "app.bootstrap.admin.enabled", havingValue = "true")
public class AdminBootstrapConfig {

    @Bean
    public ApplicationRunner createFirstAdmin(
            UserAccountRepository accounts,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.password}") String temporaryPassword) {

        return args -> {
            String username = "admin";
            if (accounts.findByUsername(username).isPresent()) {
                return;
            }

            if (temporaryPassword.isBlank()
                    || temporaryPassword.length() < 12
                    || temporaryPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalArgumentException(
                        "Временный пароль должен содержать минимум 12 символов "
                                + "и занимать не более 72 байт в UTF-8");
            }

            UserAccount admin = UserAccount.createAdmin(
                    username,
                    "Администратор IT Outsource",
                    "admin@example.test",
                    passwordEncoder.encode(temporaryPassword)
            );

            accounts.save(admin);
        };
    }
}