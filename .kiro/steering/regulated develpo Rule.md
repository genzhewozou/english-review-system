# Project Structure

## Package Organization

The project follows a standard Spring Boot layered architecture with domain-driven design principles:

```
xxx/
├── Application.java                 # Main Spring Boot application class
├── annotation/                      # Custom annotations (e.g., @OutApiLog)
├── api/                            # API interface definitions
│   ├── composite/                  # Composite API interfaces
│   ├── inner/                      # Internal service APIs
│   └── page/                       # Page/UI APIs
├── aspect/                         # AOP aspects (logging, etc.)
├── config/                         # Configuration classes
├── constant/                       # Application constants
├── controller/                     # REST controllers (mirrors api structure)
│   ├── composite/
│   ├── inner/
│   └── page/
├── dao/                           # Data access layer
│   ├── jpa/                       # JPA repositories
│   ├── mybatis/                   # MyBatis mappers
│   └── redis/                     # Redis operations
├── dto/                           # Data transfer objects
│   ├── api/                       # API DTOs
│   └── feign/                     # Feign client DTOs
├── dts/                           # Data transformation services
├── entity/                        # JPA entities
├── enums/                         # Enumeration classes
├── feign/                         # Feign clients for external services
├── job/                           # Scheduled jobs (XXL-Job)
├── mq/                            # Message queue consumers/producers
├── service/                       # Business logic layer
│   └── impl/                      # Service implementations
└── util/                          # Utility classes
```

## Domain Organization

## Architectural Patterns

### Layered Architecture
- **Controller Layer**: REST endpoints, request validation
- **Service Layer**: Business logic, transaction management
- **DAO Layer**: Data access with JPA repositories and MyBatis mappers
- **Entity Layer**: JPA entities extending BaseEntity

### API Design Patterns
- **API Interfaces**: Separate interface definitions in `api/` package
- **Controller Implementation**: Controllers implement corresponding API interfaces
- **DTO Pattern**: Separate request/response DTOs for each API layer

### Data Access Patterns
- **JPA Repositories**: For simple CRUD operations
- **MyBatis Mappers**: For complex queries and custom SQL
- **Redis DAOs**: For caching and session management

## Key Conventions

### Naming Conventions
- **Controllers**: `*Controller.java` (implements corresponding API interface)
- **Services**: `*Service.java` interface with `*ServiceImpl.java` implementation
- **Repositories**: `*Repository.java` for JPA, `*Mapper.java` for MyBatis
- **DTOs**: `*ParamsDto.java` for requests, `*ResultDto.java` for responses
- **Entities**: Plain class names matching database table names

### Package Structure Rules
- Controllers mirror the API package structure exactly
- Service implementations go in `impl/` subdirectories
- DTOs are organized by API layer (api/, feign/)
- Entities are grouped by business domain

### Configuration Files
- `src/main/resources/application.yml`: Main application configuration
- `src/main/resources/bootstrap.yml`: Bootstrap configuration for Nacos
- `src/main/resources/com/yilihuo/.../dao/mybatis/`: MyBatis XML mappers
- `src/main/resources/i18n/`: Internationalization messages

## Testing Structure
- Test packages mirror main source structure
- Unit tests for services, controllers, and utilities
- Integration tests for repository layers
- Mock-based testing with Mockito