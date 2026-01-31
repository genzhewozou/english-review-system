# Implementation Plan: English Learning System

## Overview

This implementation plan converts the English Learning System design into discrete coding tasks following the established Spring Boot project structure. The system will be built incrementally, starting with core data models and progressing through the service layer, API layer, and finally the Vue.js frontend. Each task builds on previous work and includes appropriate testing to ensure correctness.

## Tasks

- [x] 1. Set up project structure and core configuration
  - Create Spring Boot project structure following the regulated development rules
  - Configure application.yml with database and file upload settings
  - Set up mysql database for development
  - Configure file storage directories and upload limits
  - _Requirements: 1.1, 1.2, 7.1_

- [x] 2. Implement core data models and entities
  - [x] 2.1 Create base entity and enumerations
    - Implement BaseEntity with common fields (id, createdDate, updatedDate)
    - Create MaterialType, AnswerQuality, and TodoType enums
    - _Requirements: All requirements (foundational)_

  - [x] 2.2 Write property test for base entity functionality

    - **Property 6: Data Persistence Across Sessions**
    - **Validates: Requirements 2.5**

  - [x] 2.3 Implement StudyMaterial entity
    - Create StudyMaterial entity with file metadata fields
    - Add relationship mappings to Highlight entity
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [x] 2.4 Implement Highlight entity with spaced repetition fields
    - Create Highlight entity with text, context, and position fields
    - Add spaced repetition algorithm fields (easeFactor, intervalDays, etc.)
    - Add relationship mappings to StudyMaterial and ReviewRecord
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 4.1, 4.3, 4.4_

  - [x] 2.5 Implement ReviewSession and ReviewRecord entities
    - Create ReviewSession entity for tracking quiz sessions
    - Create ReviewRecord entity for individual question responses
    - Add proper relationship mappings
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [x] 2.6 Implement TodoItem entity
    - Create TodoItem entity with task management fields
    - Add relationship to Highlight for review reminders
    - _Requirements: 4.2, 5.1, 5.2, 5.3, 5.4_

- [-] 3. Implement data access layer (DAO)
  - [x] 3.1 Create JPA repositories
    - Implement StudyMaterialRepository with custom query methods
    - Implement HighlightRepository with review date queries
    - Implement ReviewSessionRepository and ReviewRecordRepository
    - Implement TodoItemRepository with due date queries
    - _Requirements: 1.3, 4.2, 5.3_

  - [ ]* 3.2 Write property tests for repository operations
    - **Property 2: Material Listing Completeness**
    - **Validates: Requirements 1.3**

- [-] 4. Implement spaced repetition service
  - [x] 4.1 Create SpacedRepetitionService implementation
    - Implement SM-2 algorithm for calculating next review dates
    - Add methods for processing review answers and updating intervals
    - Include the 5-day initial reminder logic
    - _Requirements: 4.1, 4.3, 4.4, 4.5_

  - [ ]* 4.2 Write property tests for spaced repetition algorithm
    - **Property 9: Spaced Repetition Algorithm Correctness**
    - **Validates: Requirements 4.1, 4.3, 4.4**

  - [ ]* 4.3 Write property test for five-day reminder scheduling
    - **Property 10: Five-Day Reminder Scheduling**
    - **Validates: Requirements 4.5**

- [x] 5. Implement core business services
  - [x] 5.1 Create StudyMaterialService implementation
    - Implement file upload handling with validation
    - Add methods for material retrieval and listing
    - Include file storage and metadata management
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [ ]* 5.2 Write property tests for file upload and storage
    - **Property 1: File Upload and Storage Consistency**
    - **Validates: Requirements 1.1, 1.2**

  - [x] 5.3 Create VocabularyService implementation
    - Implement highlight creation with text and context storage
    - Add comment management for highlights
    - Include highlight retrieval by material
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [ ]* 5.4 Write property tests for highlight management
    - **Property 4: Highlight Creation and Storage**
    - **Validates: Requirements 2.1, 2.2**

  - [ ]* 5.5 Write property test for comment functionality
    - **Property 5: Comment Association and Display**
    - **Validates: Requirements 2.3, 2.4, 3.5**

  - [x] 5.6 Create ReviewService implementation
    - Implement review session creation and management
    - Add question generation from highlights
    - Include answer processing and session completion
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [ ]* 5.7 Write property tests for review session functionality
    - **Property 7: Review Session Question Presentation**
    - **Validates: Requirements 3.1, 3.2**

  - [ ]* 5.8 Write property test for answer recording
    - **Property 8: Answer Recording and Processing**
    - **Validates: Requirements 3.3, 3.4**

- [x] 6. Checkpoint - Ensure core services work correctly
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Implement todo list and scheduling services
  - [x] 7.1 Create TodoService implementation
    - Implement todo item creation and management
    - Add automatic scheduling for review reminders
    - Include todo completion and synchronization with reviews
    - _Requirements: 4.2, 5.1, 5.2, 5.3, 5.4_

  - [ ]* 7.2 Write property tests for todo list functionality
    - **Property 11: Todo List Synchronization**
    - **Validates: Requirements 4.2, 5.1, 5.2**

  - [ ]* 7.3 Write property test for todo list display
    - **Property 12: Todo List Display Accuracy**
    - **Validates: Requirements 5.3, 5.4**

  - [x] 7.4 Create NotificationService implementation
    - Implement notification triggering for due reviews
    - Add notification batching for multiple due items
    - Include user preference management
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [ ]* 7.5 Write property tests for notification system
    - **Property 13: Notification System Behavior**
    - **Validates: Requirements 6.1, 6.2, 6.4**

  - [ ]* 7.6 Write property test for notification configuration
    - **Property 14: Notification Configuration Persistence**
    - **Validates: Requirements 6.3**

