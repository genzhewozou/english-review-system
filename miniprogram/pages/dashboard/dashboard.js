const materialService = require('../../services/materialService');
const vocabularyService = require('../../services/vocabularyService');
const reviewService = require('../../services/reviewService');
const todoService = require('../../services/todoService');

Page({
  data: {
    // Core dashboard data
    materialsCount: 0,
    cardsCount: 0,
    pendingReviews: 0,
    todoCount: 0,
    recentActivities: [],
    todos: [],
    
    // New dashboard data
    currentDate: '',
    learningStreak: 0,
    newMaterials: 0,
    newCards: 0,
    completedTasks: 0,
    
    // Progress data
    materialsProgress: 0,
    vocabularyProgress: 0,
    reviewProgress: 0,
    taskProgress: 0,
    
    // Chart data
    weeklyProgress: [],
    
    // Recommendations
    recommendations: [],
    
    // Alert dismissal states
    showOverdueAlert: true,
    showDueTodayAlert: true,
    showReviewAlert: true,
    
    // Computed alert counts
    overdueCount: 0,
    dueTodayCount: 0,
    
    // Alert messages
    overdueMessage: '',
    dueTodayMessage: ''
  },

  onLoad() {
    this.loadDashboardData();
  },

  // Initialize dashboard data
  async loadDashboardData() {
    try {
      // Set current date
      this.setData({
        currentDate: new Date().toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          weekday: 'long'
        })
      });
      
      // Load todo items for alert calculations
      await this.loadTodoItems();
      
      // Load other dashboard statistics
      await this.loadMaterialsData();
      await this.loadVocabularyData();
      await this.loadReviewData();
      
      // Calculate task progress
      this.calculateTaskProgress();
      
      // Simulate learning streak
      this.setData({
        learningStreak: Math.floor(Math.random() * 15) + 1
      });
      
      // Initialize weekly progress chart data
      await this.initWeeklyProgress();
      
      // Initialize recommendations
      this.initRecommendations();
      
      // Load recent activities
      await this.loadRecentActivities();
      
    } catch (error) {
      console.error('Error loading dashboard data:', error);
      wx.showToast({
        title: 'Failed to load dashboard',
        icon: 'none'
      });
    }
  },

  // Load todo items
  async loadTodoItems() {
    try {
      const todos = await todoService.getTodoItems();
      const todoCount = todos.filter(t => !t.completed).length;
      const completedTasks = todos.filter(t => t.completed).length;
      
      this.setData({
        todos,
        todoCount,
        completedTasks
      });
      
      // Calculate alert messages
      this.calculateAlertMessages();
    } catch (error) {
      console.error('Error loading todo items:', error);
      // Set empty data if API fails
      this.setData({
        todos: [],
        todoCount: 0,
        completedTasks: 0
      });
      this.calculateAlertMessages();
    }
  },

  // Load materials data
  async loadMaterialsData() {
    try {
      const materials = await materialService.getAllMaterials();
      const materialsCount = materials.length;
      
      // Calculate new materials (created in last 7 days)
      const oneWeekAgo = new Date();
      oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
      const newMaterials = materials.filter(m => {
        const createdDate = new Date(m.createdAt);
        return createdDate > oneWeekAgo;
      }).length;
      
      // Calculate progress (simulated based on weekly goal)
      const materialsProgress = Math.min(100, Math.floor((newMaterials / 5) * 100));
      
      this.setData({
        materialsCount,
        newMaterials,
        materialsProgress
      });
    } catch (error) {
      console.error('Error loading materials data:', error);
      // Set empty data if API fails
      this.setData({
        materialsCount: 0,
        newMaterials: 0,
        materialsProgress: 0
      });
    }
  },

  // Load vocabulary data
  async loadVocabularyData() {
    try {
      const cards = await vocabularyService.getAllCards();
      const cardsCount = cards.length;
      
      // Calculate new cards (created in last 7 days)
      const oneWeekAgo = new Date();
      oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
      const newCards = cards.filter(c => {
        const createdDate = new Date(c.createdAt);
        return createdDate > oneWeekAgo;
      }).length;
      
      // Calculate progress (simulated based on weekly goal)
      const vocabularyProgress = Math.min(100, Math.floor((newCards / 10) * 100));
      
      this.setData({
        cardsCount,
        newCards,
        vocabularyProgress
      });
    } catch (error) {
      console.error('Error loading vocabulary data:', error);
      // Set empty data if API fails
      this.setData({
        cardsCount: 0,
        newCards: 0,
        vocabularyProgress: 0
      });
    }
  },

  // Load review data
  async loadReviewData() {
    try {
      const dueCards = await vocabularyService.getCardsDueForReview();
      const pendingReviews = dueCards.length;
      
      // Calculate progress (simulated - could be based on reviews completed this week)
      const reviewProgress = Math.min(100, Math.floor(Math.random() * 120));
      
      this.setData({
        pendingReviews,
        reviewProgress
      });
    } catch (error) {
      console.error('Error loading review data:', error);
      // Set empty data if API fails
      this.setData({
        pendingReviews: 0,
        reviewProgress: 0
      });
    }
  },

  // Calculate task progress
  calculateTaskProgress() {
    const { completedTasks, todoCount } = this.data;
    const totalTasks = completedTasks + todoCount;
    const taskProgress = totalTasks > 0 ? Math.floor((completedTasks / totalTasks) * 100) : 0;
    
    this.setData({
      taskProgress
    });
  },

  // Calculate alert messages
  calculateAlertMessages() {
    const { todos, showOverdueAlert, showDueTodayAlert } = this.data;
    const today = new Date().toISOString().split('T')[0];
    
    // Calculate overdue tasks
    const overdueTasks = todos.filter(t => {
      return !t.completed && t.dueDate && t.dueDate < today;
    });
    
    let overdueMessage = '';
    let overdueCount = showOverdueAlert ? overdueTasks.length : 0;
    if (overdueTasks.length === 1) {
      overdueMessage = `"${overdueTasks[0].title}" is overdue`;
    } else if (overdueTasks.length > 1) {
      overdueMessage = `You have ${overdueTasks.length} overdue tasks that need attention`;
    }
    
    // Calculate due today tasks
    const dueTasks = todos.filter(t => {
      return !t.completed && t.dueDate && t.dueDate === today;
    });
    
    let dueTodayMessage = '';
    let dueTodayCount = showDueTodayAlert ? dueTasks.length : 0;
    if (dueTasks.length === 1) {
      dueTodayMessage = `"${dueTasks[0].title}" is due today`;
    } else if (dueTasks.length > 1) {
      dueTodayMessage = `You have ${dueTasks.length} tasks due today`;
    }
    
    this.setData({
      overdueMessage,
      dueTodayMessage,
      overdueCount,
      dueTodayCount
    });
  },

  // Initialize weekly progress chart data
  async initWeeklyProgress() {
    try {
      // Try to get real weekly progress data from API
      // If API doesn't exist yet, generate simulated data
      const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
      const progress = days.map(day => ({
        label: day,
        vocabulary: Math.floor(Math.random() * 100),
        review: Math.floor(Math.random() * 100)
      }));
      
      this.setData({
        weeklyProgress: progress
      });
    } catch (error) {
      console.error('Error loading weekly progress:', error);
      // Set empty data if API fails
      this.setData({
        weeklyProgress: []
      });
    }
  },

  // Initialize recommendations
  initRecommendations() {
    const recommendations = [
      {
        icon: '📚',
        title: 'Review Difficult Cards',
        description: 'Focus on cards you struggled with in previous sessions',
        action: 'review-difficult',
        actionText: 'Start Review'
      },
      {
        icon: '🎯',
        title: 'Set Weekly Goal',
        description: 'Define your vocabulary learning target for the week',
        action: 'set-goal',
        actionText: 'Set Goal'
      },
      {
        icon: '📈',
        title: 'Track Your Progress',
        description: 'View detailed analytics of your learning journey',
        action: 'view-analytics',
        actionText: 'View Analytics'
      }
    ];
    
    this.setData({
      recommendations
    });
  },

  // Load recent activities (placeholder data for now)
  async loadRecentActivities() {
    try {
      // Generate placeholder data
      // In the future, this could come from a real API endpoint
      const recentActivities = [
        {
          id: 1,
          type: 'Card Created',
          description: 'Added new vocabulary card: "perspicacious"',
          timestamp: new Date(Date.now() - 3600000).toISOString(),
          meta: 'Vocabulary'
        },
        {
          id: 2,
          type: 'Review Completed',
          description: 'Finished review session with 85% accuracy',
          timestamp: new Date(Date.now() - 7200000).toISOString(),
          meta: 'Reviews'
        },
        {
          id: 3,
          type: 'Material Uploaded',
          description: 'Uploaded new study material: "Business English Essentials"',
          timestamp: new Date(Date.now() - 86400000).toISOString(),
          meta: 'Materials'
        },
        {
          id: 4,
          type: 'Task Completed',
          description: 'Completed task: "Review 20 vocabulary cards"',
          timestamp: new Date(Date.now() - 172800000).toISOString(),
          meta: 'Tasks'
        }
      ];
      
      this.setData({
        recentActivities
      });
    } catch (error) {
      console.error('Error loading recent activities:', error);
      // Set empty data if API fails
      this.setData({
        recentActivities: []
      });
    }
  },

  // Execute recommendation action
  executeRecommendation(e) {
    const action = e.currentTarget.dataset.action;
    
    switch (action) {
      case 'review-difficult':
        wx.navigateTo({
          url: '/pages/review/review'
        });
        break;
      case 'set-goal':
        wx.navigateTo({
          url: '/pages/vocabulary/vocabulary'
        });
        break;
      case 'view-analytics':
        wx.showToast({
          title: 'Statistics page coming soon',
          icon: 'none'
        });
        break;
      default:
        break;
    }
  },

  // Alert dismissal functions
  dismissOverdueAlert() {
    this.setData({
      showOverdueAlert: false,
      overdueCount: 0
    });
  },

  dismissDueTodayAlert() {
    this.setData({
      showDueTodayAlert: false,
      dueTodayCount: 0
    });
  },

  dismissReviewAlert() {
    this.setData({
      showReviewAlert: false
    });
  },

  // Utility functions
  formatTime(timestamp) {
    return new Date(timestamp).toLocaleString();
  },

  getActivityIcon(type) {
    const iconMap = {
      'Card Created': '📝',
      'Material Uploaded': '📄',
      'Review Completed': '✅',
      'Task Completed': '✔️',
      'Deck Created': '🃏',
      'Goal Achieved': '🎯',
      'Streak Extended': '🔥'
    };
    return iconMap[type] || '📊';
  }
});