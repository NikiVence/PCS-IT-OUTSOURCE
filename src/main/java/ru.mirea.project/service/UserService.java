package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;

    public UserService() {
        this(new UserRepository());
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String fullName,
                           String email, UserRole role) throws BusinessException {
        validateUser(username, fullName, email, role);

        User user = new User(username.trim(), fullName.trim(), email.trim(), role);
        return userRepository.create(user);
    }

    public User getUserById(Integer id) throws EntityNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Пользователь с ID=" + id + " не найден"));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Integer id, String username, String fullName,
                           String email, UserRole role)
            throws EntityNotFoundException, BusinessException {
        validateUser(username, fullName, email, role);

        User user = getUserById(id);
        user.setUsername(username.trim());
        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        user.setRole(role);

        if (!userRepository.update(user)) {
            throw new EntityNotFoundException("Пользователь с ID=" + id + " не найден");
        }
        return user;
    }

    public void deleteUser(Integer id) throws EntityNotFoundException {
        if (!userRepository.deleteById(id)) {
            throw new EntityNotFoundException("Пользователь с ID=" + id + " не найден");
        }
    }

    private void validateUser(String username, String fullName,
                              String email, UserRole role) throws BusinessException {
        requireText(username, "Логин");
        requireText(fullName, "ФИО");
        requireText(email, "Email");
        requireLength(username, "Логин", 3, 50);
        requireLength(fullName, "ФИО", 2, 100);
        requireLength(email, "Email", 5, 100);
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BusinessException("Email указан в неверном формате (пример: user@mail.ru)");
        }
        if (role == null) {
            throw new BusinessException("Роль пользователя обязательна");
        }
    }

    private void requireLength(String value, String fieldName, int min, int max)
            throws BusinessException {
        int length = value.trim().length();
        if (length < min || length > max) {
            throw new BusinessException(
                    "Поле «" + fieldName + "» должно содержать от " + min + " до " + max + " символов");
        }
    }

    private void requireText(String value, String fieldName) throws BusinessException {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Поле «" + fieldName + "» не должно быть пустым");
        }
    }
}
