--liquibase formatted sql

--changeset yaroslav:init-dml-2
-- 1. Вставка пользователей
INSERT INTO users (username, password, role, first_name, last_name) VALUES
-- Администраторы (пароль: admin123 -> $2a$10$pGqlJTo1OXbmWzPpr0sgxOKD.8sT9wUVp.L1AqvUPRjmfssNYoooW)
('admin', '$2a$10$pGqlJTo1OXbmWzPpr0sgxOKD.8sT9wUVp.L1AqvUPRjmfssNYoooW', 'ADMIN', 'Иван', 'Петров'),
('admin2', '$2a$10$pGqlJTo1OXbmWzPpr0sgxOKD.8sT9wUVp.L1AqvUPRjmfssNYoooW', 'ADMIN', 'Ольга', 'Семенова'),

-- Сотрудники аэропорта (пароль: staff123 -> $2a$10$9ih0/Cd3Y93xzGkqvEVdZe2mTMka8NZEiYBd1h1m3UnkISv8SgIna)
('staff1', '$2a$10$9ih0/Cd3Y93xzGkqvEVdZe2mTMka8NZEiYBd1h1m3UnkISv8SgIna', 'AIRPORT_STAFF', 'Мария', 'Сидорова'),
('staff2', '$2a$10$9ih0/Cd3Y93xzGkqvEVdZe2mTMka8NZEiYBd1h1m3UnkISv8SgIna', 'AIRPORT_STAFF', 'Сергей', 'Васильев'),
('staff3', '$$2a$10$9ih0/Cd3Y93xzGkqvEVdZe2mTMka8NZEiYBd1h1m3UnkISv8SgIna', 'AIRPORT_STAFF', 'Анна', 'Кузнецова'),

-- Пограничники (пароль: border123 -> $2a$10$f6Cfb1wqiUm/6iV1k/6Bt.8BqzqmV1Zw/pupQHVVjQoZHvCfCCao2)
('border1', '$2a$10$f6Cfb1wqiUm/6iV1k/6Bt.8BqzqmV1Zw/pupQHVVjQoZHvCfCCao2', 'BORDER_GUARD', 'Алексей', 'Козлов'),
('border2', '$2a$10$f6Cfb1wqiUm/6iV1k/6Bt.8BqzqmV1Zw/pupQHVVjQoZHvCfCCao2', 'BORDER_GUARD', 'Дмитрий', 'Орлов'),
('border3', '$2a$10$f6Cfb1wqiUm/6iV1k/6Bt.8BqzqmV1Zw/pupQHVVjQoZHvCfCCao2', 'BORDER_GUARD', 'Наталья', 'Волкова'),

-- Таможенники (пароль: customs123 -> $2a$10$REu44wOXYaj/mZqZTkT7qu.jxB9Hp/G8oVTeCldvGwFFh38Z/We5O)
('customs1', '$2a$10$REu44wOXYaj/mZqZTkT7qu.jxB9Hp/G8oVTeCldvGwFFh38Z/We5O', 'CUSTOMS_OFFICER', 'Елена', 'Николаева'),
('customs2', '$2a$10$REu44wOXYaj/mZqZTkT7qu.jxB9Hp/G8oVTeCldvGwFFh38Z/We5O', 'CUSTOMS_OFFICER', 'Артем', 'Зайцев'),
('customs3', '$2a$10$REu44wOXYaj/mZqZTkT7qu.jxB9Hp/G8oVTeCldvGwFFh38Z/We5O', 'CUSTOMS_OFFICER', 'Ирина', 'Павлова'),

-- Пассажиры (пароль: pass123 -> $2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS)
('passenger1', '$2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS', 'PASSENGER', 'Анна', 'Смирнова'),
('passenger2', '$2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS', 'PASSENGER', 'Михаил', 'Попов'),
('passenger3', '$2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS', 'PASSENGER', 'Екатерина', 'Новикова'),
('passenger4', '$2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS', 'PASSENGER', 'Александр', 'Морозов'),
('passenger5', '$2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS', 'PASSENGER', 'Дарья', 'Васнецова'),
('passenger6', '$2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS', 'PASSENGER', 'Павел', 'Соколов'),
('passenger7', '$2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS', 'PASSENGER', 'Юлия', 'Лебедева'),
('passenger8', '$2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS', 'PASSENGER', 'Роман', 'Ковалев'),
('passenger9', '$2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS', 'PASSENGER', 'Светлана', 'Федорова'),
('passenger10', '$2a$10$A5iZR/zGii6K6iMexWylj.z1nYlszqKZSIC9AgqibGUWfVt3.9qBS', 'PASSENGER', 'Виктор', 'Медведев');

-- ... остальная часть DML скрипта остается без изменений
-- 2. Вставка пассажиров
INSERT INTO passengers (user_id, passport_number, phone, email, luggage_checked) VALUES
(11, 'AB123456', '+79161234567', 'anna.smirnova@mail.ru', false),
(12, 'CD789012', '+79162345678', 'mikhail.popov@mail.ru', true),
(13, 'EF345678', '+79163456789', 'ekaterina.novikova@mail.ru', false),
(14, 'GH901234', '+79164567890', 'alexander.morozov@mail.ru', true),
(15, 'IJ567890', '+79165678901', 'daria.vasnecova@mail.ru', false),
(16, 'KL123890', '+79166789012', 'pavel.sokolov@mail.ru', true),
(17, 'MN456701', '+79167890123', 'yulia.lebedeva@mail.ru', false),
(18, 'OP789012', '+79168901234', 'roman.kovalev@mail.ru', true),
(19, 'QR123413', '+79169012345', 'svetlana.fedorova@mail.ru', false),
(20, 'ST456714', '+79160123456', 'viktor.medvedev@mail.ru', true);

