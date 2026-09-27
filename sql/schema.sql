-- ============================================================
-- IT-OUTSOURCE — схема БД (PostgreSQL)
-- Выполнять В ПОДКЛЮЧЕНИИ к БД it_outsource (не postgres!)
-- Создаёт enum-типы, таблицы, индексы.
-- ============================================================

-- ============================================================
-- 1. ENUM-типы
-- ============================================================

CREATE TYPE user_role AS ENUM ('CLIENT', 'EXECUTOR', 'ADMIN');

CREATE TYPE request_category AS ENUM ('HARDWARE', 'SOFTWARE', 'NETWORK', 'CONSULTATION');

CREATE TYPE request_status AS ENUM ('NEW', 'IN_PROGRESS', 'WAITING', 'RESOLVED', 'CLOSED');

CREATE TYPE request_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH');

-- ============================================================
-- 2. Таблица users
-- ============================================================
CREATE TABLE users (
                       id            SERIAL PRIMARY KEY,
                       username      VARCHAR(50)  NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name     VARCHAR(100) NOT NULL,
                       email         VARCHAR(100) NOT NULL UNIQUE,
                       role          user_role    NOT NULL,
                       created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT chk_users_username_len CHECK (CHAR_LENGTH(username) >= 3)
);

-- ============================================================
-- 3. Таблица requests
-- ============================================================
CREATE TABLE requests (
                          id          SERIAL PRIMARY KEY,
                          title       VARCHAR(150) NOT NULL,
                          description TEXT,
                          category    request_category NOT NULL,
                          status      request_status   NOT NULL DEFAULT 'NEW',
                          priority    request_priority NOT NULL DEFAULT 'MEDIUM',

                          client_id   INT NOT NULL,
                          executor_id INT NULL,

                          created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          taken_at    TIMESTAMP NULL,
                          closed_at   TIMESTAMP NULL,
                          rating      INT NULL,

                          CONSTRAINT fk_requests_client
                              FOREIGN KEY (client_id) REFERENCES users(id)
                                  ON DELETE RESTRICT ON UPDATE CASCADE,

                          CONSTRAINT fk_requests_executor
                              FOREIGN KEY (executor_id) REFERENCES users(id)
                                  ON DELETE SET NULL ON UPDATE CASCADE,

                          CONSTRAINT chk_requests_rating CHECK (rating IS NULL OR (rating BETWEEN 1 AND 5)),

                          CONSTRAINT chk_requests_title CHECK (CHAR_LENGTH(title) >= 3)
);

-- ============================================================
-- 4. Индексы
-- ============================================================
CREATE INDEX idx_requests_status     ON requests(status);
CREATE INDEX idx_requests_category   ON requests(category);
CREATE INDEX idx_requests_priority   ON requests(priority);
CREATE INDEX idx_requests_client     ON requests(client_id);
CREATE INDEX idx_requests_executor   ON requests(executor_id);
CREATE INDEX idx_requests_created_at ON requests(created_at);



