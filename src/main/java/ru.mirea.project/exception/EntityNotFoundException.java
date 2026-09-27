package ru.mirea.project.exception;

/**
 * Бросается, когда запись с указанным ID не найдена в БД.
 *
 * Checked-исключение — вызывающий код обязан его обработать.
 *
 * Пример использования:
 * <pre>
 *     User user = userRepository.findById(999)
 *             .orElseThrow(() -> new EntityNotFoundException("User", 999));
 * </pre>
 */
public class EntityNotFoundException extends Exception {

    private final String entityName;
    private final Object entityId;

    /**
     * @param entityName название сущности: "User", "ServiceRequest"
     * @param entityId   ID, по которому искали
     */
    public EntityNotFoundException(String entityName, Object entityId) {
        super(String.format("%s с ID=%s не найден", entityName, entityId));
        this.entityName = entityName;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.entityName = null;
        this.entityId = null;
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getEntityId() {
        return entityId;
    }
}