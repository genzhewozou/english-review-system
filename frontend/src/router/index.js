import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Materials from '../views/Materials.vue'
import Vocabulary from '../views/Vocabulary.vue'
import Review from '../views/Review.vue'
import TodoList from '../views/TodoList.vue'
import Decks from '../views/Decks.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: 'Login' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: 'Register' }
  },
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard,
    meta: { title: 'Dashboard' }
  },
  {
    path: '/materials',
    name: 'Materials',
    component: Materials,
    meta: { title: 'Study Materials' }
  },
  {
    path: '/materials/:id',
    name: 'MaterialViewer',
    component: () => import('../views/MaterialViewer.vue'),
    meta: { title: 'View Material' }
  },
  {
    path: '/vocabulary',
    name: 'Vocabulary',
    component: Vocabulary,
    meta: { title: 'Vocabulary Management' }
  },
  {
    path: '/review',
    name: 'Review',
    component: Review,
    meta: { title: 'Review Session' }
  },
  {
    path: '/review/:sessionId',
    name: 'ReviewSession',
    component: () => import('../views/ReviewSession.vue'),
    meta: { title: 'Active Review' }
  },
  {
    path: '/todo',
    name: 'TodoList',
    component: TodoList,
    meta: { title: 'Todo List' }
  },
  {
    path: '/decks',
    name: 'Decks',
    component: Decks,
    meta: { title: 'Deck Management' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Update document title based on route
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - English Learning System` : 'English Learning System'
  next()
})

export default router