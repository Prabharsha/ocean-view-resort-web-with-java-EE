USE ocean_view_db;

-- ============================================================
-- SEED DATA — Ocean View Resort Demo
-- Current date context: 2026-03-04
--
-- Passwords (BCrypt cost=10):
--   admin    → Admin@123
--   manager  → Manager@123
--   staff1   → Staff@123
-- ============================================================

-- Disable trigger side-effects during bulk insert so we can set
-- reservation_no and total_amount explicitly.
SET @OLD_SQL_MODE = @@sql_mode;
SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION';

-- ============================================================
-- 1. USERS
-- ============================================================
INSERT INTO users (id, fname, lname, username, password, role, email, phone, is_active, last_login) VALUES
  ('user-admin-001', 'System',    'Admin',    'admin',    '$2a$10$LJ3m4ys3uz0b5V7CUOOSbOYWCJ0gE7p8VfTuJzAQrMTqe5dXvOXci', 'ADMIN',   'admin@oceanview.lk',    '+94 77 100 0001', 1, '2026-03-04 08:00:00'),
  ('user-mgr-001',   'Samantha',  'Perera',   'manager',  '$2a$10$LJ3m4ys3uz0b5V7CUOOSbOYWCJ0gE7p8VfTuJzAQrMTqe5dXvOXci', 'MANAGER', 'manager@oceanview.lk',  '+94 77 100 0002', 1, '2026-03-04 08:30:00'),
  ('user-staff-001', 'Kasun',     'Silva',    'staff1',   '$2a$10$LJ3m4ys3uz0b5V7CUOOSbOYWCJ0gE7p8VfTuJzAQrMTqe5dXvOXci', 'STAFF',   'staff1@oceanview.lk',   '+94 77 100 0003', 1, '2026-03-04 09:00:00'),
  ('user-staff-002', 'Nadeesha',  'Fernando', 'staff2',   '$2a$10$LJ3m4ys3uz0b5V7CUOOSbOYWCJ0gE7p8VfTuJzAQrMTqe5dXvOXci', 'STAFF',   'staff2@oceanview.lk',   '+94 77 100 0004', 1, '2026-03-03 14:00:00'),
  ('user-staff-003', 'Dilshan',   'Rajapaksa','staff3',   '$2a$10$LJ3m4ys3uz0b5V7CUOOSbOYWCJ0gE7p8VfTuJzAQrMTqe5dXvOXci', 'STAFF',   'staff3@oceanview.lk',   '+94 77 100 0005', 1, '2026-03-02 10:00:00');

-- ============================================================
-- 2. ROOMS  (6 rooms across 4 types)
--    is_available reflects who is currently CHECKED_IN
--    rooms occupied: 102 (res-006 CHECKED_IN), 201 (res-010 CHECKED_IN)
-- ============================================================
INSERT INTO rooms (id, room_number, room_type, floor, capacity, rate_per_night, is_available, description, amenities) VALUES
  ('room-001', '101', 'STANDARD',  1, 2,  8500.00, 1, 'Cozy garden view room with twin beds',
      '["WiFi","AC","TV","Hot Water"]'),
  ('room-002', '102', 'STANDARD',  1, 2,  8500.00, 0, 'Cozy garden view room with double bed',
      '["WiFi","AC","TV","Hot Water"]'),
  ('room-003', '201', 'DELUXE',    2, 3, 14000.00, 0, 'Spacious room with partial sea view and balcony',
      '["WiFi","AC","TV","Mini Bar","Balcony","Hot Water"]'),
  ('room-004', '202', 'DELUXE',    2, 3, 14000.00, 1, 'Spacious room with partial sea view',
      '["WiFi","AC","TV","Mini Bar","Hot Water"]'),
  ('room-005', '301', 'SUITE',     3, 4, 22000.00, 1, 'Full sea view suite with separate living area',
      '["WiFi","AC","TV","Mini Bar","Balcony","Jacuzzi","Hot Water","Room Service"]'),
  ('room-006', '401', 'PENTHOUSE', 4, 6, 45000.00, 1, 'Luxury penthouse with panoramic ocean view and private pool',
      '["WiFi","AC","TV","Mini Bar","Balcony","Jacuzzi","Private Pool","Hot Water","Room Service","Butler"]');

