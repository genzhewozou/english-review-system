package org.example.docvideoplay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ForeignKeyRemovalMigration implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ForeignKeyRemovalMigration.class);

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Run before everything else
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        logger.info("Running database migration to remove foreign key constraints and cleanup highlights table...");

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String url = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");

        try {
            // Explicitly load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL driver loaded successfully");

            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()) {

                // Step 1: Handle todo_items table foreign key constraints
                try {
                    boolean todoItemsExists = statement.executeQuery("SHOW TABLES LIKE 'todo_items'")
                            .next();

                    if (todoItemsExists) {
                        // Check for foreign key constraints referencing highlights
                        ResultSet todoForeignKeys = statement.executeQuery(
                                "SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS " +
                                        "WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'todo_items' " +
                                        "AND REFERENCED_TABLE_NAME = 'highlights'");

                        while (todoForeignKeys.next()) {
                            String constraintName = todoForeignKeys.getString("CONSTRAINT_NAME");
                            try {
                                statement.executeUpdate("ALTER TABLE todo_items DROP FOREIGN KEY " + constraintName);
                                logger.info("Removed foreign key constraint {} from todo_items table", constraintName);
                            } catch (Exception e) {
                                logger.warn("Error removing foreign key constraint {} from todo_items: {}", constraintName, e.getMessage());
                            }
                        }
                        todoForeignKeys.close();

                        // Remove relatedHighlightId column if it exists
                        try {
                            // Check if the column exists first
                            ResultSet columnCheck = statement.executeQuery(
                                    "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
                                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'todo_items' " +
                                            "AND COLUMN_NAME = 'relatedHighlightId'");

                            if (columnCheck.next()) {
                                statement.executeUpdate("ALTER TABLE todo_items DROP COLUMN relatedHighlightId");
                                logger.info("Removed relatedHighlightId column from todo_items table");
                            }
                            columnCheck.close();
                        } catch (Exception e) {
                            logger.warn("Error removing relatedHighlightId column: {}", e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error handling todo_items table: {}", e.getMessage());
                }

                // Step 2: Handle review_records table foreign key constraints
                try {
                    boolean reviewRecordsExists = statement.executeQuery("SHOW TABLES LIKE 'review_records'")
                            .next();

                    if (reviewRecordsExists) {
                        logger.info("review_records table exists");

                        // Step 3: Check if the specific foreign key constraint exists
                        ResultSet constraintResult = statement.executeQuery(
                                "SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS " +
                                        "WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'review_records' " +
                                        "AND CONSTRAINT_NAME = 'FKpjgmxkoy86dyvs5o2bwxhuigu'");

                        boolean constraintExists = constraintResult.next();
                        constraintResult.close();

                        if (constraintExists) {
                            // Drop the constraint if it exists
                            statement.executeUpdate("ALTER TABLE review_records DROP FOREIGN KEY FKpjgmxkoy86dyvs5o2bwxhuigu");
                            logger.info("Removed foreign key constraint FKpjgmxkoy86dyvs5o2bwxhuigu from review_records table");
                        } else {
                            logger.info("Foreign key constraint FKpjgmxkoy86dyvs5o2bwxhuigu does not exist");
                        }

                        // Step 4: Check for any other foreign key constraints referencing highlights table
                        ResultSet foreignKeys = statement.executeQuery(
                                "SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS " +
                                        "WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'review_records' " +
                                        "AND REFERENCED_TABLE_NAME = 'highlights'");

                        while (foreignKeys.next()) {
                            String constraintName = foreignKeys.getString("CONSTRAINT_NAME");
                            try {
                                statement.executeUpdate("ALTER TABLE review_records DROP FOREIGN KEY " + constraintName);
                                logger.info("Removed foreign key constraint {} from review_records table", constraintName);
                            } catch (Exception e) {
                                logger.warn("Error removing foreign key constraint {}: {}", constraintName, e.getMessage());
                            }
                        }
                        foreignKeys.close();

                        // Step 5: Remove the highlight_id column from review_records if it exists
                        try {
                            // Check if the column exists first
                            ResultSet columnCheck = statement.executeQuery(
                                    "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
                                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'review_records' " +
                                            "AND COLUMN_NAME = 'highlight_id'");

                            if (columnCheck.next()) {
                                statement.executeUpdate("ALTER TABLE review_records DROP COLUMN highlight_id");
                                logger.info("Removed highlight_id column from review_records table");
                            }
                            columnCheck.close();
                        } catch (Exception e) {
                            logger.warn("Error removing highlight_id column: {}", e.getMessage());
                        }
                    } else {
                        logger.info("review_records table does not exist");
                    }
                } catch (Exception e) {
                    logger.warn("Error handling review_records table: {}", e.getMessage());
                }

                // Step 6: Handle cards table foreign key constraints
                try {
                    boolean cardsExists = statement.executeQuery("SHOW TABLES LIKE 'cards'")
                            .next();

                    if (cardsExists) {
                        logger.info("cards table exists");

                        // Check for any foreign key constraints in cards table
                        ResultSet cardsForeignKeys = statement.executeQuery(
                                "SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS " +
                                        "WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'cards'"
                        );

                        while (cardsForeignKeys.next()) {
                            String constraintName = cardsForeignKeys.getString("CONSTRAINT_NAME");
                            try {
                                statement.executeUpdate("ALTER TABLE cards DROP FOREIGN KEY " + constraintName);
                                logger.info("Removed foreign key constraint {} from cards table", constraintName);
                            } catch (Exception e) {
                                logger.warn("Error removing foreign key constraint {} from cards: {}", constraintName, e.getMessage());
                            }
                        }
                        cardsForeignKeys.close();
                    } else {
                        logger.info("cards table does not exist");
                    }
                } catch (Exception e) {
                    logger.warn("Error handling cards table: {}", e.getMessage());
                }

                // Step 7: Remove the highlights table if it exists
                try {
                    boolean highlightsExists = statement.executeQuery("SHOW TABLES LIKE 'highlights'")
                            .next();

                    if (highlightsExists) {
                        statement.executeUpdate("DROP TABLE IF EXISTS highlights");
                        logger.info("Removed highlights table");
                    } else {
                        logger.info("highlights table does not exist");
                    }
                } catch (Exception e) {
                    logger.error("Error removing highlights table: {}", e.getMessage());
                }

            } catch (Exception e) {
                logger.error("Error running foreign key removal migration: {}", e.getMessage());
            }

        } catch (ClassNotFoundException e) {
            logger.error("Error loading MySQL driver: {}", e.getMessage());
        }

        logger.info("Foreign key removal migration completed");
    }
}
