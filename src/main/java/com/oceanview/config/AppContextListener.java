package com.oceanview.config;

import com.oceanview.util.DBConnection;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("=== Ocean View Resort Application Starting ===");
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            log.info("Database connection verified on startup");

            // Log user count to confirm seed data is present
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    log.info("Users in database: {}", count);
                    if (count == 0) {
                        log.warn("No users found! Run schema.sql and data.sql first.");
                    }
                }
            }
        } catch (Exception e) {
            log.error("STARTUP FAILED - could not connect to database: {}", e.getMessage(), e);
            log.error("Check src/main/resources/db.properties for correct DB URL, username, password");
        }
        log.info("=== Application started. To fix login issues run: " +
                 "mvn exec:java -Dexec.mainClass=\"com.oceanview.util.GeneratePasswordHashes\" ===");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("=== Ocean View Resort Application Shutting Down ===");
    }
}