-- ============================================================
-- 3. GUESTS  (10 guests)
-- ============================================================
INSERT INTO guests (id, name, address, contact, email, nic, loyalty_pts, created_at) VALUES
  ('guest-001', 'Pansilu Prabharsha',  '12 Galle Rd, Colombo 03',          '+94 71 234 5678', 'pansilu@gmail.com',     '199801234567',  120, '2026-01-10 10:00:00'),
  ('guest-002', 'Hiruni Wickramasinghe','45 Kandy Rd, Peradeniya',          '+94 77 345 6789', 'hiruni.w@gmail.com',    '199503456789',  250, '2026-01-15 11:00:00'),
  ('guest-003', 'Roshan Mendis',        '7 Beach Ave, Negombo',             '+94 70 456 7890', 'roshan.m@yahoo.com',    '198904567890',   80, '2026-01-20 09:30:00'),
  ('guest-004', 'Amali Jayasuriya',     '23 High St, Galle',                '+94 76 567 8901', 'amali.j@hotmail.com',   '199205678901',  310, '2026-01-25 14:00:00'),
  ('guest-005', 'Tharindu Senanayake',  '56 Temple Rd, Kandy',              '+94 71 678 9012', 'tharindu.s@gmail.com',  '199706789012',   45, '2026-02-01 10:00:00'),
  ('guest-006', 'Priya Ranasinghe',     '89 Lake Dr, Battaramulla',         '+94 77 789 0123', 'priya.r@gmail.com',     '199007890123',  190, '2026-02-05 09:00:00'),
  ('guest-007', 'Saman Kulathunga',     '34 Main St, Kurunegala',           '+94 70 890 1234', 'saman.k@yahoo.com',     '198508901234',   60, '2026-02-10 15:00:00'),
  ('guest-008', 'Dilini Wijeratne',     '15 Park Rd, Ratnapura',            '+94 76 901 2345', 'dilini.w@gmail.com',    '200009012345',   30, '2026-02-15 11:30:00'),
  ('guest-009', 'Nuwan Bandara',        '67 Queen St, Anuradhapura',        '+94 71 012 3456', 'nuwan.b@hotmail.com',   '199210123456',  170, '2026-02-20 08:00:00'),
  ('guest-010', 'Chamari Dissanayake',  '99 Sea View Rd, Trincomalee',      '+94 77 123 4567', 'chamari.d@gmail.com',   '199411234567',   95, '2026-03-01 10:00:00');

-- ============================================================
-- 4. RESERVATIONS
--    Drop triggers temporarily so we can set reservation_no,
--    total_amount, and status explicitly without conflicts.
--
--    Layout (current date = 2026-03-04):
--    res-001 : Jan  – CHECKED_OUT  (room-001, guest-001)  3 nights × 8500
--    res-002 : Jan  – CHECKED_OUT  (room-003, guest-002)  4 nights × 14000
--    res-003 : Jan  – CHECKED_OUT  (room-005, guest-003)  3 nights × 22000
--    res-004 : Jan  – CANCELLED    (room-002, guest-004)  (no payment)
--    res-005 : Feb  – CHECKED_OUT  (room-002, guest-005)  3 nights × 8500
--    res-006 : Feb  – CHECKED_OUT  (room-004, guest-006)  3 nights × 14000
--    res-007 : Feb  – CHECKED_OUT  (room-006, guest-007)  3 nights × 45000
--    res-008 : Feb  – CANCELLED    (room-005, guest-008)  (no payment)
--    res-009 : Mar  – CONFIRMED    (room-001, guest-009)  arriving 2026-03-06
--    res-010 : Mar  – CHECKED_IN   (room-002, guest-010)  in-house now
--    res-011 : Mar  – CHECKED_IN   (room-003, guest-001)  in-house now
--    res-012 : Mar  – PENDING      (room-004, guest-002)  arriving 2026-03-07
--    res-013 : Mar  – CONFIRMED    (room-005, guest-003)  arriving 2026-03-08
--    res-014 : Mar  – PENDING      (room-006, guest-004)  arriving 2026-03-10
-- ============================================================

