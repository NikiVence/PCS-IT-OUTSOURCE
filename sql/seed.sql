-- ============================================================
-- IT-OUTSOURCE — тестовые данные (PostgreSQL / DataGrip)
-- ============================================================
-- ВАЖНО: этот файл выполняется в подключении к БД "it_outsource".
-- Сверху в DataGrip выбери data source "it_outsource", не "postgres".
-- ============================================================

-- ============================================================
-- Пользователи (6 штук: 1 админ, 3 клиента, 2 исполнителя)
-- Пароль у всех: "password123" (BCrypt-хэш)
-- ============================================================
INSERT INTO users (username, password_hash, full_name, email, role) VALUES
                                                                        ('admin',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Иванов Иван Иванович',     'admin@it-outsource.ru',     'ADMIN'),
                                                                        ('client1',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Петров Пётр Петрович',     'petrov@mail.ru',            'CLIENT'),
                                                                        ('client2',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Сидорова Анна Сергеевна',  'sidorova@mail.ru',          'CLIENT'),
                                                                        ('exec1',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Кузнецов Дмитрий Олегович','kuznetsov@it-outsource.ru', 'EXECUTOR'),
                                                                        ('exec2',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Смирнов Алексей Игоревич', 'smirnov@it-outsource.ru',   'EXECUTOR'),
                                                                        ('client3',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Морозова Елена Викторовна','morozova@mail.ru',          'CLIENT');

-- ============================================================
-- Заявки (12 штук) — разные статусы, категории, приоритеты
-- client_id: 2, 3, 6 — клиенты
-- executor_id: 4, 5 — исполнители
-- ============================================================
INSERT INTO requests
(title, description, category, status, priority, client_id, executor_id, created_at, taken_at, closed_at, rating)
VALUES
-- NEW — новые, ещё не в работе
('Не включается ноутбук',
 'После обновления Windows не загружается. Чёрный экран.',
 'HARDWARE', 'NEW', 'HIGH', 2, NULL,
 '2026-09-15 09:30:00', NULL, NULL, NULL),

('Не работает корпоративная почта',
 'Outlook не синхронизируется, ошибка 0x8004010F.',
 'SOFTWARE', 'NEW', 'HIGH', 3, NULL,
 '2026-09-16 11:15:00', NULL, NULL, NULL),

('Медленно работает интернет',
 'Скорость упала до 1 Мбит/с. Нужна диагностика.',
 'NETWORK', 'NEW', 'MEDIUM', 6, NULL,
 '2026-09-17 14:00:00', NULL, NULL, NULL),

-- IN_PROGRESS — в работе у исполнителей
('Установка 1С:Бухгалтерия',
 'Требуется установка и настройка на новом ПК.',
 'SOFTWARE', 'IN_PROGRESS', 'MEDIUM', 2, 4,
 '2026-09-10 10:00:00', '2026-09-10 12:00:00', NULL, NULL),

('Замена жёсткого диска',
 'HDD сыпется, нужна замена на SSD 512 ГБ.',
 'HARDWARE', 'IN_PROGRESS', 'HIGH', 3, 5,
 '2026-09-11 08:45:00', '2026-09-11 09:30:00', NULL, NULL),

('Настройка VPN для удалённой работы',
 'Сотрудник уходит на удалёнку, нужен VPN-доступ.',
 'NETWORK', 'IN_PROGRESS', 'MEDIUM', 6, 4,
 '2026-09-12 13:20:00', '2026-09-12 15:00:00', NULL, NULL),

-- WAITING — ожидают ответа клиента/поставщика
('Замена картриджа в принтере',
 'Принтер HP LaserJet, закончился тонер.',
 'HARDWARE', 'WAITING', 'LOW', 2, 5,
 '2026-09-13 16:00:00', '2026-09-13 17:00:00', NULL, NULL),

('Настройка резервного копирования',
 'Нужно настроить ежедневный бэкап на сетевой диск.',
 'SOFTWARE', 'WAITING', 'MEDIUM', 3, 4,
 '2026-09-14 09:00:00', '2026-09-14 10:30:00', NULL, NULL),

-- RESOLVED — решены, но ещё не закрыты
('Восстановление пароля от домена',
 'Забыл пароль, нужна помощь с восстановлением.',
 'SOFTWARE', 'RESOLVED', 'LOW', 6, 5,
 '2026-09-08 11:00:00', '2026-09-08 11:30:00', NULL, NULL),

('Настройка Wi-Fi в переговорной',
 'Не ловит сигнал, нужна точка доступа.',
 'NETWORK', 'RESOLVED', 'MEDIUM', 2, 4,
 '2026-09-09 15:00:00', '2026-09-09 16:00:00', NULL, NULL),

-- CLOSED — закрыты с оценками
('Замена клавиатуры',
 'Залипают клавиши, замена на новую.',
 'HARDWARE', 'CLOSED', 'LOW', 3, 5,
 '2026-09-01 10:00:00', '2026-09-01 11:00:00', '2026-09-01 18:00:00', 5),

('Консультация по Microsoft Office',
 'Помощь с формулами в Excel.',
 'CONSULTATION', 'CLOSED', 'LOW', 6, 4,
 '2026-09-02 14:00:00', '2026-09-02 14:30:00', '2026-09-02 15:00:00', 4);