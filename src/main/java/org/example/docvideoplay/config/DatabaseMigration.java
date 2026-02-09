package org.example.docvideoplay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@Order(1) // Run before other runners
public class DatabaseMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Running database migration to remove first_name and last_name columns...");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Step 1: Make the columns nullable
            try {
                statement.executeUpdate("ALTER TABLE users MODIFY COLUMN first_name VARCHAR(255) NULL");
                logger.info("Made first_name column nullable");
            } catch (Exception e) {
                logger.warn("Error modifying first_name column: {}", e.getMessage());
            }

            try {
                statement.executeUpdate("ALTER TABLE users MODIFY COLUMN last_name VARCHAR(255) NULL");
                logger.info("Made last_name column nullable");
            } catch (Exception e) {
                logger.warn("Error modifying last_name column: {}", e.getMessage());
            }

            // Step 2: Remove the columns
            try {
                statement.executeUpdate("ALTER TABLE users DROP COLUMN first_name");
                logger.info("Removed first_name column");
            } catch (Exception e) {
                logger.warn("Error removing first_name column: {}", e.getMessage());
            }

            try {
                statement.executeUpdate("ALTER TABLE users DROP COLUMN last_name");
                logger.info("Removed last_name column");
            } catch (Exception e) {
                logger.warn("Error removing last_name column: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error running database migration: {}", e.getMessage());
        }

        logger.info("Database migration completed");
    }
}