DROP TRIGGER IF EXISTS trg_reservation_no;
DROP TRIGGER IF EXISTS trg_calc_total;
DROP TRIGGER IF EXISTS trg_prevent_double_booking;

INSERT INTO reservations
  (id, reservation_no, guest_id, room_id, check_in_date, check_out_date, num_guests, status, total_amount, special_requests, created_by, updated_at, created_at)
VALUES
  -- ── January 2026 ──────────────────────────────────────────────────────────
  ('res-001','RES-2026-0001','guest-001','room-001','2026-01-05','2026-01-08',2,'CHECKED_OUT', 25500.00,
   NULL,'user-staff-001','2026-01-08 11:00:00','2026-01-03 10:00:00'),

  ('res-002','RES-2026-0002','guest-002','room-003','2026-01-10','2026-01-14',2,'CHECKED_OUT',56000.00,
   'Early check-in requested','user-mgr-001','2026-01-14 10:30:00','2026-01-07 14:00:00'),

  ('res-003','RES-2026-0003','guest-003','room-005','2026-01-18','2026-01-21',3,'CHECKED_OUT',66000.00,
   'Honeymoon setup please','user-staff-001','2026-01-21 11:00:00','2026-01-15 09:00:00'),

  ('res-004','RES-2026-0004','guest-004','room-002','2026-01-22','2026-01-25',1,'CANCELLED',  25500.00,
   NULL,'user-staff-002','2026-01-20 16:00:00','2026-01-18 11:00:00'),

  -- ── February 2026 ─────────────────────────────────────────────────────────
  ('res-005','RES-2026-0005','guest-005','room-002','2026-02-02','2026-02-05',2,'CHECKED_OUT', 25500.00,
   NULL,'user-staff-001','2026-02-05 10:30:00','2026-01-30 10:00:00'),

  ('res-006','RES-2026-0006','guest-006','room-004','2026-02-10','2026-02-13',2,'CHECKED_OUT', 42000.00,
   'Ground floor preferred','user-staff-002','2026-02-13 11:00:00','2026-02-07 15:00:00'),

  ('res-007','RES-2026-0007','guest-007','room-006','2026-02-14','2026-02-17',4,'CHECKED_OUT',135000.00,
   'Anniversary decoration','user-mgr-001','2026-02-17 10:00:00','2026-02-10 10:00:00'),

  ('res-008','RES-2026-0008','guest-008','room-005','2026-02-20','2026-02-23',2,'CANCELLED',  66000.00,
   NULL,'user-staff-003','2026-02-18 09:00:00','2026-02-15 12:00:00'),

  -- ── March 2026 (current / upcoming) ───────────────────────────────────────
  ('res-009','RES-2026-0009','guest-009','room-001','2026-03-06','2026-03-09',2,'CONFIRMED',  25500.00,
   NULL,'user-staff-001', NULL,'2026-03-01 10:00:00'),

  ('res-010','RES-2026-0010','guest-010','room-002','2026-03-04','2026-03-07',1,'CHECKED_IN', 25500.00,
   'Late checkout if possible','user-staff-002','2026-03-04 14:00:00','2026-03-02 09:00:00'),

  ('res-011','RES-2026-0011','guest-001','room-003','2026-03-04','2026-03-06',2,'CHECKED_IN', 28000.00,
   'Extra pillows','user-mgr-001','2026-03-04 14:30:00','2026-03-01 11:00:00'),

  ('res-012','RES-2026-0012','guest-002','room-004','2026-03-07','2026-03-10',2,'PENDING',    42000.00,
   NULL,'user-staff-001', NULL,'2026-03-03 14:00:00'),

  ('res-013','RES-2026-0013','guest-003','room-005','2026-03-08','2026-03-12',3,'CONFIRMED',  88000.00,
   'Sea-facing bed orientation','user-staff-003', NULL,'2026-03-02 16:00:00'),

  ('res-014','RES-2026-0014','guest-004','room-006','2026-03-10','2026-03-15',5,'PENDING',   225000.00,
   'Airport pickup required','user-mgr-001', NULL,'2026-03-03 10:00:00');

