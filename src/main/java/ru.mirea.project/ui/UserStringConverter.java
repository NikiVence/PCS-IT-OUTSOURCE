package ru.mirea.project.ui;

import javafx.util.StringConverter;
import ru.mirea.project.model.User;

public class UserStringConverter extends StringConverter<User> {

    @Override
    public String toString(User user) {
        if (user == null) {
            return "";
        }
        return user.getFullName() + " (" + user.getUsername() + ")";
    }

    @Override
    public User fromString(String value) {
        return null;
    }
}
