import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Materials from '../views/Materials.vue'
import Vocabulary from '../views/Vocabulary.vue'
import Review from '../views/Review.vue'
import TodoList from '../views/TodoList.vue'
import HighlightTest from '../components/HighlightTest.vue'
import ApiTest from '../components/ApiTest.vue'

const routes = [
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
    path: '/test-highlight',
    name: 'HighlightTest',
    component: HighlightTest,
    meta: { title: 'Highlight Test' }
  },
  {
    path: '/test-api',
    name: 'ApiTest',
    component: ApiTest,
    meta: { title: 'API Test' }
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