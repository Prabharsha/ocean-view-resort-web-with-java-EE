package com.oceanview.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final Logger log = LoggerFactory.getLogger(DBConnection.class);

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            props.load(is);
            Class.forName(props.getProperty("db.driver"));
            String url = props.getProperty("db.url");
            log.info("Connecting to database: {}", url);
            connection = DriverManager.getConnection(
                url,
                props.getProperty("db.username"),
                props.getProperty("db.password"));
            log.info("Database connection established successfully");
        } catch (Exception e) {
            log.error("Failed to establish database connection", e);
            throw new RuntimeException("Failed to establish database connection", e);
        }
    }

    public static synchronized DBConnection getInstance() {
        try {
            if (instance == null || instance.connection.isClosed()) {
                log.debug("Creating new DBConnection instance");
                instance = new DBConnection();
            }
        } catch (SQLException e) {
            log.warn("Existing connection is closed/invalid, re-creating: {}", e.getMessage());
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}



