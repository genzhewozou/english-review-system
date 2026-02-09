<template>
  <div id="app" :class="{ 'dark-mode': isDarkMode }">
    <nav class="navbar">
      <div class="nav-brand">
        <router-link to="/" class="brand-link" aria-label="English Learning System Home">
          <span class="brand-icon">📚</span>
          <span class="brand-text">English Mastery</span>
        </router-link>
      </div>
      
      <!-- Desktop Navigation -->
      <div class="nav-container">
        <div class="nav-links desktop-nav">
          <template v-if="isAuthenticated">
            <router-link to="/" class="nav-link" active-class="nav-link-active" aria-label="Dashboard">
              <span class="nav-icon">🏠</span>
              <span class="nav-text">Dashboard</span>
            </router-link>
            <router-link to="/materials" class="nav-link" active-class="nav-link-active" aria-label="Study Materials">
              <span class="nav-icon">📄</span>
              <span class="nav-text">Materials</span>
            </router-link>
            <router-link to="/vocabulary" class="nav-link" active-class="nav-link-active" aria-label="Vocabulary Management">
              <span class="nav-icon">📝</span>
              <span class="nav-text">Vocabulary</span>
            </router-link>
            <router-link to="/decks" class="nav-link" active-class="nav-link-active" aria-label="Deck Management">
              <span class="nav-icon">🃏</span>
              <span class="nav-text">Decks</span>
            </router-link>
            <router-link to="/review" class="nav-link" active-class="nav-link-active" aria-label="Review Sessions">
              <span class="nav-icon">🔄</span>
              <span class="nav-text">Review</span>
            </router-link>
            <router-link to="/todo" class="nav-link" active-class="nav-link-active" aria-label="Todo List">
              <span class="nav-icon">✅</span>
              <span class="nav-text">Tasks</span>
            </router-link>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-link" active-class="nav-link-active" aria-label="Login">
              <span class="nav-text">Login</span>
            </router-link>
            <router-link to="/register" class="nav-link" active-class="nav-link-active" aria-label="Register">
              <span class="nav-text">Register</span>
            </router-link>
          </template>
        </div>
        
        <!-- Navigation Actions -->
        <div class="nav-actions">
          <button class="btn-theme-toggle" @click="toggleTheme" aria-label="Toggle theme">
            {{ isDarkMode ? '☀️' : '🌙' }}
          </button>
          <template v-if="isAuthenticated">
            <div class="notification-wrapper">
              <NotificationPanel />
            </div>
            <button class="btn-logout" @click="handleLogout" aria-label="Logout">
              <span class="nav-icon">🚪</span>
              <span class="nav-text">Logout</span>
            </button>
          </template>
        </div>
      </div>
      
      <!-- Mobile Navigation Toggle -->
      <div class="mobile-nav-toggle">
        <button @click="isMobileMenuOpen = !isMobileMenuOpen" class="menu-toggle-btn" aria-label="Toggle menu">
          <span v-if="!isMobileMenuOpen">☰</span>
          <span v-else>✕</span>
        </button>
      </div>
    </nav>
    
    <!-- Mobile Navigation Menu -->
    <div v-if="isMobileMenuOpen" class="mobile-nav-menu">
      <div class="mobile-nav-content">
        <template v-if="isAuthenticated">
          <router-link to="/" class="mobile-nav-link" @click="isMobileMenuOpen = false" aria-label="Dashboard">
            <span class="nav-icon">🏠</span>
            <span class="nav-text">Dashboard</span>
          </router-link>
          <router-link to="/materials" class="mobile-nav-link" @click="isMobileMenuOpen = false" aria-label="Study Materials">
            <span class="nav-icon">📄</span>
            <span class="nav-text">Materials</span>
          </router-link>
          <router-link to="/vocabulary" class="mobile-nav-link" @click="isMobileMenuOpen = false" aria-label="Vocabulary Management">
            <span class="nav-icon">📝</span>
            <span class="nav-text">Vocabulary</span>
          </router-link>
          <router-link to="/decks" class="mobile-nav-link" @click="isMobileMenuOpen = false" aria-label="Deck Management">
            <span class="nav-icon">🃏</span>
            <span class="nav-text">Decks</span>
          </router-link>
          <router-link to="/review" class="mobile-nav-link" @click="isMobileMenuOpen = false" aria-label="Review Sessions">
            <span class="nav-icon">🔄</span>
            <span class="nav-text">Review</span>
          </router-link>
          <router-link to="/todo" class="mobile-nav-link" @click="isMobileMenuOpen = false" aria-label="Todo List">
            <span class="nav-icon">✅</span>
            <span class="nav-text">Tasks</span>
          </router-link>
          <div class="mobile-nav-actions">
            <button class="btn-theme-toggle" @click="toggleTheme" aria-label="Toggle theme">
              {{ isDarkMode ? '☀️ Light Mode' : '🌙 Dark Mode' }}
            </button>
            <button class="btn-logout" @click="handleLogout" aria-label="Logout">
              <span class="nav-icon">🚪</span>
              <span class="nav-text">Logout</span>
            </button>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="mobile-nav-link" @click="isMobileMenuOpen = false" aria-label="Login">
            <span class="nav-text">Login</span>
          </router-link>
          <router-link to="/register" class="mobile-nav-link" @click="isMobileMenuOpen = false" aria-label="Register">
            <span class="nav-text">Register</span>
          </router-link>
          <div class="mobile-nav-actions">
            <button class="btn-theme-toggle" @click="toggleTheme" aria-label="Toggle theme">
              {{ isDarkMode ? '☀️ Light Mode' : '🌙 Dark Mode' }}
            </button>
          </div>
        </template>
      </div>
    </div>
    
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script>
import NotificationPanel from './components/NotificationPanel.vue'

