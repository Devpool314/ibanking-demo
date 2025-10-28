-- Ghi chú: Cú pháp ON DUPLICATE KEY UPDATE ... dùng cho MySQL/MariaDB
-- để script có thể chạy lại nhiều lần mà không gây lỗi khóa chính (primary key) bị trùng.

-- =============================================
-- Bảng: users
-- Tạo 1 admin và 3 user sinh viên với các số dư khác nhau.
-- Mật khẩu {noop} là để Spring Security biết đây là mật khẩu thô (không mã hóa).
-- =============================================
INSERT INTO users (id, username, password, email, full_name, phone, balance, role, created_at)
VALUES 
(1, 'admin', '{noop}admin123', 'admin@example.com', 'Quản Trị Viên', '0900000001', 0, 'ADMIN', NOW()),
(2, 'user_sv001', '{noop}user123', 'sv001@example.com', 'Nguyễn Văn A', '0900000002', 10000000, 'USER', NOW()),
(3, 'user_sv002', '{noop}user123', 'sv002@example.com', 'Trần Văn B', '0900000003', 5000000, 'USER', NOW()),
(4, 'user_sv003', '{noop}user123', 'sv003@example.com', 'Lê Thị C', '0900000004', 20000000, 'USER', NOW())
ON DUPLICATE KEY UPDATE 
username=VALUES(username), password=VALUES(password), email=VALUES(email), full_name=VALUES(full_name), balance=VALUES(balance), role=VALUES(role);


-- =============================================
-- Bảng: students
-- Tạo 3 sinh viên tương ứng
-- =============================================
INSERT INTO students (id, mssv, name, tuition_fee)
VALUES 
(1, 'SV001', 'Nguyễn Văn A', 20000000),
(2, 'SV002', 'Trần Văn B', 18000000),
(3, 'SV003', 'Lê Thị C', 21000000)
ON DUPLICATE KEY UPDATE 
mssv=VALUES(mssv), name=VALUES(name), tuition_fee=VALUES(tuition_fee);


-- =============================================
-- Bảng: tuition_info
-- Tạo thông tin học phí cho 3 sinh viên
-- paid=0: Chưa thanh toán đủ, paid=1: Đã thanh toán đủ
-- =============================================
INSERT INTO tuition_info (id, amount, fullname, mssv, paid)
VALUES 
(1, 20000000, 'Nguyễn Văn A', 'SV001', 0),
(2, 18000000, 'Trần Văn B', 'SV002', 1),
(3, 21000000, 'Lê Thị C', 'SV003', 0)
ON DUPLICATE KEY UPDATE 
amount=VALUES(amount), fullname=VALUES(fullname), mssv=VALUES(mssv), paid=VALUES(paid);


-- =============================================
-- Bảng: payment (Lịch sử thanh toán đã xử lý)
-- Tạo lịch sử thanh toán: 1 thành công (cho SV002), 1 thất bại (cho SV001)
-- =============================================
INSERT INTO payment (id, payer_username, amount, available_balance, tuition_fee, terms_accepted, paid, payment_date, status, mssv, description, student_name, correlation_id, created_at)
VALUES 
(1, 'user_sv002', 18000000, 5000000, 18000000, 1, 1, NOW() - INTERVAL 2 DAY, 'SUCCESS', 'SV002', 'Thanh toan hoc phi hoc ky 1', 'Trần Văn B', 'CORR-AAA-111', NOW() - INTERVAL 2 DAY),
(2, 'user_sv001', 20000000, 10000, 20000000, 1, 0, NOW() - INTERVAL 1 DAY, 'FAILED_INSUFFICIENT_FUNDS', 'SV001', 'Thanh toan hoc phi (That bai)', 'Nguyễn Văn A', 'CORR-BBB-222', NOW() - INTERVAL 1 DAY),
(3, 'user_sv001', 10000000, 10000000, 20000000, 1, 1, NOW() - INTERVAL 1 HOUR, 'SUCCESS', 'SV001', 'Thanh toan hoc phi (Dot 1)', 'Nguyễn Văn A', 'CORR-CCC-333', NOW() - INTERVAL 1 HOUR)
ON DUPLICATE KEY UPDATE 
status=VALUES(status), amount=VALUES(amount), description=VALUES(description);


-- =============================================
-- Bảng: payment_requests (Yêu cầu thanh toán đang xử lý)
-- Tạo 1 request đang PENDING cho SV003
-- =============================================
INSERT INTO payment_requests (id, payer_username, payer_email, mssv, amount, otp_code, created_at, expires_at, verified, status)
VALUES 
(1, 'user_sv003', 'sv003@example.com', 'SV003', 21000000, '123456', NOW(), NOW() + INTERVAL 5 MINUTE, 0, 'PENDING')
ON DUPLICATE KEY UPDATE 
status=VALUES(status), otp_code=VALUES(otp_code), expires_at=VALUES(expires_at);


-- =============================================
-- Bảng: otp_verification
-- Tạo 1 OTP đang PENDING liên kết với payment_request ở trên (giả sử transaction_id = 1)
-- =============================================
INSERT INTO otp_verification (id, email, otp_hash, attempts, status, transaction_id, created_at, expires_at)
VALUES 
(1, 'sv003@example.com', 'HASH_CUA_123456', 0, 'PENDING', 1, NOW(), NOW() + INTERVAL 5 MINUTE)
ON DUPLICATE KEY UPDATE 
status=VALUES(status), otp_hash=VALUES(otp_hash), expires_at=VALUES(expires_at);