CREATE DATABASE IF NOT EXISTS ocean_view_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ocean_view_db;

-- ============================================================
-- TABLES
-- ============================================================

CREATE TABLE users (
  id           VARCHAR(36)   PRIMARY KEY,
  fname        VARCHAR(100)  NOT NULL,
  lname        VARCHAR(100)  NOT NULL,
  username     VARCHAR(50)   UNIQUE NOT NULL,
  password     VARCHAR(255)  NOT NULL,
  role         ENUM('ADMIN','MANAGER','STAFF') NOT NULL,
  email        VARCHAR(100)  UNIQUE NOT NULL,
  phone        VARCHAR(20),
  is_active    TINYINT(1)    DEFAULT 1,
  last_login   TIMESTAMP     NULL,
  created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE guests (
  id           VARCHAR(36)   PRIMARY KEY,
  name         VARCHAR(200)  NOT NULL,
  address      TEXT,
  contact      VARCHAR(20)   NOT NULL,
  email        VARCHAR(100)  NOT NULL,
  nic          VARCHAR(20)   UNIQUE,
  loyalty_pts  INT           DEFAULT 0,
  created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rooms (
  id              VARCHAR(36)    PRIMARY KEY,
  room_number     VARCHAR(10)    UNIQUE NOT NULL,
  room_type       ENUM('STANDARD','DELUXE','SUITE','PENTHOUSE') NOT NULL,
  floor           INT            NOT NULL,
  capacity        INT            NOT NULL DEFAULT 2,
  rate_per_night  DECIMAL(10,2)  NOT NULL,
  is_available    TINYINT(1)     DEFAULT 1,
  description     TEXT,
  amenities       JSON,
  image_url       VARCHAR(255)
);

CREATE TABLE reservations (
  id              VARCHAR(36)    PRIMARY KEY,
  reservation_no  VARCHAR(20)    UNIQUE NOT NULL,
  guest_id        VARCHAR(36)    NOT NULL,
  room_id         VARCHAR(36)    NOT NULL,
  check_in_date   DATE           NOT NULL,
  check_out_date  DATE           NOT NULL,
  num_guests      INT            NOT NULL DEFAULT 1,
  status          ENUM('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED') DEFAULT 'PENDING',
  total_amount    DECIMAL(10,2),
  special_requests TEXT,
  created_by      VARCHAR(36)    NOT NULL,
  updated_at      TIMESTAMP      NULL ON UPDATE CURRENT_TIMESTAMP,
  created_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (guest_id)   REFERENCES guests(id),
  FOREIGN KEY (room_id)    REFERENCES rooms(id),
  FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE payments (
  id              VARCHAR(36)    PRIMARY KEY,
  reservation_id  VARCHAR(36)    NOT NULL UNIQUE,
  amount          DECIMAL(10,2)  NOT NULL,
  payment_method  ENUM('CASH','CARD') NOT NULL,
  reference_no    VARCHAR(100),
  payment_date    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
  status          ENUM('PENDING','COMPLETED','FAILED','REFUNDED') DEFAULT 'PENDING',
  processed_by    VARCHAR(36)    NOT NULL,
  notes           TEXT,
  FOREIGN KEY (reservation_id) REFERENCES reservations(id),
  FOREIGN KEY (processed_by)   REFERENCES users(id)
);

CREATE TABLE audit_log (
  id           VARCHAR(36)   PRIMARY KEY,
  user_id      VARCHAR(36),
  action       VARCHAR(100)  NOT NULL,
  entity       VARCHAR(50),
  entity_id    VARCHAR(36),
  details      TEXT,
  ip_address   VARCHAR(45),
  created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- STORED PROCEDURES
-- ============================================================

DELIMITER $$

CREATE PROCEDURE sp_calculate_bill(IN p_reservation_id VARCHAR(36), OUT p_total DECIMAL(10,2))
BEGIN
  SELECT DATEDIFF(r.check_out_date, r.check_in_date) * rm.rate_per_night
  INTO p_total
  FROM reservations r
  JOIN rooms rm ON r.room_id = rm.id
  WHERE r.id = p_reservation_id;
END$$

CREATE PROCEDURE sp_monthly_report(IN p_year INT, IN p_month INT)
BEGIN
  SELECT
    COUNT(r.id)                                                          AS total_reservations,
    COALESCE(SUM(p.amount), 0)                                          AS total_revenue,
    COALESCE(AVG(p.amount), 0)                                          AS avg_revenue,
    COUNT(CASE WHEN r.status = 'CANCELLED' THEN 1 END)                  AS cancellations,
    COUNT(CASE WHEN p.payment_method = 'CARD' THEN 1 END)               AS card_payments,
    COUNT(CASE WHEN p.payment_method = 'CASH' THEN 1 END)               AS cash_payments
  FROM reservations r
  LEFT JOIN payments p ON r.id = p.reservation_id
  WHERE YEAR(r.check_in_date) = p_year AND MONTH(r.check_in_date) = p_month;
END$$

CREATE PROCEDURE sp_weekly_report(IN p_week_start DATE)
BEGIN
  SELECT
    DATE(r.check_in_date)   AS day,
    COUNT(r.id)             AS reservations,
    COALESCE(SUM(p.amount), 0) AS revenue,
    rm.room_type,
    COUNT(rm.id)            AS rooms_occupied
  FROM reservations r
  LEFT JOIN payments p ON r.id = p.reservation_id
  JOIN rooms rm ON r.room_id = rm.id
  WHERE r.check_in_date BETWEEN p_week_start AND DATE_ADD(p_week_start, INTERVAL 6 DAY)
    AND r.status NOT IN ('CANCELLED')
  GROUP BY DATE(r.check_in_date), rm.room_type
  ORDER BY day;
END$$

CREATE PROCEDURE sp_check_in(IN p_reservation_id VARCHAR(36), IN p_staff_id VARCHAR(36))
BEGIN
  DECLARE v_room_id VARCHAR(36);
  START TRANSACTION;
    SELECT room_id INTO v_room_id FROM reservations WHERE id = p_reservation_id;
    UPDATE reservations SET status = 'CHECKED_IN'  WHERE id = p_reservation_id;
    UPDATE rooms         SET is_available = 0      WHERE id = v_room_id;
    INSERT INTO audit_log (id, user_id, action, entity, entity_id)
    VALUES (UUID(), p_staff_id, 'CHECK_IN', 'reservation', p_reservation_id);
  COMMIT;
END$$

CREATE PROCEDURE sp_check_out(IN p_reservation_id VARCHAR(36), IN p_staff_id VARCHAR(36))
BEGIN
  DECLARE v_room_id VARCHAR(36);
  START TRANSACTION;
    SELECT room_id INTO v_room_id FROM reservations WHERE id = p_reservation_id;
    UPDATE reservations SET status = 'CHECKED_OUT' WHERE id = p_reservation_id;
    UPDATE rooms         SET is_available = 1      WHERE id = v_room_id;
    INSERT INTO audit_log (id, user_id, action, entity, entity_id)
    VALUES (UUID(), p_staff_id, 'CHECK_OUT', 'reservation', p_reservation_id);
  COMMIT;
END$$

DELIMITER ;

-- ============================================================
-- TRIGGERS
-- ============================================================

DELIMITER $$

-- Auto-generate reservation_no: RES-YYYY-NNNN
CREATE TRIGGER trg_reservation_no
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
  DECLARE v_count INT;
  SELECT COUNT(*) + 1 INTO v_count FROM reservations WHERE YEAR(created_at) = YEAR(NOW());
  SET NEW.reservation_no = CONCAT('RES-', YEAR(NOW()), '-', LPAD(v_count, 4, '0'));
END$$

-- Auto-calculate total_amount from room rate and stay duration
CREATE TRIGGER trg_calc_total
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
  DECLARE v_rate DECIMAL(10,2);
  SELECT rate_per_night INTO v_rate FROM rooms WHERE id = NEW.room_id;
  SET NEW.total_amount = DATEDIFF(NEW.check_out_date, NEW.check_in_date) * v_rate;
END$$

-- Block overlapping reservations at the database level
CREATE TRIGGER trg_prevent_double_booking
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
  DECLARE v_count INT;
  SELECT COUNT(*) INTO v_count FROM reservations
  WHERE room_id = NEW.room_id
    AND status NOT IN ('CANCELLED', 'CHECKED_OUT')
    AND check_in_date  < NEW.check_out_date
    AND check_out_date > NEW.check_in_date;
  IF v_count > 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Room already booked for these dates';
  END IF;
END$$

-- Free room automatically when reservation is cancelled mid-stay
CREATE TRIGGER trg_cancel_frees_room
AFTER UPDATE ON reservations
FOR EACH ROW
BEGIN
  IF NEW.status = 'CANCELLED' AND OLD.status = 'CHECKED_IN' THEN
    UPDATE rooms SET is_available = 1 WHERE id = NEW.room_id;
  END IF;
END$$

DELIMITER ;

-- ============================================================
-- VIEWS
-- ============================================================

CREATE VIEW vw_reservation_summary AS
SELECT
  r.id, r.reservation_no, r.status,
  g.name            AS guest_name,
  g.email           AS guest_email,
  g.contact,
  rm.room_number,
  rm.room_type,
  rm.rate_per_night,
  r.check_in_date,
  r.check_out_date,
  DATEDIFF(r.check_out_date, r.check_in_date) AS nights,
  r.total_amount,
  p.payment_method,
  p.reference_no,
  p.status          AS payment_status,
  u.fname           AS staff_fname
FROM reservations r
JOIN  guests  g  ON r.guest_id   = g.id
JOIN  rooms   rm ON r.room_id    = rm.id
LEFT JOIN payments p ON r.id     = p.reservation_id
JOIN  users   u  ON r.created_by = u.id;

CREATE VIEW vw_room_occupancy AS
SELECT
  rm.room_type,
  COUNT(rm.id)                                                                         AS total_rooms,
  SUM(CASE WHEN rm.is_available = 0 THEN 1 ELSE 0 END)                                AS occupied,
  SUM(CASE WHEN rm.is_available = 1 THEN 1 ELSE 0 END)                                AS available,
  ROUND(SUM(CASE WHEN rm.is_available=0 THEN 1 ELSE 0 END) * 100.0 / COUNT(rm.id),1) AS occupancy_pct
FROM rooms rm
GROUP BY rm.room_type;

