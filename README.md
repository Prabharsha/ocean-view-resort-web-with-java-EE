# Ocean View Resort Reservation System

Ocean View Resort is a Jakarta EE web application for managing resort operations such as reservations, guests, rooms, and payments. It delivers a staff-facing dashboard with role-based access for managers and administrators.

## Features
- Reservation, guest, and room management
- Payments with cash/card processing
- Manager/admin reports and dashboards
- Staff management and maintenance tooling
- Help & FAQ section for guidance

## Tech Stack
- **Java** 17
- **Jakarta EE** (Servlet/JSP)
- **Maven** (WAR packaging)
- **MySQL** 8+

## Prerequisites
- Java 17+
- Maven 3.8.1+
- MySQL 8.0+

## Setup
1. **Create the database and user**
   ```sql
   CREATE DATABASE IF NOT EXISTS ocean_view_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'resort_user'@'localhost' IDENTIFIED BY 'secure_password_123';
   GRANT ALL PRIVILEGES ON ocean_view_db.* TO 'resort_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Load schema and seed data**
   ```bash
   mysql -u resort_user -p ocean_view_db < src/main/resources/schema.sql
   mysql -u resort_user -p ocean_view_db < src/main/resources/data.sql
   ```

3. **Configure application properties**
   - `src/main/resources/db.properties`
   - `src/main/resources/email.properties` (optional)

For a detailed walkthrough (including IDE setup and troubleshooting), see [`DEBUG_AND_RUN_GUIDE.md`](DEBUG_AND_RUN_GUIDE.md).

## Build & Test
```bash
mvn clean package
mvn test
```

## Run
Deploy the generated WAR (`target/ocean-view-resort-1.0.0.war`) to a Jakarta-compatible server such as Tomcat 10+ or GlassFish 7+. Example using Tomcat:

1. Copy the WAR to your Tomcat webapps folder as `ROOT.war`.
2. Start Tomcat and visit `http://localhost:8080`.

## Useful URLs
- `/auth?action=login`
- `/dashboard`
- `/reservations`
- `/guests`
- `/rooms`
- `/payments`
- `/reports` (manager/admin only)
- `/staff` (admin only)

## Documentation
- `docs/TASK_A_UML_Design_Documentation.md`
- `docs/TASK_C_Test_Plan_Documentation.md`