export default {
  name: 'App',
  components: {
    NotificationPanel
  },
  data() {
    return {
      isAuthenticated: false,
      speechTestResult: '',
      isDarkMode: false,
      isMobileMenuOpen: false
    }
  },
  created() {
    this.checkAuthStatus();
    this.loadThemePreference();
  },
  methods: {
    checkAuthStatus() {
      this.isAuthenticated = localStorage.getItem('isAuthenticated') === 'true';
    },
    handleLogout() {
      localStorage.removeItem('isAuthenticated');
      localStorage.removeItem('user');
      this.isAuthenticated = false;
      this.$router.push('/login');
    },
    loadThemePreference() {
      const savedTheme = localStorage.getItem('darkMode');
      this.isDarkMode = savedTheme ? savedTheme === 'true' : window.matchMedia('(prefers-color-scheme: dark)').matches;
    },
    toggleTheme() {
      this.isDarkMode = !this.isDarkMode;
      localStorage.setItem('darkMode', this.isDarkMode);
    },
    testSpeech() {
      console.log('Testing speech synthesis...');
      this.speechTestResult = 'Testing speech synthesis...';
      
      if ('speechSynthesis' in window) {
        console.log('Web Speech API is available');
        
        try {
          // Test with a simple utterance
          const testUtterance = new SpeechSynthesisUtterance('Hello, this is a test of the speech synthesis system.');
          testUtterance.volume = 1.0;
          testUtterance.rate = 1.0;
          testUtterance.lang = 'en-US';

          testUtterance.onend = () => {
            console.log('Speech test passed');
            this.speechTestResult = 'Speech synthesis is working!';
          };
          testUtterance.onerror = (event) => {
            console.error('Speech test failed:', event.error);
            this.speechTestResult = 'Speech synthesis failed: ' + event.error;
          };

          // Cancel any ongoing speech and test
          speechSynthesis.cancel();
          speechSynthesis.speak(testUtterance);
          console.log('Speech test started');
          
        } catch (err) {
          console.error('Error in speech test:', err);
          this.speechTestResult = 'Error: ' + err.message;
        }
      } else {
        console.error('Web Speech API not supported');
        this.speechTestResult = 'Web Speech API not supported in this browser';
      }
    }
  },
  watch: {
    '$route': 'checkAuthStatus'
  }
}
</script>

