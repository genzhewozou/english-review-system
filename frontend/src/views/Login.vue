<template>
  <main class="auth-container" aria-labelledby="login-heading">
    <section class="auth-card" tabindex="0">
      <h2 id="login-heading">Login</h2>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group" role="group" aria-labelledby="username-label">
          <label for="username" id="username-label" class="form-label">Username</label>
          <input 
            type="text" 
            id="username"
            v-model="form.username" 
            placeholder="Enter your username"
            :disabled="loading"
            class="form-control"
            required
            aria-required="true"
          />
        </div>
        <div class="form-group" role="group" aria-labelledby="password-label">
          <label for="password" id="password-label" class="form-label">Password</label>
          <input 
            type="password" 
            id="password"
            v-model="form.password" 
            placeholder="Enter your password"
            :disabled="loading"
            class="form-control"
            required
            aria-required="true"
          />
        </div>
        <div class="form-actions">
          <button 
            type="submit" 
            :disabled="loading"
            class="btn btn-primary"
            aria-busy="{{ loading }}"
          >
            <span v-if="loading" class="loading-spinner"></span>
            {{ loading ? 'Logging in...' : 'Login' }}
          </button>
        </div>
      </form>
      <p class="auth-link">
        Don't have an account? <router-link to="/register" aria-label="Register here">Register here</router-link>
      </p>
      <div v-if="error" class="error-message" role="alert" aria-live="assertive">
        {{ error }}
      </div>
    </section>
  </main>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      loading: false,
      error: ''
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true;
      this.error = '';
      
      try {
        const response = await fetch('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.form)
        });
        
        // Handle both JSON and plain text responses
        const contentType = response.headers.get('content-type');
        let data;
        if (contentType && contentType.includes('application/json')) {
          data = await response.json();
        } else {
          data = await response.text();
        }
        
        if (response.ok) {
          // Store user info and token in localStorage
          localStorage.setItem('user', JSON.stringify(data));
          localStorage.setItem('authToken', data.token);
          localStorage.setItem('isAuthenticated', 'true');
          // Redirect to dashboard
          this.$router.push('/');
        } else {
          this.error = typeof data === 'string' ? data : (data.message || 'Login failed');
        }
      } catch (err) {
        this.error = 'An error occurred. Please try again.';
        console.error('Login error:', err);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
  padding: var(--space-8);
  background-color: var(--bg-secondary);
}

.auth-card {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-lg);
  padding: var(--space-8);
  width: 100%;
  max-width: 480px;
  border: 1px solid var(--surface-border);
  transition: var(--transition-normal);
}

.auth-card:hover {
  box-shadow: var(--shadow-xl);
  transform: translateY(-2px);
}

.auth-card:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

h2 {
  margin-top: 0;
  color: var(--text-primary);
  text-align: center;
  margin-bottom: var(--space-6);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  letter-spacing: -0.025em;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.form-group {
  margin-bottom: var(--space-4);
}

.form-label {
  display: block;
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  font-size: var(--text-sm);
  margin-bottom: var(--space-2);
  text-align: left;
}

.form-control {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--surface-border);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  transition: var(--transition-normal);
  background-color: var(--surface-primary);
  color: var(--text-primary);
}

.form-control:focus {
  outline: none;
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
  transform: translateY(-1px);
}

.form-control:disabled {
  background-color: var(--bg-tertiary);
  color: var(--text-light);
  cursor: not-allowed;
}

.form-actions {
  margin-top: var(--space-6);
  display: flex;
  justify-content: center;
}

.form-actions .btn {
  width: 100%;
  padding: var(--space-4) var(--space-8);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-lg);
  transition: var(--transition-normal);
}

.auth-link {
  text-align: center;
  margin-top: var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.auth-link a {
  color: var(--primary-600);
  text-decoration: none;
  font-weight: var(--font-medium);
  transition: var(--transition-normal);
}

.auth-link a:hover {
  text-decoration: underline;
  color: var(--primary-700);
}

.auth-link a:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
  border-radius: var(--radius-sm);
}

.error-message {
  margin-top: var(--space-4);
  padding: var(--space-4);
  background-color: var(--error-50);
  border: 1px solid var(--error-200);
  border-radius: var(--radius-lg);
  color: var(--error-600);
  font-size: var(--text-sm);
  text-align: center;
  animation: fadeIn var(--transition-normal) var(--transition-ease-out);
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s ease-in-out infinite;
  margin-right: var(--space-2);
}

@keyframes spin {
  to { transform: rotate(360deg); }
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

/* Responsive design */
@media (max-width: 768px) {
  .auth-container {
    padding: var(--space-4);
    min-height: 80vh;
  }
  
  .auth-card {
    padding: var(--space-6);
    max-width: 100%;
  }
  
  h2 {
    font-size: var(--text-xl);
    margin-bottom: var(--space-4);
  }
  
  .form-actions {
    margin-top: var(--space-4);
  }
  
  .form-label {
    font-size: var(--text-sm);
  }
  
  .form-control {
    font-size: var(--text-sm);
    padding: var(--space-3) var(--space-4);
  }
  
  .form-actions .btn {
    padding: var(--space-3) var(--space-6);
    font-size: var(--text-sm);
  }
}

/* High contrast mode support */
@media (prefers-contrast: high) {
  .auth-card {
    border: 2px solid var(--text-primary);
  }
  
  .form-control {
    border: 2px solid var(--text-primary);
  }
  
  .error-message {
    border: 2px solid var(--error-600);
  }
}

/* Reduced motion support */
@media (prefers-reduced-motion: reduce) {
  .loading-spinner {
    animation: none;
  }
  
  .error-message {
    animation: none;
  }
  
  .form-control:focus {
    transform: none;
  }
  
  .auth-card:hover {
    transform: none;
  }
}
</style>