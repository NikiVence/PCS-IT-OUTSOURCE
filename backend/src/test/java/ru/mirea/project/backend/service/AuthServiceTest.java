package ru.mirea.project.backend.service;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.mirea.project.backend.domain.UserAccount;
import ru.mirea.project.backend.repository.UserAccountRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserAccountRepository repository = mock(UserAccountRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AuthService service = new AuthService(repository, encoder);

    @Test
    void acceptsCorrectPasswordAndRejectsWrongPassword() {
        UserAccount account = mock(UserAccount.class);
        when(account.isActive()).thenReturn(true);
        when(account.getPasswordHash()).thenReturn(encoder.encode("test-password"));
        when(repository.findByUsername("ilgiz")).thenReturn(Optional.of(account));

        assertTrue(service.credentialsAreValid("ilgiz", "test-password"));
        assertFalse(service.credentialsAreValid("ilgiz", "wrong-password"));
    }

    @Test
    void rejectsDisabledAccountEvenWithCorrectPassword() {
        UserAccount account = mock(UserAccount.class);
        when(account.isActive()).thenReturn(false);
        when(account.getPasswordHash()).thenReturn(encoder.encode("test-password"));
        when(repository.findByUsername("ilgiz")).thenReturn(Optional.of(account));

        assertFalse(service.credentialsAreValid("ilgiz", "test-password"));
    }

    @Test
    void rejectsUnknownUsername() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertFalse(service.credentialsAreValid("unknown", "test-password"));
    }
}