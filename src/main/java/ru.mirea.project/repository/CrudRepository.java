package ru.mirea.project.repository;

import java.util.List;
import java.util.Optional;

/** Общий контракт хранилища; сервисы не зависят от способа хранения данных. */
public interface CrudRepository<T> {
    T create(T entity);
    Optional<T> findById(Integer id);
    List<T> findAll();
    boolean update(T entity);
    boolean deleteById(Integer id);
}