- [x] 8. Implement REST API layer
  - [x] 8.1 Create API interface definitions
    - Define StudyMaterialApi interface with upload and retrieval endpoints
    - Define VocabularyApi interface for highlight management
    - Define ReviewApi interface for quiz functionality
    - Define TodoApi interface for task management
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.3, 3.1, 5.3_

  - [x] 8.2 Implement StudyMaterialController
    - Implement file upload endpoint with multipart handling
    - Add material listing and retrieval endpoints
    - Include proper error handling and validation
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [ ]* 8.3 Write property test for material access
    - **Property 3: Material Access Functionality**
    - **Validates: Requirements 1.4**

  - [x] 8.4 Implement VocabularyController
    - Implement highlight creation and update endpoints
    - Add highlight retrieval by material endpoints
    - Include comment management endpoints
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 8.5 Implement ReviewController
    - Implement review session management endpoints
    - Add question retrieval and answer submission endpoints
    - Include session completion and results endpoints
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [x] 8.6 Implement TodoController
    - Implement todo item management endpoints
    - Add todo completion and listing endpoints
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 9. Checkpoint - Ensure API layer works correctly
  - Ensure all tests pass, ask the user if questions arise.

- [x] 10. Set up Vue.js frontend project
  - [x] 10.1 Initialize Vue.js project with required dependencies
    - Create Vue 3 project with Composition API
    - Install Vue Router, Axios, and UI component libraries
    - Set up project structure and build configuration
    - _Requirements: 7.1, 7.2_

  - [x] 10.2 Create main application layout and routing
    - Implement main App.vue with navigation structure
    - Set up Vue Router with routes for all major sections
    - Create responsive layout components
    - _Requirements: 7.1, 7.2, 7.3, 7.5_

  - [ ]* 10.3 Write property test for interface responsiveness
    - **Property 15: Interface Responsiveness and Navigation**
    - **Validates: Requirements 7.1, 7.3, 7.5**

- [x] 11. Implement file upload and material management frontend
  - [x] 11.1 Create file upload component
    - Implement drag-and-drop file upload interface
    - Add file type validation and progress indicators
    - Include preview functionality for uploaded files
    - _Requirements: 1.1, 1.2_

  - [x] 11.2 Create material list and viewer components
    - Implement material listing with search and filtering
    - Create material viewer with highlighting capabilities
    - Add material selection and navigation
    - _Requirements: 1.3, 1.4, 2.1_

  - [x] 11.3 Integrate video player functionality
    - Add Video.js integration for video playback
    - Implement video format support (MP4, AVI, MOV)
    - Include graceful fallback for unsupported formats
    - _Requirements: 8.1, 8.4, 8.5_

  - [ ]* 11.4 Write property test for video handling
    - **Property 16: Video Handling and Playback**
    - **Validates: Requirements 8.1, 8.4, 8.5**

- [x] 12. Implement highlighting and annotation frontend
  - [x] 12.1 Create text highlighting component
    - Implement text selection and highlight creation
    - Add highlight visualization and interaction
    - Include context preservation and display
    - _Requirements: 2.1, 2.2_

  - [x] 12.2 Create comment management interface
    - Implement comment addition and editing for highlights
    - Add comment display in highlight tooltips
    - Include comment persistence and retrieval
    - _Requirements: 2.3, 2.4_

  - [x] 12.3 Implement video transcript highlighting
    - Add transcript display for videos
    - Implement timestamp-linked highlighting
    - Include transcript synchronization with video playback
    - _Requirements: 8.2, 8.3_

  - [ ]* 12.4 Write property test for video transcript highlighting
    - **Property 17: Video Transcript Highlighting**
    - **Validates: Requirements 8.2, 8.3**

- [x] 13. Implement review and quiz frontend
  - [x] 13.1 Create review session interface
    - Implement review session start and management
    - Add question display with highlight context
    - Include answer quality selection interface
    - _Requirements: 3.1, 3.2, 3.3_

  - [x] 13.2 Create quiz progress and completion interface
    - Implement progress tracking during reviews
    - Add session completion and results display
    - Include navigation between questions
    - _Requirements: 3.4, 3.5_

- [x] 14. Implement todo list and notification frontend
  - [x] 14.1 Create todo list interface
    - Implement todo item display with due dates
    - Add todo completion and management
    - Include custom task creation
    - _Requirements: 5.3, 5.4_

  - [x] 14.2 Create notification and alert system
    - Implement notification display for due reviews
    - Add overdue alert highlighting
    - Include notification preference settings
    - _Requirements: 6.1, 6.2, 6.3_

- [x] 15. Integration and final testing
  - [x] 15.1 Wire all components together
    - Connect frontend components to backend APIs
    - Implement proper error handling and loading states
    - Add data flow validation between components
    - _Requirements: All requirements_

  - [x] 15.2 Write integration tests for complete workflows
    - Test complete user workflows from upload to review
    - Validate data consistency across the entire system
    - _Requirements: All requirements_

- [x] 16. Final checkpoint - Complete system validation
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP development
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation and user feedback
- Property tests validate universal correctness properties from the design
- Unit tests validate specific examples and edge cases
- The implementation follows the established Spring Boot project structure guidelines