<style>
/* Comprehensive design system for professional learning platform */
:root {
  /* ========== COLOR SYSTEM ========== */
  
  /* Primary colors - conveying trust, professionalism, and academic credibility */
  --primary-50: #f0f7ff;
  --primary-100: #e0f0ff;
  --primary-200: #b9e0ff;
  --primary-300: #7cc0ff;
  --primary-400: #3b99ff;
  --primary-500: #2c5aa0;
  --primary-600: #1e427a;
  --primary-700: #14305a;
  --primary-800: #0f2443;
  --primary-900: #0a1a32;
  
  /* Secondary colors - for growth and achievement */
  --secondary-50: #f0fdf4;
  --secondary-100: #dcfce7;
  --secondary-200: #bbf7d0;
  --secondary-300: #86efac;
  --secondary-400: #4ade80;
  --secondary-500: #4a9f7b;
  --secondary-600: #338061;
  --secondary-700: #226649;
  --secondary-800: #165339;
  --secondary-900: #0f402c;
  
  /* Accent colors - for energy and focus */
  --accent-50: #fffbeb;
  --accent-100: #fef3c7;
  --accent-200: #fde68a;
  --accent-300: #fcd34d;
  --accent-400: #fbbf24;
  --accent-500: #f46a55;
  --accent-600: #e24a35;
  --accent-700: #c83121;
  --accent-800: #a72918;
  --accent-900: #8c2215;
  
  /* Neutral colors - for text and backgrounds */
  --neutral-50: #f9fafb;
  --neutral-100: #f3f4f6;
  --neutral-200: #e5e7eb;
  --neutral-300: #d1d5db;
  --neutral-400: #9ca3af;
  --neutral-500: #6b7280;
  --neutral-600: #4b5563;
  --neutral-700: #374151;
  --neutral-800: #1f2937;
  --neutral-900: #111827;
  
  /* Status colors */
  --success-50: #f0fdf4;
  --success-100: #dcfce7;
  --success-200: #bbf7d0;
  --success-300: #86efac;
  --success-400: #4ade80;
  --success-500: #22c55e;
  --success-600: #16a34a;
  --success-700: #15803d;
  --success-800: #166534;
  --success-900: #14532d;
  
  --warning-50: #fffbeb;
  --warning-100: #fef3c7;
  --warning-200: #fde68a;
  --warning-300: #fcd34d;
  --warning-400: #fbbf24;
  --warning-500: #f59e0b;
  --warning-600: #d97706;
  --warning-700: #b45309;
  --warning-800: #92400e;
  --warning-900: #78350f;
  
  --error-50: #fef2f2;
  --error-100: #fee2e2;
  --error-200: #fecaca;
  --error-300: #fca5a5;
  --error-400: #f87171;
  --error-500: #ef4444;
  --error-600: #dc2626;
  --error-700: #b91c1c;
  --error-800: #991b1b;
  --error-900: #7f1d1d;
  
  --info-50: #eff6ff;
  --info-100: #dbeafe;
  --info-200: #bfdbfe;
  --info-300: #93c5fd;
  --info-400: #60a5fa;
  --info-500: #3b82f6;
  --info-600: #2563eb;
  --info-700: #1d4ed8;
  --info-800: #1e40af;
  --info-900: #1e3a8a;
  
  /* Text colors */
  --text-primary: var(--neutral-900);
  --text-secondary: var(--neutral-700);
  --text-tertiary: var(--neutral-500);
  --text-light: var(--neutral-400);
  --text-inverted: var(--neutral-50);
  
  /* Background colors */
  --bg-primary: #ffffff;
  --bg-secondary: var(--neutral-50);
  --bg-tertiary: var(--neutral-100);
  --bg-quaternary: var(--neutral-200);
  --bg-overlay: rgba(0, 0, 0, 0.5);
  
  /* Surface colors */
  --surface-primary: #ffffff;
  --surface-secondary: var(--neutral-50);
  --surface-tertiary: var(--neutral-100);
  --surface-border: var(--neutral-200);
  --surface-shadow: var(--neutral-900);
  
  /* ========== TYPOGRAPHY SYSTEM ========== */
  
  /* Font families */
  --font-sans: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  --font-serif: 'Merriweather', Georgia, Cambria, 'Times New Roman', Times, serif;
  --font-mono: 'Fira Code', 'SF Mono', Menlo, Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
  
  /* Font sizes */
  --text-xs: 0.75rem;    /* 12px */
  --text-sm: 0.875rem;   /* 14px */
  --text-base: 1rem;     /* 16px */
  --text-lg: 1.125rem;   /* 18px */
  --text-xl: 1.25rem;    /* 20px */
  --text-2xl: 1.5rem;    /* 24px */
  --text-3xl: 1.875rem;  /* 30px */
  --text-4xl: 2.25rem;   /* 36px */
  --text-5xl: 3rem;      /* 48px */
  --text-6xl: 3.75rem;   /* 60px */
  --text-7xl: 4.5rem;    /* 72px */
  
  /* Font weights */
  --font-light: 300;
  --font-normal: 400;
  --font-medium: 500;
  --font-semibold: 600;
  --font-bold: 700;
  --font-extrabold: 800;
  --font-black: 900;
  
  /* Line heights */
  --leading-none: 1;
  --leading-tight: 1.25;
  --leading-snug: 1.375;
  --leading-normal: 1.5;
  --leading-relaxed: 1.625;
  --leading-loose: 2;
  
  /* Letter spacing */
  --tracking-tighter: -0.05em;
  --tracking-tight: -0.025em;
  --tracking-normal: 0em;
  --tracking-wide: 0.025em;
  --tracking-wider: 0.05em;
  --tracking-widest: 0.1em;
  
  /* ========== SPACING SYSTEM ========== */
  
  /* Spacing utilities */
  --space-1: 0.25rem;    /* 4px */
  --space-2: 0.5rem;     /* 8px */
  --space-3: 0.75rem;    /* 12px */
  --space-4: 1rem;       /* 16px */
  --space-5: 1.25rem;    /* 20px */
  --space-6: 1.5rem;     /* 24px */
  --space-8: 2rem;       /* 32px */
  --space-10: 2.5rem;    /* 40px */
  --space-12: 3rem;      /* 48px */
  --space-16: 4rem;      /* 64px */
  --space-20: 5rem;      /* 80px */
  --space-24: 6rem;      /* 96px */
  --space-32: 8rem;      /* 128px */
  
  /* Container widths */
  --container-sm: 640px;
  --container-md: 768px;
  --container-lg: 1024px;
  --container-xl: 1280px;
  --container-2xl: 1440px;
  
  /* ========== BORDER AND SHADOW SYSTEM ========== */
  
  /* Border radius */
  --radius-none: 0;
  --radius-sm: 0.125rem;  /* 2px */
  --radius-md: 0.375rem;  /* 6px */
  --radius-lg: 0.5rem;    /* 8px */
  --radius-xl: 0.75rem;   /* 12px */
  --radius-2xl: 1rem;     /* 16px */
  --radius-3xl: 1.5rem;   /* 24px */
  --radius-full: 9999px;
  
  /* Shadows */
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  --shadow-2xl: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  --shadow-inner: inset 0 2px 4px 0 rgba(0, 0, 0, 0.06);
  
  /* ========== TRANSITION SYSTEM ========== */
  
  /* Transition durations */
  --transition-fast: 0.15s;
  --transition-normal: 0.25s;
  --transition-slow: 0.35s;
  
  /* Transition timing functions */
  --transition-linear: linear;
  --transition-ease-in: cubic-bezier(0.4, 0, 1, 1);
  --transition-ease-out: cubic-bezier(0, 0, 0.2, 1);
  --transition-ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);
  
  /* ========== Z-INDEX SYSTEM ========== */
  
  --z-dropdown: 1000;
  --z-sticky: 1020;
  --z-fixed: 1030;
  --z-modal-backdrop: 1040;
  --z-modal: 1050;
  --z-popover: 1060;
  --z-tooltip: 1070;
  --z-toast: 1080;
  --z-notification: 1090;
}

