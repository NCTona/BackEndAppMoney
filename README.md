# 💰 Expenses Tracker — Spring Boot Backend

REST API backend cho ứng dụng quản lý chi tiêu cá nhân, tích hợp AI predictions.

## ⚡ Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Framework | Spring Boot 3.3.5 |
| Java | 17 |
| Database | MySQL 8.0 |
| Auth | JWT (jjwt 0.12.5) |
| Security | Spring Security + SSL (PKCS12) |
| Build | Maven Wrapper |
| Rate Limit | Bucket4j |
| Coverage | JaCoCo |

## 📋 Yêu cầu

- **JDK** 17+
- **MySQL** 8.0+
- **Maven** 3.8+ (hoặc dùng `mvnw` có sẵn)

## 🚀 Cài đặt

### 1. Clone repo

```bash
git clone https://github.com/NCTona/BackEndAppMoney.git
cd BackEndAppMoney
```

### 2. Tạo database MySQL

```sql
CREATE DATABASE expensestracker;
```

### 3. Tạo file `.env`

Copy từ `.env.example` và điền thông tin:

```bash
cp .env.example .env
```

Nội dung `.env`:

```env
DB_USERNAME=root
DB_PASSWORD=your_password
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
JWT_SECRET=your_jwt_secret_key_at_least_32_chars
SSL_KEYSTORE_PASSWORD=your_keystore_password
DB_ENCRYPTION_KEY=your_16_char_key
DB_ENCRYPTION_IV=your_16_char_iv
```

> ⚠️ `MAIL_PASSWORD` là **App Password** của Gmail, không phải mật khẩu thường. Tạo tại [Google App Passwords](https://myaccount.google.com/apppasswords).

### 4. Cấu hình SSL Certificate

Tạo file `expense-tracker.p12` và đặt vào `src/main/resources/`:

```bash
keytool -genkeypair -alias expensetracker -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore src/main/resources/expense-tracker.p12 \
  -validity 3650 -storepass your_keystore_password
```

### 5. Build & Run

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

Server chạy tại `https://localhost:8080`

## 📡 API Endpoints

### 🔐 Authentication

| Method | Endpoint | Mô tả |
|---|---|---|
| `POST` | `/api/users/register` | Đăng ký tài khoản |
| `POST` | `/api/users/login` | Đăng nhập (trả JWT) |
| `POST` | `/api/users/logout` | Đăng xuất |
| `POST` | `/api/users/refresh-token` | Refresh access token |
| `POST` | `/api/users/send-otp` | Gửi OTP qua email |
| `POST` | `/api/users/verify-otp` | Xác thực OTP |
| `POST` | `/api/users/reset-password` | Đặt lại mật khẩu |

### 💳 Transactions

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/finance?month=&year=` | Lấy giao dịch theo tháng |
| `POST` | `/api/transactions` | Tạo giao dịch mới |
| `PUT` | `/api/transactions/{id}` | Sửa giao dịch |
| `DELETE` | `/api/transactions/{id}` | Xóa giao dịch |
| `GET` | `/api/transactions/search` | Tìm kiếm giao dịch |

### 📊 Reports

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/report/monthly_expense` | Báo cáo chi theo tháng |
| `GET` | `/api/report/monthly_income` | Báo cáo thu theo tháng |

### 🔄 Fixed Transactions

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/fixed-transactions` | Danh sách giao dịch cố định |
| `POST` | `/api/fixed-transactions` | Tạo giao dịch cố định |
| `PUT` | `/api/fixed-transactions/{id}` | Sửa giao dịch cố định |
| `DELETE` | `/api/fixed-transactions/{id}` | Xóa giao dịch cố định |

### 📏 Category Limits (Ngân sách)

| Method | Endpoint | Mô tả |
|---|---|---|
| `PUT` | `/api/category-limits/save` | Đặt hạn mức chi tiêu |
| `GET` | `/api/category-limits/remaining` | Xem hạn mức còn lại |
| `GET` | `/api/category-limits/current` | Xem hạn mức hiện tại |

### 🤖 AI Forecast (tích hợp MLOps)

| Method | Endpoint | Model | Mô tả |
|---|---|---|---|
| `GET` | `/api/forecast/summary` | Tất cả | **Tổng hợp 3 model trong 1 response** |
| `GET` | `/api/forecast/categories` | LightGBM | Dự đoán chi theo danh mục |
| `GET` | `/api/forecast/trend/{categoryId}` | LightGBM | So sánh xu hướng với số đông |
| `GET` | `/api/forecast/anomalies` | IForest | Phát hiện giao dịch bất thường |
| `GET` | `/api/forecast/alerts` | Thống kê | Cảnh báo ngày chi cao |

### 🔧 MLOps Internal

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/internal/model/download` | Tải model TFLite |
| `POST` | `/api/internal/model/upload` | Upload model mới |
| `GET` | `/api/internal/data/export` | Export dữ liệu training |

## 🏗️ Cấu trúc dự án

```
src/main/java/com/example/expensestracker/
├── controller/
│   ├── UserController.java            # Auth APIs
│   ├── TransactionControlller.java    # CRUD giao dịch
│   ├── FinanceController.java         # Tổng hợp tài chính
│   ├── ReportController.java          # Báo cáo
│   ├── CategoryController.java        # Danh mục
│   ├── CategoryLimitController.java   # Hạn mức ngân sách
│   ├── FixedTransactionController.java# Giao dịch cố định
│   ├── CategoryForecastController.java# AI Forecast APIs
│   └── MLOpsInternalController.java   # Internal APIs cho MLOps
├── service/
│   ├── CategoryForecastService.java   # LightGBM predictions
│   ├── WeeklyForecastService.java     # LSTM weekly predictions
│   ├── AnomalyDetectionService.java   # Isolation Forest
│   └── SpendingPatternService.java    # Statistical alerts
├── model/
│   ├── entity/                        # JPA entities
│   └── dto/                           # Request/Response DTOs
├── repositories/                      # Spring Data JPA
├── config/                            # Security, JWT config
└── filter/                            # JWT filter, rate limit
```

## ⚙️ Kết nối MLOps Server

Backend gọi FastAPI prediction server. Cấu hình trong `application.properties`:

```properties
mlops.predict-server-url=http://localhost:8001
```

> Đảm bảo **MLOps server** (`serve_predict.py`) đang chạy trước khi gọi các AI forecast APIs.

## 🔒 Bảo mật

- **JWT** — Access token (2.5 phút) + Refresh token (15 ngày)
- **SSL/TLS** — HTTPS với PKCS12 certificate
- **Rate Limiting** — 10 requests/phút (Bucket4j)
- **DB Encryption** — Mã hóa dữ liệu nhạy cảm (AES)
- **Login Lock** — Khóa 15 phút sau 5 lần đăng nhập sai
