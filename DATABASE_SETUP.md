# Database Setup Instructions

## Development Setup (H2 Database)

For development, the application uses an in-memory H2 database by default. No additional setup is required.

- **Profile**: `dev` (default)
- **Database**: H2 in-memory
- **Console**: Available at `http://localhost:8080/api/h2-console`
- **JDBC URL**: `jdbc:h2:mem:english_learning_dev`
- **Username**: `sa`
- **Password**: (empty)

## Production Setup (MySQL Database)

### Prerequisites

1. Install MySQL 8.0 or later
2. Ensure MySQL service is running

### Database Setup

1. **Run the setup script**:
   ```bash
   mysql -u root -p < src/main/resources/db/mysql-setup.sql
   ```

2. **Or manually create the database**:
   ```sql
   CREATE DATABASE english_learning_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

### Application Configuration

1. **Set the active profile to production**:
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   ```

2. **Configure database credentials** (optional, uses environment variables):
   ```bash
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   ```

3. **Or update application-prod.yml** with your database credentials.

### Running the Application

**Development mode**:
```bash
./mvnw spring-boot:run
```

**Production mode**:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## File Storage Setup

The application will automatically create the necessary directories:

- **Development**: `./uploads/dev/`
- **Production**: `/var/uploads/english-learning/`

Ensure the application has write permissions to these directories.

## Verification

1. Start the application
2. Check the logs for successful database connection
3. Verify file storage directories are created
4. Access the application at `http://localhost:8080/api`

## Troubleshooting

### Common Issues

1. **Database connection failed**:
   - Verify MySQL is running
   - Check credentials in configuration
   - Ensure database exists

2. **File upload issues**:
   - Check directory permissions
   - Verify disk space availability
   - Check file size limits in configuration

3. **H2 Console not accessible**:
   - Ensure development profile is active
   - Check if port 8080 is available
   - Verify context path `/api`