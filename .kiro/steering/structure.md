---
inclusion: always
---

# Project Structure

## Repository Layout

```
english-review-system/
├── src/main/java/                    # Backend Java source code
├── src/main/resources/               # Backend configuration and resources
├── src/test/java/                    # Backend tests
├── frontend/                         # Vue.js web application
├── miniprogram/                      # WeChat Mini Program
├── uploads/                          # File storage (dev and prod)
├── pom.xml                          # Maven configuration
└── mvnw, mvnw.cmd                   # Maven wrapper scripts
```

## Backend Architecture (Spring Boot)

### Package Structure: `org.example.docvideoplay`

```
docvideoplay/
├── annotation/          # Custom annotations
├── api/                 # API interfaces and DTOs
│   ├── composite/       # Composite API operations
│   ├── inner/           # Internal APIs
│   └── page/            # Pagination APIs
├── aspect/              # AOP aspects
├── config/              # Configuration classes
│   ├── SecurityConfig.java          # Spring Security + JWT
│   ├── JwtUtil.java                 # JWT token utilities
│   ├── FileStorageConfig.java       # File upload configuration
│   ├── DatabaseMigration.java       # Schema migrations
│   └── GlobalExceptionHandler.java  # Centralized error handling
├── constant/            # Application constants
├── controller/          # REST controllers
│   ├── composite/       # Composite endpoints
│   ├── inner/           # Internal endpoints
│   └── page/            # Page-based endpoints
├── dao/                 # Data access layer
│   ├── jpa/             # JPA repositories
│   ├── mybatis/         # MyBatis mappers (if used)
│   └── redis/           # Redis operations (if used)
├── dto/                 # Data Transfer Objects
│   ├── api/             # API request/response DTOs
│   └── feign/           # Feign client DTOs
├── entity/              # JPA entities
│   ├── BaseEntity.java              # Base entity with common fields
│   ├── User.java                    # User entity
│   ├── Card.java                    # Flashcard entity
│   ├── Deck.java                    # Deck entity
│   ├── ReviewRecord.java            # Review history
│   ├── ReviewSession.java           # Review session
│   ├── StudyMaterial.java           # Learning materials
│   ├── Tag.java                     # Tags for organization
│   └── TodoItem.java                # Todo tasks
├── enums/               # Enumerations
│   ├── AnswerQuality.java           # Review quality ratings
│   ├── MaterialType.java            # Material types (DOCUMENT, VIDEO, ARTICLE)
│   └── TodoType.java                # Todo item types
├── feign/               # Feign clients for external services
├── job/                 # Scheduled jobs
├── mq/                  # Message queue handlers
├── repository/          # Spring Data JPA repositories
├── service/             # Business logic layer
│   ├── impl/            # Service implementations
│   ├── CardService.java
│   ├── DeckService.java
│   ├── ReviewService.java
│   ├── SpacedRepetitionService.java # SM-2 algorithm
│   ├── StudyMaterialService.java
│   ├── VocabularyService.java
│   ├── TodoService.java
│   └── NotificationService.java
├── utils/               # Utility classes
└── DocVideoPlayApplication.java     # Main application class
```

### Key Architectural Patterns

- **Layered Architecture**: Controller → Service → Repository → Entity
- **DTO Pattern**: Separate API models from domain entities
- **Repository Pattern**: Spring Data JPA repositories for data access
- **Service Layer**: Business logic encapsulation with interface/implementation separation
- **Global Exception Handling**: Centralized error handling via `@ControllerAdvice`
- **JWT Authentication**: Stateless authentication with JWT tokens
- **File Storage Abstraction**: Configurable file storage paths per environment

## Frontend Architecture (Vue.js)

```
frontend/src/
├── assets/              # Static assets and global styles
│   ├── common.css       # Common styles
│   └── main.css         # Main stylesheet
├── components/          # Reusable Vue components
│   ├── ArticleViewer.vue
│   ├── CardForm.vue
│   ├── CardItem.vue
│   ├── CommentManager.vue
│   ├── DocumentViewer.vue
│   ├── FileUpload.vue
│   ├── LoadingSpinner.vue
│   ├── MaterialCard.vue
│   ├── ModernConfirmDialog.vue
│   ├── NotificationAlert.vue
│   ├── NotificationPanel.vue
│   ├── ReviewProgress.vue
│   ├── ReviewQuestion.vue
│   ├── SessionNavigation.vue
│   ├── TextHighlighter.vue
│   ├── VideoPlayer.vue
│   └── VideoTranscriptHighlighter.vue
├── composables/         # Vue 3 Composition API composables
│   ├── useAnimation.js
│   ├── useApiService.js
│   ├── useBrowserNotifications.js
│   ├── useComponentIntegration.js
│   ├── useErrorHandler.js
│   ├── useFileUpload.js
│   ├── useLoadingState.js
│   ├── useLocalStorage.js
│   ├── useNotification.js
│   └── useSpeechService.js
├── router/              # Vue Router configuration
├── services/            # API service layer
│   ├── deckService.js
│   ├── materialService.js
│   ├── reviewService.js
│   ├── todoService.js
│   └── vocabularyService.js
├── stores/              # Pinia state management
│   └── notificationStore.js
├── utils/               # Utility functions
│   ├── confirmDialog.js
│   └── dataFlowValidator.js
├── views/               # Page-level components (routes)
│   ├── Dashboard.vue
│   ├── Login.vue
│   ├── Register.vue
│   ├── Materials.vue
│   ├── MaterialViewer.vue
│   ├── Decks.vue
│   ├── CardBrowser.vue
│   ├── CardEditor.vue
│   ├── CardTest.vue
│   ├── Review.vue
│   ├── ReviewSession.vue
│   ├── Vocabulary.vue
│   ├── TodoList.vue
│   └── Statistics.vue
├── App.vue              # Root component
└── main.js              # Application entry point
```

### Frontend Patterns

- **Composition API**: Modern Vue 3 composables for reusable logic
- **Service Layer**: Separate API calls from components
- **Pinia Stores**: Centralized state management
- **Component Hierarchy**: Views → Components → Composables
- **Utility Functions**: Shared helpers in utils/

## WeChat Mini Program

```
miniprogram/
├── pages/               # Mini program pages
│   ├── login/
│   ├── register/
│   ├── dashboard/
│   ├── materials/
│   ├── material-viewer/
│   ├── vocabulary/
│   ├── review/
│   ├── review-session/
│   ├── todo/
│   └── decks/
├── app.js               # Application logic
├── app.json             # Configuration and routing
└── app.wxss             # Global styles
```

## File Storage Structure

```
uploads/
├── dev/                 # Development environment
│   ├── documents/       # Uploaded documents
│   ├── videos/          # Uploaded videos
│   └── articles/        # Uploaded articles
└── english-learning/    # Production environment
    ├── documents/
    ├── videos/
    └── articles/
```

## Configuration Files

- `application.yml` - Main Spring Boot configuration
- `application-dev.yml` - Development profile
- `application-prod.yml` - Production profile
- `application-test.yml` - Test profile
- `frontend/.env.development` - Frontend dev environment
- `frontend/.env.production` - Frontend prod environment
- `frontend/vite.config.js` - Vite build configuration