-- Sync room availability: mark rooms with active CHECKED_IN reservations as unavailable
UPDATE rooms SET is_available = 0 WHERE id IN ('room-002','room-003'); -- res-010, res-011
UPDATE rooms SET is_available = 1 WHERE id NOT IN ('room-002','room-003');

-- Recreate the three triggers that were dropped above
DELIMITER $$

CREATE TRIGGER trg_reservation_no
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
  DECLARE v_count INT;
  SELECT COUNT(*) + 1 INTO v_count FROM reservations WHERE YEAR(created_at) = YEAR(NOW());
  SET NEW.reservation_no = CONCAT('RES-', YEAR(NOW()), '-', LPAD(v_count, 4, '0'));
END$$

CREATE TRIGGER trg_calc_total
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
  DECLARE v_rate DECIMAL(10,2);
  SELECT rate_per_night INTO v_rate FROM rooms WHERE id = NEW.room_id;
  SET NEW.total_amount = DATEDIFF(NEW.check_out_date, NEW.check_in_date) * v_rate;
END$$

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

DELIMITER ;

-- ============================================================
-- 5. PAYMENTS
--    Only CHECKED_OUT reservations have COMPLETED payments.
--    One CHECKED_IN reservation has a PENDING payment (partially paid).
-- ============================================================
INSERT INTO payments (id, reservation_id, amount, payment_method, reference_no, payment_date, status, processed_by, notes)
VALUES
  -- January completed checkouts
  ('pay-001','res-001', 25500.00,'CASH','RCPT-2601-001','2026-01-08 11:30:00','COMPLETED','user-staff-001', NULL),
  ('pay-002','res-002', 56000.00,'CARD','TXN-9841-0012','2026-01-14 11:00:00','COMPLETED','user-staff-001', 'Visa ending 4523'),
  ('pay-003','res-003', 66000.00,'CARD','TXN-9841-0031','2026-01-21 11:30:00','COMPLETED','user-mgr-001',   'MasterCard ending 7890'),

  -- February completed checkouts
  ('pay-004','res-005', 25500.00,'CASH','RCPT-2602-002','2026-02-05 11:00:00','COMPLETED','user-staff-002', NULL),
  ('pay-005','res-006', 42000.00,'CARD','TXN-9841-0055','2026-02-13 11:30:00','COMPLETED','user-staff-001', 'Visa ending 3311'),
  ('pay-006','res-007',135000.00,'CARD','TXN-9841-0078','2026-02-17 10:30:00','COMPLETED','user-mgr-001',   'Amex ending 1001. Anniversary package included.'),

  -- March in-house — deposit / pending
  ('pay-007','res-010', 25500.00,'CASH','RCPT-2603-010','2026-03-04 14:30:00','PENDING',  'user-staff-002', 'Advance deposit collected'),
  ('pay-008','res-011', 28000.00,'CARD','TXN-9841-0102','2026-03-04 15:00:00','PENDING',  'user-mgr-001',   'Card pre-authorised');

