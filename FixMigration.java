import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class FixMigration {
    public static void main(String[] args) {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded successfully");

            // Connect to database
            String url = "jdbc:mysql://sh-cdb-l89atvre.sql.tencentcdb.com:21231/english_learning_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            String user = "root";
            String password = "woaini,1996";
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database");

            Statement stmt = conn.createStatement();

            // Step 1: Check if the foreign key constraint exists
            ResultSet rs = stmt.executeQuery(
                "SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS " +
                "WHERE CONSTRAINT_SCHEMA = 'english_learning_db' AND TABLE_NAME = 'review_records' " +
                "AND CONSTRAINT_NAME = 'FKpjgmxkoy86dyvs5o2bwxhuigu'");

            if (rs.next()) {
                // Drop the foreign key constraint
                stmt.executeUpdate("ALTER TABLE review_records DROP FOREIGN KEY FKpjgmxkoy86dyvs5o2bwxhuigu");
                System.out.println("Dropped foreign key constraint FKpjgmxkoy86dyvs5o2bwxhuigu");
            } else {
                System.out.println("Foreign key constraint FKpjgmxkoy86dyvs5o2bwxhuigu does not exist");
            }
            rs.close();

            // Step 2: Check for any other foreign keys referencing highlights
            rs = stmt.executeQuery(
                "SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS " +
                "WHERE CONSTRAINT_SCHEMA = 'english_learning_db' AND TABLE_NAME = 'review_records' " +
                "AND REFERENCED_TABLE_NAME = 'highlights'");

            while (rs.next()) {
                String constraintName = rs.getString("CONSTRAINT_NAME");
                stmt.executeUpdate("ALTER TABLE review_records DROP FOREIGN KEY " + constraintName);
                System.out.println("Dropped foreign key constraint " + constraintName);
            }
            rs.close();

            // Step 3: Drop the highlights table
            stmt.executeUpdate("DROP TABLE IF EXISTS highlights");
            System.out.println("Dropped highlights table");

            // Close resources
            stmt.close();
            conn.close();
            System.out.println("Migration completed successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
