---
inclusion: always
---

# Technology Stack

## Backend

- **Framework**: Spring Boot 2.7.18
- **Java Version**: Java 8
- **Build Tool**: Maven 3.x
- **Database**: MySQL 8.0+ (production), H2 (development/testing)
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT authentication
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Testing**: JUnit, jqwik (property-based testing)

### Key Dependencies

- **Document Processing**: Apache POI (Word/Excel), Apache PDFBox (PDF), Apache Tika (text extraction)
- **JWT**: jjwt 0.11.5
- **Database**: MySQL Connector 8.0.33, HikariCP (connection pooling)
- **Validation**: Spring Boot Starter Validation

## Frontend

- **Framework**: Vue.js 3.2 with Composition API
- **Build Tool**: Vite 2.9
- **State Management**: Pinia 2.0
- **Routing**: Vue Router 4.0
- **UI Library**: Element Plus 2.2
- **HTTP Client**: Axios 0.27
- **Video Player**: Video.js 7.20
- **Rich Text**: Quill 1.3.7
- **Code Quality**: ESLint, Prettier

## WeChat Mini Program

- **Platform**: WeChat Mini Program (WXML/WXSS/JS)
- **Pages**: Login, Register, Dashboard, Materials, Vocabulary, Review, Todo, Decks

## Common Commands

### Backend (Maven)

```bash
# Development mode (H2 database)
./mvnw spring-boot:run

# Production mode (MySQL)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Run tests
./mvnw test

# Build JAR
./mvnw clean package

# Skip tests during build
./mvnw clean package -DskipTests

# Compile only
./mvnw compile
```

### Frontend (npm)

```bash
# Install dependencies
npm install

# Development server (with host access)
npm run dev

# Production build
npm run build

# Preview production build
npm run preview

# Lint and fix
npm run lint

# Format code
npm run format
```

## Environment Profiles

- **dev**: Development (H2 in-memory database, verbose logging)
- **prod**: Production (MySQL, optimized settings)
- **test**: Testing (H2, test-specific configuration)

## API Configuration

- **Backend Port**: 2001
- **Frontend Dev Port**: 3000 (Vite default)
- **API Base Path**: `/api`
- **Swagger UI**: `http://localhost:2001/swagger-ui.html`
- **H2 Console**: `http://localhost:2001/api/h2-console` (dev only)

## File Upload Limits

- **Max File Size**: 100MB
- **Max Request Size**: 100MB
- **Allowed Document Types**: pdf, doc, docx, txt, rtf
- **Allowed Video Types**: mp4, avi, mov, wmv, flv
- **Allowed Article Types**: html, htm, md

## Database Connection

- **Development**: H2 in-memory (`jdbc:h2:mem:english_learning_dev`)
- **Production**: MySQL with HikariCP connection pooling
- **JPA DDL**: `none` (manual schema management via migrations)