-- ============================================================
-- 6. AUDIT LOG  (representative entries)
-- ============================================================
INSERT INTO audit_log (id, user_id, action, entity, entity_id, details, ip_address, created_at)
VALUES
  ('log-001','user-admin-001', 'LOGIN',        NULL,          NULL,      'Successful login',                    '192.168.1.10', '2026-03-04 08:00:00'),
  ('log-002','user-mgr-001',   'LOGIN',        NULL,          NULL,      'Successful login',                    '192.168.1.11', '2026-03-04 08:30:00'),
  ('log-003','user-staff-001', 'LOGIN',        NULL,          NULL,      'Successful login',                    '192.168.1.12', '2026-03-04 09:00:00'),
  ('log-004','user-staff-001', 'CREATE',       'reservation', 'res-001', 'Created reservation RES-2026-0001',   '192.168.1.12', '2026-01-03 10:00:00'),
  ('log-005','user-mgr-001',   'CREATE',       'reservation', 'res-002', 'Created reservation RES-2026-0002',   '192.168.1.11', '2026-01-07 14:00:00'),
  ('log-006','user-staff-001', 'CHECK_IN',     'reservation', 'res-001', 'Guest checked in — room 101',         '192.168.1.12', '2026-01-05 14:00:00'),
  ('log-007','user-staff-001', 'CHECK_OUT',    'reservation', 'res-001', 'Guest checked out — room 101',        '192.168.1.12', '2026-01-08 11:00:00'),
  ('log-008','user-staff-001', 'CHECK_IN',     'reservation', 'res-002', 'Guest checked in — room 201',         '192.168.1.12', '2026-01-10 13:30:00'),
  ('log-009','user-staff-001', 'CHECK_OUT',    'reservation', 'res-002', 'Guest checked out — room 201',        '192.168.1.12', '2026-01-14 10:30:00'),
  ('log-010','user-staff-002', 'CANCEL',       'reservation', 'res-004', 'Guest cancelled — room 102',          '192.168.1.13', '2026-01-20 16:00:00'),
  ('log-011','user-staff-001', 'CHECK_IN',     'reservation', 'res-005', 'Guest checked in — room 102',         '192.168.1.12', '2026-02-02 14:00:00'),
  ('log-012','user-staff-002', 'CHECK_OUT',    'reservation', 'res-005', 'Guest checked out — room 102',        '192.168.1.13', '2026-02-05 10:30:00'),
  ('log-013','user-mgr-001',   'CHECK_IN',     'reservation', 'res-007', 'Guest checked in — room 401',         '192.168.1.11', '2026-02-14 14:00:00'),
  ('log-014','user-mgr-001',   'CHECK_OUT',    'reservation', 'res-007', 'Guest checked out — room 401',        '192.168.1.11', '2026-02-17 10:00:00'),
  ('log-015','user-staff-003', 'CANCEL',       'reservation', 'res-008', 'Guest cancelled — room 301',          '192.168.1.14', '2026-02-18 09:00:00'),
  ('log-016','user-staff-002', 'CHECK_IN',     'reservation', 'res-010', 'Guest checked in — room 102',         '192.168.1.13', '2026-03-04 14:00:00'),
  ('log-017','user-mgr-001',   'CHECK_IN',     'reservation', 'res-011', 'Guest checked in — room 201',         '192.168.1.11', '2026-03-04 14:30:00'),
  ('log-018','user-staff-001', 'HTTP_REQUEST', 'HTTP_REQUEST', NULL,     'GET /dashboard',                      '0:0:0:0:0:0:0:1', '2026-03-04 09:05:00'),
  ('log-019','user-mgr-001',   'HTTP_REQUEST', 'HTTP_REQUEST', NULL,     'GET /reports?action=dashboard',       '0:0:0:0:0:0:0:1', '2026-03-04 08:35:00'),
  ('log-020','user-admin-001', 'HTTP_REQUEST', 'HTTP_REQUEST', NULL,     'GET /dashboard',                      '0:0:0:0:0:0:0:1', '2026-03-04 08:05:00');

-- Restore SQL mode
SET SESSION sql_mode = @OLD_SQL_MODE;
