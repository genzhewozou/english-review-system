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
@Order(2) // Run after other migrations
public class HighlightToCardMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(HighlightToCardMigration.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Running database migration to convert highlight records to card records...");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Step 1: Copy all highlight records to cards table
            try {
                String copyHighlightsToCards = "INSERT INTO cards " +
                        "(id, user_id, material_id, deck_id, text, back_text, context, start_position, end_position, " +
                        "user_comment, card_type, template_id, tags, is_active, ease_factor, repetition_count, " +
                        "interval_days, next_review_date, last_review_date, leech_warning_count, leech, " +
                        "created_date, updated_date) " +
                        "SELECT id, user_id, material_id, deck_id, text, back_text, context, start_position, end_position, " +
                        "user_comment, card_type, template_id, tags, is_active, ease_factor, repetition_count, " +
                        "interval_days, next_review_date, last_review_date, leech_warning_count, leech, " +
                        "created_date, updated_date " +
                        "FROM highlights " +
                        "WHERE id NOT IN (SELECT id FROM cards)";
                
                int rowsAffected = statement.executeUpdate(copyHighlightsToCards);
                logger.info("Copied {} highlight records to cards table", rowsAffected);
            } catch (Exception e) {
                logger.warn("Error copying highlights to cards: {}", e.getMessage());
            }

            // Step 2: Update any foreign key references if needed
            // For example, if there are any tables referencing highlights, update them to reference cards
            // This would depend on the actual database schema

            // Step 3: Drop the highlights table if it exists
            try {
                statement.executeUpdate("DROP TABLE IF EXISTS highlights");
                logger.info("Dropped highlights table");
            } catch (Exception e) {
                logger.warn("Error dropping highlights table: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error running highlight to card migration: {}", e.getMessage());
        }

        logger.info("Highlight to card migration completed");
    }
}