/* Dark mode variables */
.dark-mode {
  /* Text colors */
  --text-primary: var(--neutral-50);
  --text-secondary: var(--neutral-200);
  --text-tertiary: var(--neutral-400);
  --text-light: var(--neutral-500);
  
  /* Background colors */
  --bg-primary: #0f172a;
  --bg-secondary: #1e293b;
  --bg-tertiary: #334155;
  --bg-quaternary: #475569;
  --bg-overlay: rgba(0, 0, 0, 0.7);
  
  /* Surface colors */
  --surface-primary: #1e293b;
  --surface-secondary: #334155;
  --surface-tertiary: #475569;
  --surface-border: #475569;
  --surface-shadow: rgba(0, 0, 0, 0.5);
  
  /* Shadows */
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.3);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.4), 0 2px 4px -1px rgba(0, 0, 0, 0.3);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.5), 0 4px 6px -2px rgba(0, 0, 0, 0.4);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.6), 0 10px 10px -5px rgba(0, 0, 0, 0.5);
  --shadow-2xl: 0 25px 50px -12px rgba(0, 0, 0, 0.75);
  --shadow-inner: inset 0 2px 4px 0 rgba(0, 0, 0, 0.3);
}

/* Global reset and base styles */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

#app {
  font-family: var(--font-sans);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--text-primary);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-secondary);
  line-height: var(--leading-normal);
  font-weight: var(--font-normal);
  font-size: var(--text-base);
}

