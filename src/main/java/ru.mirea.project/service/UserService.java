package ru.mirea.project.service;

import org.mindrot.jbcrypt.BCrypt;
import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this(new UserRepository());
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String password, String fullName,
                           String email, UserRole role) throws BusinessException {
        requireText(username, "Имя пользователя");
        requireText(password, "Пароль");
        requireText(fullName, "ФИО");
        requireText(email, "Email");
        if (role == null) {
            throw new BusinessException("Роль пользователя обязательна");
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(username.trim(), passwordHash, fullName.trim(), email.trim(), role);
        return userRepository.create(user);
    }

    public User getUserById(Integer id) throws EntityNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
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
        requireText(username, "Имя пользователя");
        requireText(fullName, "ФИО");
        requireText(email, "Email");
        if (role == null) {
            throw new BusinessException("Роль пользователя обязательна");
        }

        User user = getUserById(id);
        user.setUsername(username.trim());
        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        user.setRole(role);

        if (!userRepository.update(user)) {
            throw new EntityNotFoundException("User", id);
        }
        return user;
    }

    public void deleteUser(Integer id) throws EntityNotFoundException {
        getUserById(id);
        if (!userRepository.deleteById(id)) {
            throw new EntityNotFoundException("User", id);
        }
    }

    private void requireText(String value, String fieldName) throws BusinessException {
        if (value == null || value.isBlank()) {
            throw new BusinessException(fieldName + " не должно быть пустым");
        }
    }
}