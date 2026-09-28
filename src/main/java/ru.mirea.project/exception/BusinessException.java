package ru.mirea.project.exception;

/**
 * Бросается при нарушении бизнес-правил системы.
 *
 * Примеры:
 * <ul>
 *     <li>попытка выполнить запрещённый переход статуса заявки</li>
 *     <li>REQ-025: клиент пытается редактировать заявку в статусе ≠ NEW</li>
 *     <li>REQ-027: попытка оценить заявку до её закрытия</li>
 *     <li>Попытка назначить исполнителем пользователя с ролью ≠ EXECUTOR</li>
 * </ul>
 *
 * Checked-исключение — Service-слой объявляет его в throws,
 * UI-слой ловит и показывает пользователю понятное сообщение.
 */
public class BusinessException extends Exception {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