/* Navigation bar */
.navbar {
  background-color: var(--surface-primary);
  color: var(--text-primary);
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-md);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  border-bottom: 1px solid var(--surface-border);
  min-height: 72px;
}

.navbar:hover {
  box-shadow: var(--shadow-lg);
}

.nav-brand {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  color: var(--primary-600);
  font-weight: var(--font-bold);
  font-size: 1.25rem;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  padding: 0.5rem 1rem;
  border-radius: var(--radius-xl);
  background-color: transparent;
  flex-shrink: 0;
}

.brand-link:hover {
  background-color: var(--primary-50);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.brand-icon {
  font-size: 1.5rem;
}

.brand-text {
  white-space: nowrap;
  font-family: var(--font-sans);
  letter-spacing: var(--tracking-tight);
  font-size: 1.35rem;
  background: linear-gradient(135deg, var(--primary-600), var(--secondary-600));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Navigation Container */
.nav-container {
  display: flex;
  align-items: center;
  gap: 2rem;
  flex: 1;
  justify-content: center;
}

/* Desktop Navigation */
.desktop-nav {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  flex: 1;
  justify-content: center;
}

.nav-links {
  display: flex;
  align-items: center;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  text-decoration: none;
  padding: 0.75rem 1.25rem;
  border-radius: var(--radius-xl);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  font-weight: var(--font-medium);
  position: relative;
  overflow: hidden;
  white-space: nowrap;
  font-size: var(--text-sm);
  letter-spacing: var(--tracking-wide);
  flex-shrink: 0;
}

.nav-link:hover,
.nav-link-active {
  color: var(--primary-600);
  background-color: var(--primary-50);
  box-shadow: var(--shadow-sm);
}

.nav-link:hover::after,
.nav-link-active::after {
  width: 100%;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background-color: var(--primary-500);
  transition: width var(--transition-normal) var(--transition-ease-in-out);
}

.nav-icon {
  font-size: 1.1rem;
}

.nav-text {
  font-size: var(--text-sm);
}

/* Navigation Actions */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-shrink: 0;
}

.notification-wrapper {
  position: relative;
}

/* Mobile Navigation */
.mobile-nav-toggle {
  display: none;
  flex-shrink: 0;
}

.menu-toggle-btn {
  background-color: transparent;
  border: 1px solid var(--surface-border);
  color: var(--text-secondary);
  font-size: 1.25rem;
  padding: 0.5rem;
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.menu-toggle-btn:hover {
  background-color: var(--primary-600);
  color: white;
  border-color: var(--primary-600);
  box-shadow: var(--shadow-sm);
}

.mobile-nav-menu {
  position: fixed;
  top: 72px;
  left: 0;
  right: 0;
  background-color: var(--surface-primary);
  box-shadow: var(--shadow-lg);
  z-index: var(--z-fixed);
  animation: slideDown var(--transition-normal) var(--transition-ease-out);
  border-bottom: 1px solid var(--surface-border);
  max-height: calc(100vh - 72px);
  overflow-y: auto;
}

.mobile-nav-content {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: var(--radius-xl);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  font-weight: var(--font-medium);
}

.mobile-nav-link:hover {
  background-color: var(--primary-50);
  color: var(--primary-600);
  transform: translateX(4px);
}

.mobile-nav-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--surface-border);
}

