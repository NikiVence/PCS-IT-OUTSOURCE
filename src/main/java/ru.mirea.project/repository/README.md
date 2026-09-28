# Репозиторий

Слой доступа к PostgreSQL через JDBC. SQL находится здесь, а бизнес-правила — в сервисах.

- `CrudRepository<T>` — интерфейс создания, поиска по ID, чтения списка, обновления и удаления.
- `UserRepository implements CrudRepository<User>` — работа с `users`, дополнительно поиск по логину.
- `ServiceRequestRepository implements CrudRepository<ServiceRequest>` — работа с `requests`, дополнительно выборки по клиенту и исполнителю.

`ServiceRequestService` принимает зависимости типа `CrudRepository`, поэтому использует реализации полиморфно. В тестах JDBC можно заменить хранилищем в памяти.

Соединения выдаёт `DatabaseManager`. Запросы используют `PreparedStatement`, параметры передаются через `?`; соединения, запросы и результаты закрываются try-with-resources. Ошибки SQL оборачиваются в `DatabaseException`, отсутствие записи передаётся как `Optional.empty()` или `false`.

Инструкция запуска находится в [основном README](../../../../../README.md), структура таблиц — в [ER-диаграмме](../../../../../docs/er-diagram.md).
