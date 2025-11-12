package com.banking.database;

import com.banking.util.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    private static final Logger logger = LoggerUtil.getLogger(DatabaseConnection.class);
    private static Connection connection = null;

    static {
        safeInitialize();
    }

    private static void initializeDatabase() {
        try {
            Properties props = new Properties();
            InputStream input = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("database.properties");

            if (input == null) {
                logger.warn("Database configuration file not found, using defaults");
                // Use default configuration
                props.setProperty("database.url", "jdbc:sqlite:database/banking.db");
                props.setProperty("database.driver", "org.sqlite.JDBC");
            } else {
                props.load(input);
            }

            String url = props.getProperty("database.url", "jdbc:sqlite:database/banking.db");
            String driver = props.getProperty("database.driver", "org.sqlite.JDBC");

            // Extract database file path from URL
            String dbPath = url.replace("jdbc:sqlite:", "");
            File dbFile = new File(dbPath);

            // Create database directory if it doesn't exist
            File dbDir = dbFile.getParentFile();
            if (dbDir != null && !dbDir.exists()) {
                boolean dirsCreated = dbDir.mkdirs();
                if (dirsCreated) {
                    logger.info("Created database directory: {}", dbDir.getAbsolutePath());
                }
            }

            logger.info("Database path: {}", dbFile.getAbsolutePath());

            // Check if file exists and is readable/writable
            if (dbFile.exists()) {
                if (!dbFile.canRead() || !dbFile.canWrite()) {
                    logger.error("Database file exists but doesn't have read/write permissions: {}", dbPath);
                    throw new SQLException("Database file permission error");
                }
                logger.info("Database file exists: {}", dbPath);
            } else {
                logger.info("Database file will be created: {}", dbPath);
            }

            // Load SQLite JDBC driver
            Class.forName(driver);

            // Create connection
            connection = DriverManager.getConnection(url);
            logger.info("Database connection established successfully");

            // Verify connection and create tables
            if (connection != null && !connection.isClosed()) {
                createTables();
            }

        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            // Don't throw exception here, allow application to start
            // but database operations will fail gracefully
        }
    }
    // Keep this method but don't call it automatically
    public static void recreateDatabase() {
        logger.warn("MANUAL DATABASE RECREATION REQUESTED - THIS WILL DELETE ALL DATA!");

        try {
            // Close existing connection
            closeConnection();

            // Get database file path
            Properties props = new Properties();
            InputStream input = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("database.properties");

            if (input != null) {
                props.load(input);
            }
            String url = props.getProperty("database.url", "jdbc:sqlite:database/banking.db");
            String dbPath = url.replace("jdbc:sqlite:", "");

            // Delete the corrupted file
            File dbFile = new File(dbPath);
            if (dbFile.exists()) {
                boolean deleted = dbFile.delete();
                if (deleted) {
                    logger.info("Deleted database file: {}", dbPath);
                } else {
                    logger.error("Failed to delete database file: {}", dbPath);
                    return;
                }
            }

            // Reinitialize
            initializeDatabase();
            logger.info("Database recreated successfully");

        } catch (Exception e) {
            logger.error("Failed to recreate database", e);
        }
    }

    // Add a safe initialization method
    public static void safeInitialize() {
        try {
            Properties props = new Properties();
            InputStream input = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("database.properties");

            if (input != null) {
                props.load(input);
            }
            String url = props.getProperty("database.url", "jdbc:sqlite:database/banking.db");
            String driver = props.getProperty("database.driver", "org.sqlite.JDBC");

            // Extract database file path from URL
            String dbPath = url.replace("jdbc:sqlite:", "");
            File dbFile = new File(dbPath);

            // Create database directory if it doesn't exist
            File dbDir = dbFile.getParentFile();
            if (dbDir != null && !dbDir.exists()) {
                boolean dirsCreated = dbDir.mkdirs();
                if (dirsCreated) {
                    logger.info("Created database directory: {}", dbDir.getAbsolutePath());
                }
            }

            logger.info("Database path: {}", dbFile.getAbsolutePath());

            // Load SQLite JDBC driver
            Class.forName(driver);

            connection = DriverManager.getConnection(url);
            logger.info("Database connection established successfully");

            // Create tables if they don't exist (safe operation)
            createTables();

        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            // Don't throw exception here, allow application to start
            // but database operations will fail gracefully
        }
    }

    private static void createTables() {
        String[] createTables = {
                // Users table
                "CREATE TABLE IF NOT EXISTS users (" +
                        "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password_hash VARCHAR(255) NOT NULL, " +
                        "email VARCHAR(100) UNIQUE NOT NULL, " +
                        "full_name VARCHAR(100) NOT NULL, " +
                        "phone_number VARCHAR(15), " +
                        "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "last_login TIMESTAMP, " +
                        "is_active BOOLEAN DEFAULT 1)",

                // Accounts table
                "CREATE TABLE IF NOT EXISTS accounts (" +
                        "account_number VARCHAR(20) PRIMARY KEY, " +
                        "user_id INTEGER NOT NULL, " +
                        "account_holder_name VARCHAR(100) NOT NULL, " +
                        "balance DECIMAL(15,2) DEFAULT 0.00, " +
                        "account_type VARCHAR(20) DEFAULT 'SAVINGS', " +
                        "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "min_balance DECIMAL(15,2) DEFAULT 100.00, " +
                        "is_active BOOLEAN DEFAULT 1, " +
                        "FOREIGN KEY (user_id) REFERENCES users(user_id))",

                // Transactions table
                "CREATE TABLE IF NOT EXISTS transactions (" +
                        "transaction_id VARCHAR(50) PRIMARY KEY, " +
                        "from_account VARCHAR(20), " +
                        "to_account VARCHAR(20), " +
                        "amount DECIMAL(15,2) NOT NULL, " +
                        "transaction_type VARCHAR(20) NOT NULL, " +
                        "description TEXT, " +
                        "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "status BOOLEAN DEFAULT 1, " +
                        "FOREIGN KEY (from_account) REFERENCES accounts(account_number), " +
                        "FOREIGN KEY (to_account) REFERENCES accounts(account_number))"
        };

        try (Statement stmt = connection.createStatement()) {
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");

            for (String sql : createTables) {
                try {
                    stmt.execute(sql);
                    logger.debug("Executed SQL: {}", sql);
                } catch (SQLException e) {
                    logger.error("Failed to execute SQL: {}", sql, e);
                    throw e;
                }
            }
            logger.info("Database tables created/verified successfully");
        } catch (SQLException e) {
            logger.error("Failed to create database tables", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            initializeDatabase();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }

    // Test database connection
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SELECT 1");
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
        }
        return false;
    }
}