.mobile-nav-actions .btn-theme-toggle {
  width: 100%;
  border-radius: var(--radius-xl);
  padding: 0.75rem;
  justify-content: center;
  gap: 0.5rem;
}

.mobile-nav-actions .btn-logout {
  width: 100%;
  justify-content: center;
}

/* Button styles */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: var(--radius-xl);
  font-weight: var(--font-medium);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  font-size: var(--text-sm);
  border: none;
  cursor: pointer;
  text-decoration: none;
  font-family: var(--font-sans);
  letter-spacing: var(--tracking-wide);
  position: relative;
  overflow: hidden;
  user-select: none;
}

.btn-primary {
  background-color: var(--primary-600);
  color: white;
}

.btn-primary:hover {
  background-color: var(--primary-700);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-primary:active {
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

.btn-secondary {
  background-color: var(--secondary-600);
  color: white;
}

.btn-secondary:hover {
  background-color: var(--secondary-700);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-secondary:active {
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

.btn-outline {
  background-color: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--surface-border);
}

.btn-outline:hover {
  background-color: var(--bg-tertiary);
  border-color: var(--primary-400);
  color: var(--primary-600);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-outline:active {
  transform: translateY(0);
  box-shadow: none;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: var(--text-xs);
  border-radius: var(--radius-lg);
}

.btn-lg {
  padding: 1rem 2rem;
  font-size: var(--text-base);
  border-radius: var(--radius-2xl);
}

.btn-theme-toggle {
  background-color: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--surface-border);
  padding: 0.5rem;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.btn-theme-toggle:hover {
  background-color: var(--primary-600);
  color: white;
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-600);
}

.btn-logout {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: transparent;
  color: var(--accent-600);
  border: 1px solid var(--accent-600);
  padding: 0.5rem 1rem;
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
}

.btn-logout:hover {
  background-color: var(--accent-600);
  color: white;
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-logout:active {
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

/* Mobile Navigation */
.mobile-nav-toggle {
  display: none;
}

.menu-toggle-btn {
  background-color: transparent;
  border: 1px solid var(--surface-border);
  color: var(--text-secondary);
  font-size: 1.25rem;
  padding: 0.5rem;
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.menu-toggle-btn:hover {
  background-color: var(--primary-600);
  color: white;
  border-color: var(--primary-600);
  box-shadow: var(--shadow-sm);
}

.mobile-nav-menu {
  position: fixed;
  top: 72px;
  left: 0;
  right: 0;
  background-color: var(--surface-primary);
  box-shadow: var(--shadow-lg);
  z-index: var(--z-fixed);
  animation: slideDown var(--transition-normal) var(--transition-ease-out);
  border-bottom: 1px solid var(--surface-border);
}

.mobile-nav-content {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: var(--radius-xl);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  font-weight: var(--font-medium);
}

.mobile-nav-link:hover {
  background-color: var(--primary-50);
  color: var(--primary-600);
  transform: translateX(4px);
}

.mobile-nav-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--surface-border);
}

.mobile-nav-actions .btn-theme-toggle {
  width: 100%;
  border-radius: var(--radius-xl);
  padding: 0.75rem;
  justify-content: center;
  gap: 0.5rem;
}

.mobile-nav-actions .btn-logout {
  width: 100%;
  justify-content: center;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in {
  animation: fadeIn var(--transition-slow) var(--transition-ease-out) forwards;
}

/* Main content */
.main-content {
  flex: 1;
  padding: 2rem;
  max-width: var(--container-2xl);
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
  animation: fadeIn var(--transition-normal) var(--transition-ease-out);
}

/* Card styles */
.card {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-sm);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  border: 1px solid var(--surface-border);
  position: relative;
  overflow: hidden;
}

.card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--primary-200);
}

.card:focus-within {
  border-color: var(--primary-400);
  box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
}

/* Loading spinner */
.loading {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s var(--transition-ease-in-out) infinite;
}

.loading-dark {
  border-color: rgba(0, 0, 0, 0.1);
  border-top-color: var(--primary-600);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Form elements */
.form-control {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid var(--surface-border);
  border-radius: var(--radius-xl);
  font-size: var(--text-base);
  font-family: var(--font-sans);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  background-color: var(--surface-primary);
  color: var(--text-primary);
}

.form-control:focus {
  outline: none;
  border-color: var(--primary-400);
  box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
  background-color: var(--surface-primary);
}

.form-control::placeholder {
  color: var(--text-light);
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  font-size: var(--text-sm);
  letter-spacing: var(--tracking-wide);
}

.form-group {
  margin-bottom: 1.5rem;
}

/* Badge styles */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
}

.badge-primary {
  background-color: var(--primary-100);
  color: var(--primary-700);
}

.badge-secondary {
  background-color: var(--secondary-100);
  color: var(--secondary-700);
}

.badge-success {
  background-color: var(--success-100);
  color: var(--success-700);
}

.badge-warning {
  background-color: var(--warning-100);
  color: var(--warning-700);
}

.badge-error {
  background-color: var(--error-100);
  color: var(--error-700);
}

/* Responsive Design */
@media (max-width: 1200px) {
  .nav-link {
    padding: 0.6rem 1rem;
  }
  
  .nav-text {
    font-size: var(--text-sm);
  }
  
  .nav-container {
    gap: 1rem;
  }
}

@media (max-width: 1024px) {
  .nav-brand {
    flex-shrink: 0;
  }
  
  .brand-text {
    font-size: 1.1rem;
  }
  
  .nav-link {
    padding: 0.5rem 0.75rem;
  }
  
  .nav-text {
    font-size: var(--text-xs);
  }
  
  .nav-icon {
    font-size: 1rem;
  }
  
  .nav-actions {
    gap: 0.75rem;
  }
  
  .main-content {
    padding: 1.5rem;
    max-width: var(--container-xl);
  }
}

@media (max-width: 768px) {
  .navbar {
    padding: 1rem;
    min-height: 64px;
  }
  
  .brand-link {
    padding: 0.25rem 0.5rem;
  }
  
  .brand-icon {
    font-size: 1.25rem;
  }
  
  .brand-text {
    font-size: 1rem;
  }
  
  .nav-container {
    display: none;
  }
  
  .mobile-nav-toggle {
    display: block;
  }
  
  .nav-actions {
    gap: 0.5rem;
  }
  
  .main-content {
    padding: 1rem;
    max-width: var(--container-md);
  }
  
  .card {
    padding: 1.25rem;
    border-radius: var(--radius-xl);
  }
  
  .btn {
    padding: 0.75rem 1.25rem;
  }
  
  .mobile-nav-menu {
    top: 64px;
    max-height: calc(100vh - 64px);
  }
}

@media (max-width: 480px) {
  .navbar {
    padding: 0.75rem;
  }
  
  .brand-link {
    padding: 0.25rem 0.5rem;
    gap: 0.5rem;
  }
  
  .brand-icon {
    font-size: 1.1rem;
  }
  
  .brand-text {
    font-size: 0.9rem;
  }
  
  .menu-toggle-btn {
    width: 36px;
    height: 36px;
    font-size: 1.1rem;
  }
  
  .nav-actions {
    gap: 0.5rem;
  }
  
  .btn-theme-toggle {
    width: 36px;
    height: 36px;
    font-size: 0.9rem;
  }
  
  .main-content {
    padding: 0.75rem;
  }
  
  .card {
    padding: 1rem;
  }
  
  .mobile-nav-link {
    padding: 0.75rem;
  }
  
  .mobile-nav-actions {
    gap: 0.5rem;
  }
}

/* Utility classes */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

.mt-1 { margin-top: var(--space-1); }
.mt-2 { margin-top: var(--space-2); }
.mt-3 { margin-top: var(--space-3); }
.mt-4 { margin-top: var(--space-4); }
.mt-5 { margin-top: var(--space-5); }
.mt-6 { margin-top: var(--space-6); }

.mb-1 { margin-bottom: var(--space-1); }
.mb-2 { margin-bottom: var(--space-2); }
.mb-3 { margin-bottom: var(--space-3); }
.mb-4 { margin-bottom: var(--space-4); }
.mb-5 { margin-bottom: var(--space-5); }
.mb-6 { margin-bottom: var(--space-6); }

.ml-1 { margin-left: var(--space-1); }
.ml-2 { margin-left: var(--space-2); }
.ml-3 { margin-left: var(--space-3); }
.ml-4 { margin-left: var(--space-4); }

.mr-1 { margin-right: var(--space-1); }
.mr-2 { margin-right: var(--space-2); }
.mr-3 { margin-right: var(--space-3); }
.mr-4 { margin-right: var(--space-4); }

.p-1 { padding: var(--space-1); }
.p-2 { padding: var(--space-2); }
.p-3 { padding: var(--space-3); }
.p-4 { padding: var(--space-4); }
.p-5 { padding: var(--space-5); }
.p-6 { padding: var(--space-6); }

.flex {
  display: flex;
}

.flex-col {
  flex-direction: column;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

.justify-between {
  justify-content: space-between;
}

.gap-1 { gap: var(--space-1); }
.gap-2 { gap: var(--space-2); }
.gap-3 { gap: var(--space-3); }
.gap-4 { gap: var(--space-4); }
.gap-5 { gap: var(--space-5); }
.gap-6 { gap: var(--space-6); }

.w-full {
  width: 100%;
}

.h-full {
  height: 100%;
}

.flex-1 {
  flex: 1;
}

/* Scrollbar styles */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--bg-tertiary);
  border-radius: var(--radius-full);
}

::-webkit-scrollbar-thumb {
  background: var(--neutral-400);
  border-radius: var(--radius-full);
  transition: background var(--transition-fast);
}

::-webkit-scrollbar-thumb:hover {
  background: var(--neutral-500);
}

/* Selection styles */
::selection {
  background-color: var(--primary-200);
  color: var(--primary-800);
}

::-moz-selection {
  background-color: var(--primary-200);
  color: var(--primary-800);
}
</style>