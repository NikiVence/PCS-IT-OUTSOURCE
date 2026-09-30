# ER-диаграмма

Диаграмма соответствует [sql/schema.sql](../sql/schema.sql). Mermaid отображается в GitHub и совместимых редакторах Markdown.

```mermaid
erDiagram
    users ||--o{ requests : "client_id: создаёт"
    users |o--o{ requests : "executor_id: исполняет"

    users {
        SERIAL id PK
        VARCHAR(50) username UK "NOT NULL, минимум 3 символа"
        VARCHAR(100) full_name "NOT NULL"
        VARCHAR(100) email UK "NOT NULL"
        user_role role "NOT NULL"
        TIMESTAMP created_at "NOT NULL, DEFAULT CURRENT_TIMESTAMP"
    }
    requests {
        SERIAL id PK
        VARCHAR(150) title "NOT NULL, минимум 3 символа"
        TEXT description "NULL допускается в БД"
        request_category category "NOT NULL"
        request_status status "NOT NULL, DEFAULT NEW"
        request_priority priority "NOT NULL, DEFAULT MEDIUM"
        INT client_id FK "NOT NULL"
        INT executor_id FK "NULL допускается"
        TIMESTAMP created_at "NOT NULL, DEFAULT CURRENT_TIMESTAMP"
        TIMESTAMP taken_at "NULL допускается"
        TIMESTAMP closed_at "NULL допускается"
        INT rating "NULL или 1..5"
    }
```

У каждой заявки ровно один клиент и ноль или один исполнитель. У пользователя может быть любое количество заявок в каждой из этих ролей. Роли CLIENT/EXECUTOR для соответствующих операций проверяет Java-сервис.

| Связь | При удалении пользователя | При изменении ID |
|---|---|---|
| `requests.client_id → users.id` | RESTRICT: клиент с заявками не удаляется | CASCADE |
| `requests.executor_id → users.id` | SET NULL: заявка остаётся без исполнителя | CASCADE |

| PostgreSQL ENUM | Значения |
|---|---|
| `user_role` | CLIENT, EXECUTOR, ADMIN |
| `request_category` | HARDWARE, SOFTWARE, NETWORK, CONSULTATION |
| `request_status` | NEW, IN_PROGRESS, WAITING, RESOLVED, CLOSED |
| `request_priority` | LOW, MEDIUM, HIGH |

Индексы заявок: `status`, `category`, `priority`, `client_id`, `executor_id`, `created_at`. Первичные и уникальные ключи также индексируются PostgreSQL.
