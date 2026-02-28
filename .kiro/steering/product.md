---
inclusion: always
---

# Product Overview

English Learning System - A comprehensive spaced repetition learning platform for English language acquisition.

## Core Features

- **Study Materials Management**: Upload and manage documents (PDF, DOCX, TXT), videos (MP4, AVI, MOV), and articles (HTML, MD) for learning
- **Flashcard System**: Create and organize flashcards in decks with customizable templates
- **Spaced Repetition**: Intelligent review scheduling using SM-2 algorithm with configurable intervals
- **Vocabulary Management**: Track and review vocabulary with context from study materials
- **Review Sessions**: Interactive review sessions with quality-based feedback (Again, Hard, Good, Easy)
- **Todo System**: Task management for learning activities
- **Notifications**: Reminder system for scheduled reviews
- **Multi-Platform**: Web frontend (Vue.js), WeChat Mini Program, and REST API

## Domain Concepts

- **Card**: Flashcard with front/back content, associated with a deck
- **Deck**: Collection of related cards for organized study
- **Review Record**: Individual review attempt with quality rating and timing
- **Review Session**: Group of cards reviewed together
- **Study Material**: Learning content (document, video, article) with text extraction
- **Spaced Repetition**: Algorithm that schedules reviews based on performance (ease factor, interval)
- **Tag**: Categorization system for cards and materials

## User Workflow

1. Upload study materials (documents/videos/articles)
2. Create flashcards from materials or manually
3. Organize cards into decks
4. Review cards according to spaced repetition schedule
5. Rate review quality to adjust future scheduling
6. Track progress through statistics and todo items