-- 3. Вставка рейсов
INSERT INTO flights (flight_number, departure_city, arrival_city, departure_time, arrival_time, total_seats, available_seats, status, created_by) VALUES
-- Рейсы на сегодня
('SU1001', 'Москва', 'Санкт-Петербург', '2024-12-20 08:00:00', '2024-12-20 09:30:00', 180, 175, 'SCHEDULED', 1),
('SU1002', 'Москва', 'Сочи', '2024-12-20 10:15:00', '2024-12-20 13:00:00', 200, 195, 'SCHEDULED', 1),
('SU1003', 'Санкт-Петербург', 'Москва', '2024-12-20 11:30:00', '2024-12-20 13:00:00', 180, 178, 'BOARDING', 1),
('SU1004', 'Москва', 'Екатеринбург', '2024-12-20 14:45:00', '2024-12-20 17:30:00', 160, 160, 'SCHEDULED', 1),

-- Рейсы на завтра
('SU1005', 'Москва', 'Новосибирск', '2024-12-21 16:20:00', '2024-12-21 20:45:00', 220, 220, 'SCHEDULED', 1),
('SU1006', 'Сочи', 'Москва', '2024-12-21 18:10:00', '2024-12-21 20:55:00', 200, 200, 'SCHEDULED', 1),
('SU1007', 'Москва', 'Калининград', '2024-12-21 07:30:00', '2024-12-21 09:15:00', 150, 150, 'SCHEDULED', 1),
('SU1008', 'Москва', 'Краснодар', '2024-12-21 09:45:00', '2024-12-21 12:00:00', 170, 170, 'SCHEDULED', 1),

-- Завершенные рейсы
('SU0901', 'Москва', 'Казань', '2024-12-19 12:00:00', '2024-12-19 13:30:00', 160, 0, 'ARRIVED', 1),
('SU0902', 'Санкт-Петербург', 'Минск', '2024-12-19 15:20:00', '2024-12-19 16:50:00', 140, 0, 'ARRIVED', 1);

-- 4. Вставка билетов
INSERT INTO tickets (flight_id, passenger_id, seat_number, price, ticket_number, status, booking_date) VALUES
-- Билеты на рейс SU1001 (Москва - СПб)
(1, 1, '15A', 4500.00, 'TK100001', 'BOOKED', '2024-12-15 10:30:00'),
(1, 2, '15B', 4500.00, 'TK100002', 'BOOKED', '2024-12-15 11:15:00'),
(1, 3, '16A', 4500.00, 'TK100003', 'CHECKED_IN', '2024-12-16 09:45:00'),
(1, 4, '16B', 4500.00, 'TK100004', 'BOARDED', '2024-12-16 14:20:00'),
(1, 5, '17A', 5200.00, 'TK100005', 'BOOKED', '2024-12-17 16:30:00'),

-- Билеты на рейс SU1002 (Москва - Сочи)
(2, 6, '8C', 7200.00, 'TK100006', 'BOOKED', '2024-12-14 12:45:00'),
(2, 7, '8D', 7200.00, 'TK100007', 'BOOKED', '2024-12-14 13:20:00'),
(2, 8, '9A', 6800.00, 'TK100008', 'CHECKED_IN', '2024-12-15 10:15:00'),
(2, 9, '9B', 6800.00, 'TK100009', 'BOOKED', '2024-12-16 11:30:00'),

-- Билеты на рейс SU1003 (СПб - Москва) - статус BOARDING
(3, 10, '12D', 4800.00, 'TK100010', 'CHECKED_IN', '2024-12-15 15:40:00'),
(3, 1, '12E', 4800.00, 'TK100011', 'BOARDED', '2024-12-16 08:25:00'),

-- Билеты на завершенные рейсы
(9, 2, '5A', 3800.00, 'TK090001', 'BOARDED', '2024-12-18 09:10:00'),
(9, 3, '5B', 3800.00, 'TK090002', 'BOARDED', '2024-12-18 10:45:00'),
(10, 4, '7C', 4200.00, 'TK090003', 'BOARDED', '2024-12-17 14:20:00');

-- 5. Вставка посадочных талонов
INSERT INTO boarding_passes (ticket_id, check_in_time, passport_verified, luggage_verified, boarded, verified_by_border_guard, verified_by_customs) VALUES
-- Для рейса SU1001
(3, '2024-12-20 06:30:00', true, true, false, 6, 9),
(4, '2024-12-20 06:45:00', true, true, true, 7, 10),

-- Для рейса SU1002
(8, '2024-12-20 08:15:00', true, false, false, 6, 9),

-- Для рейса SU1003
(10, '2024-12-20 09:50:00', true, true, false, 7, 10),
(11, '2024-12-20 10:05:00', true, true, true, 8, 9),

-- Для завершенных рейсов
(12, '2024-12-19 10:30:00', true, true, true, 6, 9),
(13, '2024-12-19 10:45:00', true, true, true, 7, 10),
(14, '2024-12-19 13:15:00', true, true, true, 8, 9);