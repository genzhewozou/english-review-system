<template>
  <div id="app">
    <nav class="navbar">
      <div class="nav-brand">
        <h1>English Learning System</h1>
      </div>
      <div class="nav-links">
        <router-link to="/" class="nav-link">Dashboard</router-link>
        <router-link to="/materials" class="nav-link">Materials</router-link>
        <router-link to="/vocabulary" class="nav-link">Vocabulary</router-link>
        <router-link to="/review" class="nav-link">Review</router-link>
        <router-link to="/todo" class="nav-link">Todo List</router-link>
        <router-link to="/test-highlight" class="nav-link">Test Highlight</router-link>
        <router-link to="/test-api" class="nav-link">Test API</router-link>
        <NotificationPanel />
      </div>
    </nav>
    
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
  }
}
</script>

<style>
#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  background-color: #2c3e50;
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.nav-brand h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
}

.nav-links {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover,
.nav-link.router-link-active {
  background-color: #34495e;
}

.main-content {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

/* Responsive design */
@media (max-width: 768px) {
  .navbar {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }
  
  .nav-links {
    flex-wrap: wrap;
    justify-content: center;
    gap: 1rem;
  }
  
  .main-content {
    padding: 1rem;
  }
}
</style>