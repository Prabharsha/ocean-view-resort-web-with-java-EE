# Ocean View Resort - Debug & Run Guide

## Project Overview
- **Framework**: Jakarta EE (Java EE)
- **Build Tool**: Maven
- **Java Version**: 17
- **Database**: MySQL
- **Application Type**: WAR (Web Application)
- **Server**: Requires Jakarta-compatible application server (Tomcat 10+, GlassFish, WildFly, etc.)

---

## Prerequisites

1. **Java 17+** installed
   ```powershell
   java -version
   ```

2. **Maven 3.8.1+** installed
   ```powershell
   mvn -version
   ```

3. **MySQL 8.0+** installed and running
   ```powershell
   mysql --version
   ```

4. **Git** (for version control)

---

## Step 1: Database Setup

### 1.1 Create Database and User
```sql
CREATE DATABASE IF NOT EXISTS ocean_view_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'resort_user'@'localhost' IDENTIFIED BY 'secure_password_123';
GRANT ALL PRIVILEGES ON ocean_view_db.* TO 'resort_user'@'localhost';
FLUSH PRIVILEGES;
```

### 1.2 Run Schema Script
```powershell
# Using MySQL command line
mysql -u resort_user -p ocean_view_db < src/main/resources/schema.sql
```

### 1.3 Load Sample Data (Optional)
```powershell
mysql -u resort_user -p ocean_view_db < src/main/resources/data.sql
```

---

## Step 2: Configure Application Properties

### 2.1 Update Database Properties
Edit `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/ocean_view_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=resort_user
db.password=secure_password_123
db.driver=com.mysql.cj.jdbc.Driver
```

### 2.2 Configure Email (Optional)
Edit `src/main/resources/email.properties`:
```properties
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.from.email=your-email@gmail.com
mail.from.password=your-app-password
```

---

## Step 3: Building the Project

### 3.1 Clean Build
```powershell
mvn clean package
```

This creates: `target/ocean-view-resort-1.0.0.war`

### 3.2 Build Without Running Tests
```powershell
mvn clean package -DskipTests
```

### 3.3 Run Unit Tests
```powershell
mvn test
```

---

## Step 4: Running the Application

### Option A: Using Maven (Development)
```powershell
# Run with embedded server (if configured)
mvn tomcat7:run
# OR
mvn jetty:run
```

**Note**: You need to add the plugin to pom.xml first (see Step 5).

### Option B: Deploy to Application Server (Recommended)

#### For Tomcat 10+ (Jakarta EE compatible):
1. Download Apache Tomcat 10+ from https://tomcat.apache.org
2. Extract to a location (e.g., `C:\apache-tomcat-10.1.x`)
3. Copy WAR file:
   ```powershell
   Copy-Item -Path "target/ocean-view-resort-1.0.0.war" -Destination "C:\apache-tomcat-10.1.x\webapps\ROOT.war"
   ```
4. Start Tomcat:
   ```powershell
   C:\apache-tomcat-10.1.x\bin\catalina.bat run
   ```
5. Access application:
   ```
   http://localhost:8080
   ```

#### For GlassFish 7+:
1. Download from https://glassfish.org
2. Deploy using:
   ```powershell
   asadmin deploy target/ocean-view-resort-1.0.0.war
   ```

---

## Step 5: IDE Configuration (JetBrains IntelliJ IDEA)

### 5.1 Import Project
1. **File** → **Open** → Select project folder
2. Select "Maven" when prompted
3. Wait for Maven to download dependencies

### 5.2 Configure Application Server
1. **Run** → **Edit Configurations**
2. Click **+** → **Tomcat Server** → **Local**
3. Download Tomcat if needed (IntelliJ can do this)
4. Set:
   - **Server**: Tomcat 10+
   - **Deployment**: 
     - Click **+** → Select `ocean-view-resort` WAR artifact
     - Application Context: `/`

### 5.3 Run Configuration
1. Select Tomcat configuration from dropdown
2. Click **Run** button (Shift+F10)
3. Check **Run** tab for logs

### 5.4 Debug Configuration
1. Same as above, but click **Debug** (Shift+F9) instead of **Run**
2. Set breakpoints in code by clicking left margin
3. Application will pause at breakpoints

---

## Debugging Tips

