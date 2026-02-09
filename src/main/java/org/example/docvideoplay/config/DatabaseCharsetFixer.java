package org.example.docvideoplay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ensures MySQL tables/columns used for free-text (like user comments) support UTF-8 (utf8mb4),
 * so users can save Chinese and other non-Latin characters.
 *
 * This is a lightweight, idempotent fixer that only runs ALTER statements when necessary.
 */
@Component
public class DatabaseCharsetFixer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCharsetFixer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCharsetFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Only relevant for MySQL/MariaDB.
            String productName = jdbcTemplate.getDataSource()
                    .getConnection()
                    .getMetaData()
                    .getDatabaseProductName();

            if (productName == null || !productName.toLowerCase().contains("mysql")) {
                return;
            }

            // Fix cards table if needed (common cause: latin1 column collation).
            ensureUtf8mb4ForCardsColumns();
        } catch (Exception e) {
            // Don't block app startup if DB user lacks ALTER permissions.
            logger.warn("Database charset fixer skipped due to error: {}", e.getMessage());
        }
    }

    private void ensureUtf8mb4ForCardsColumns() {
        // Check current collation for key text columns.
        List<String> collations = jdbcTemplate.queryForList(
                "SELECT COLLATION_NAME FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cards' " +
                        "AND COLUMN_NAME IN ('text','context','user_comment')",
                String.class
        );

        boolean needsFix = collations.stream().anyMatch(c ->
                c == null || !c.toLowerCase().contains("utf8mb4")
        );

        if (!needsFix) {
            return;
        }

        logger.info("Fixing MySQL charset/collation for table 'cards' to utf8mb4");

        // Convert whole table to utf8mb4; this updates existing column collations too.
        jdbcTemplate.execute("ALTER TABLE cards CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        // Ensure specific columns are long enough and explicitly utf8mb4.
        jdbcTemplate.execute("ALTER TABLE cards MODIFY text VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL");
        jdbcTemplate.execute("ALTER TABLE cards MODIFY context VARCHAR(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL");
        jdbcTemplate.execute("ALTER TABLE cards MODIFY user_comment VARCHAR(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL");
    }
}

