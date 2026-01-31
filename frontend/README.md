# English Learning System Frontend

## Overview

Vue.js 3 frontend application for the English Learning System, built with the Composition API and modern development tools.

## Technology Stack

- **Vue.js 3.2.31** - Progressive JavaScript framework with Composition API
- **Vue Router 4.0.14** - Official router for Vue.js
- **Pinia 2.0.13** - State management library for Vue
- **Axios 0.27.2** - HTTP client for API communication
- **Element Plus 2.1.11** - Vue 3 UI component library
- **Video.js 7.18.1** - HTML5 video player for video playback
- **Quill 1.3.7** - Rich text editor for highlighting functionality
- **Vite 2.9.5** - Fast build tool and development server

## Project Structure

```
frontend/
├── src/
│   ├── assets/          # Static assets and global styles
│   ├── components/      # Reusable Vue components
│   ├── composables/     # Vue 3 composables for shared logic
│   ├── router/          # Vue Router configuration
│   ├── views/           # Page-level components
│   ├── App.vue          # Root component
│   └── main.js          # Application entry point
├── public/              # Public static files
├── index.html           # HTML template
├── vite.config.js       # Vite configuration
└── package.json         # Dependencies and scripts
```

## Features

- **Responsive Design**: Mobile-first responsive layout
- **Component Library**: Element Plus for consistent UI components
- **Video Playback**: Video.js integration for multimedia content
- **Text Highlighting**: Quill.js for rich text editing and highlighting
- **State Management**: Pinia for centralized state management
- **API Integration**: Axios for backend communication
- **Development Server**: Vite with hot module replacement

## Development Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- **npm run preview** - Preview production build

## Environment Configuration

The application supports environment-specific configuration:

- `.env.development` - Development environment variables
- `.env.production` - Production environment variables

## API Integration

The frontend is configured to proxy API requests to the Spring Boot backend:

- Development: `http://localhost:8080/api`
- Production: `/api` (relative to frontend domain)

## Browser Support

- Modern browsers with ES2015+ support
- Chrome 61+, Firefox 60+, Safari 12+, Edge 79+

## Getting Started

1. Install dependencies: `npm install`
2. Start development server: `npm run dev`
3. Open browser to `http://localhost:3000`

## Notes

This setup is compatible with Node.js 12+ and provides a solid foundation for the English Learning System frontend with all required dependencies and build configuration.