### 1. Enable Debug Logging
Update a servlet/controller:
```java
System.out.println("Debug: Variable value = " + variable);
// OR
java.util.logging.Logger.getLogger(YourClass.class.getName()).info("Debug message");
```

### 2. Check Tomcat Logs
```powershell
# Catalina logs
C:\apache-tomcat-10.1.x\logs\catalina.out

# Windows event viewer for service logs
Get-EventLog -LogName Application | Where-Object {$_.Source -like "*Tomcat*"}
```

### 3. Use Browser DevTools
- **F12** - Open Developer Tools
- **Network** tab - Check HTTP requests
- **Console** tab - JavaScript errors
- **Application** tab - Cookies and session storage

### 4. Common Issues & Solutions

#### Issue: 404 Not Found
- Check web.xml URL mappings
- Verify controller exists and is in correct package
- Check filter configurations

#### Issue: 500 Internal Server Error
- Check Tomcat logs for stack traces
- Verify database connection properties
- Check database is running and accessible

#### Issue: Database Connection Failed
```powershell
# Test MySQL connection
mysql -h localhost -u resort_user -p -e "SELECT 1;"
```

#### Issue: ClassNotFoundException
- Run `mvn clean install`
- Check pom.xml dependencies
- Ensure all JARs are in WEB-INF/lib/

#### Issue: JSP Compilation Errors
- Check JSP syntax in WEB-INF/views/
- Verify JSTL taglib URIs match dependency versions
- Clear browser cache (Ctrl+Shift+Delete)

### 5. IntelliJ Debugger Shortcuts
- **F8** - Step over
- **F7** - Step into
- **Shift+F8** - Step out
- **F9** - Resume execution
- **Ctrl+Shift+F8** - View breakpoints
- **Ctrl+F8** - Toggle breakpoint at cursor

### 6. Maven Troubleshooting
```powershell
# Clear local repository cache
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository"

# Rebuild with debug output
mvn clean package -X

# Check dependency tree
mvn dependency:tree

# Force update dependencies
mvn clean install -U
```

---

## Application Access

### Default URLs
- **Home**: http://localhost:8080/
- **Login**: http://localhost:8080/auth?action=login
- **Dashboard**: http://localhost:8080/dashboard
- **Reservations**: http://localhost:8080/reservations
- **Guests**: http://localhost:8080/guests
- **Rooms**: http://localhost:8080/rooms
- **Payments**: http://localhost:8080/payments
- **Reports**: http://localhost:8080/reports

### Default Credentials (from data.sql)
- Check `data.sql` for seed user credentials
- First user typically: `admin` / `password`

---

## Performance Profiling

### Using JProfiler or YourKit
1. Download profiler
2. Configure in Tomcat startup options
3. Monitor memory, CPU, and thread usage

### Using VisualVM
```powershell
# Find Java process
jps -l

# Connect and monitor
jvisualvm
```

---

## Deployment Checklist

- [ ] Database created and populated
- [ ] db.properties configured with correct credentials
- [ ] email.properties configured (if email features needed)
- [ ] All dependencies resolved (`mvn clean install`)
- [ ] No compilation errors
- [ ] Unit tests passing (`mvn test`)
- [ ] WAR built successfully
- [ ] Application server installed and running
- [ ] WAR deployed to application server
- [ ] Application accessible via browser
- [ ] Login functionality working
- [ ] Database queries returning expected results

---

## Additional Resources

- [Jakarta EE Documentation](https://jakarta.ee)
- [Apache Tomcat 10 Docs](https://tomcat.apache.org)
- [Maven Official Guide](https://maven.apache.org/guides/index.html)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [IntelliJ IDEA Help](https://www.jetbrains.com/help/idea/)

---

## Quick Start Commands

```powershell
# Clone and setup (if not done)
# cd ocean-view-resort

# Setup database
mysql -u root -p < src/main/resources/schema.sql
mysql -u root -p < src/main/resources/data.sql

# Build
mvn clean package

# Deploy to Tomcat (update path as needed)
Copy-Item -Path "target/ocean-view-resort-1.0.0.war" -Destination "C:\apache-tomcat-10.1.x\webapps\ROOT.war"

# Start Tomcat
C:\apache-tomcat-10.1.x\bin\startup.bat

# Access
# http://localhost:8080
```

---

**Last Updated**: March 2026

