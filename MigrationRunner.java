import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MigrationRunner {
    public static void main(String[] args) {
        try {
            // Explicitly load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading MySQL driver: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String url = "jdbc:mysql://sh-cdb-l89atvre.sql.tencentcdb.com:21231/english_learning_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "woaini,1996";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            System.out.println("Running database migration to remove foreign key constraints and cleanup highlights table...");

            // Step 1: Check if review_records table exists
            boolean reviewRecordsExists = statement.executeQuery("SHOW TABLES LIKE 'review_records'")
                    .next();

            if (reviewRecordsExists) {
                System.out.println("review_records table exists");

                // Step 2: Check if the specific foreign key constraint exists
                ResultSet constraintResult = statement.executeQuery(
                        "SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS " +
                                "WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'review_records' " +
                                "AND CONSTRAINT_NAME = 'FKpjgmxkoy86dyvs5o2bwxhuigu'");

                boolean constraintExists = constraintResult.next();
                constraintResult.close();

                if (constraintExists) {
                    // Drop the constraint if it exists
                    statement.executeUpdate("ALTER TABLE review_records DROP FOREIGN KEY FKpjgmxkoy86dyvs5o2bwxhuigu");
                    System.out.println("Removed foreign key constraint FKpjgmxkoy86dyvs5o2bwxhuigu from review_records table");
                } else {
                    System.out.println("Foreign key constraint FKpjgmxkoy86dyvs5o2bwxhuigu does not exist");
                }

                // Step 3: Check for any other foreign key constraints referencing highlights table
                ResultSet foreignKeys = statement.executeQuery(
                        "SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS " +
                                "WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'review_records' " +
                                "AND REFERENCED_TABLE_NAME = 'highlights'");

                while (foreignKeys.next()) {
                    String constraintName = foreignKeys.getString("CONSTRAINT_NAME");
                    try {
                        statement.executeUpdate("ALTER TABLE review_records DROP FOREIGN KEY " + constraintName);
                        System.out.println("Removed foreign key constraint " + constraintName + " from review_records table");
                    } catch (Exception e) {
                        System.out.println("Error removing foreign key constraint " + constraintName + ": " + e.getMessage());
                    }
                }
                foreignKeys.close();

                // Step 4: Remove the highlights column from review_records if it exists
                try {
                    statement.executeUpdate("ALTER TABLE review_records DROP COLUMN IF EXISTS highlight_id");
                    System.out.println("Removed highlight_id column from review_records table");
                } catch (Exception e) {
                    System.out.println("Error removing highlight_id column: " + e.getMessage());
                }
            } else {
                System.out.println("review_records table does not exist");
            }

            // Step 5: Remove the highlights table if it exists
            boolean highlightsExists = statement.executeQuery("SHOW TABLES LIKE 'highlights'")
                    .next();

            if (highlightsExists) {
                statement.executeUpdate("DROP TABLE IF EXISTS highlights");
                System.out.println("Removed highlights table");
            } else {
                System.out.println("highlights table does not exist");
            }

            System.out.println("Database migration completed successfully!");

        } catch (Exception e) {
            System.out.println("Error running database migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
