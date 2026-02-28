const reviewService = require('../../services/reviewService');
const todoService = require('../../services/todoService');

Page({
  data: {
    loading: true,
    session: null,
    currentQuestion: null,
    currentQuestionIndex: 0,
    questions: [],
    submittingAnswer: false,
    sessionStartTime: null,
    sessionPaused: false,
    answerHistory: [],
    showAddToTodoModal: false,
    addingToTodo: false,
    todoForm: {
      title: '',
      description: '',
      dueDate: ''
    },
    showComment: false,
    isFlipped: false
  },

  onLoad(options) {
    this.loadSession(options.sessionId || options.id);
  },

  // Load session
  async loadSession(sessionId) {
    try {
      this.setData({ loading: true });
      const sessionData = await reviewService.getSession(sessionId);
      this.setData({ session: sessionData });
      
      if (!sessionData.completed) {
        this.setData({ sessionStartTime: new Date() });
        await this.loadQuestions();
      }
    } catch (error) {
      console.error('Failed to load session:', error);
      this.setData({ session: null });
      wx.showToast({
        title: 'Failed to load session',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  // Load questions
  async loadQuestions() {
    try {
      const session = this.data.session;
      if (!session) return;
      
      const list = await reviewService.getSessionQuestions(session.id);
      
      this.setData({
        questions: list,
        'session.totalQuestions': list.length
      });

      if (list.length === 0) {
        await this.completeSession();
        this.setData({ currentQuestion: null });
        return;
      }

      this.setData({ currentQuestionIndex: 0 });
      this.setCurrentQuestionFromIndex();
    } catch (error) {
      console.error('Failed to load questions list, falling back to next-question:', error);
      // Fallback to old behaviour
      await this.loadNextQuestion();
    }
  },

  // Set current question from index
  setCurrentQuestionFromIndex() {
    const questions = this.data.questions;
    const currentQuestionIndex = this.data.currentQuestionIndex;
    const q = questions[currentQuestionIndex];
    
    if (!q) {
      this.setData({ currentQuestion: null });
      return;
    }
    
    const currentQuestion = {
      id: q.cardId,
      text: q.text,
      backText: q.backText,
      context: q.context,
      userComment: q.userComment || null,
      position: q.questionNumber || q.position,
      total: q.totalQuestions || q.total,
      dueDate: q.dueDate,
      easeFactor: q.easeFactor,
      interval: q.interval
    };
    
    this.setData({ 
      currentQuestion: currentQuestion,
      showComment: false,
      isFlipped: false
    });
  },

  // Load next question
  async loadNextQuestion() {
    try {
      const session = this.data.session;
      if (!session) return;
      
      const data = await reviewService.getNextQuestion(session.id);

      // If no question returned, complete the session
      if (!data) {
        await this.completeSession();
        this.setData({ currentQuestion: null });
        return;
      }

      // Map flat QuestionResultDto into the structure expected
      const currentQuestion = {
        id: data.cardId,
        text: data.text,
        backText: data.backText,
        context: data.context,
        userComment: data.userComment || null,
        position: data.questionNumber || data.position,
        total: data.totalQuestions || data.total,
        dueDate: data.dueDate,
        easeFactor: data.easeFactor,
        interval: data.interval
      };

      this.setData({ currentQuestion: currentQuestion });

      // Keep session totalQuestions in sync if backend provides it
      if (typeof data.totalQuestions === 'number') {
        this.setData({ 'session.totalQuestions': data.totalQuestions });
      } else if (typeof data.total === 'number') {
        this.setData({ 'session.totalQuestions': data.total });
      }
    } catch (error) {
      console.error('Failed to load next question:', error);
    }
  },

  // Submit answer
  async submitAnswer(e) {
    try {
      const quality = e.currentTarget.dataset.quality;
      const session = this.data.session;
      const currentQuestion = this.data.currentQuestion;
      
      if (this.data.submittingAnswer || !session || !currentQuestion) return;
      
      this.setData({ submittingAnswer: true });
      
      console.log('Submitting answer:', {
        sessionId: session.id,
        cardId: currentQuestion.id,
        quality: quality
      });
      
      await reviewService.submitAnswer(session.id, {
        cardId: currentQuestion.id,
        quality: quality,
        responseTimeSeconds: null
      });

      // Record answer in history
      const answerHistory = [...this.data.answerHistory];
      answerHistory.push({
        id: currentQuestion.id,
        text: currentQuestion.text,
        backText: currentQuestion.backText,
        context: currentQuestion.context,
        userComment: currentQuestion.userComment,
        quality: quality,
        timestamp: new Date()
      });
      this.setData({ answerHistory: answerHistory });

      // Update session stats
      const sessionData = { ...session };
      sessionData.totalQuestions = sessionData.totalQuestions || 0;
      if (quality === 'PERFECT' || quality === 'DIFFICULT') {
        sessionData.correctAnswers = (sessionData.correctAnswers || 0) + 1;
      }
      this.setData({ session: sessionData });

      const currentQuestionIndex = this.data.currentQuestionIndex + 1;
      this.setData({ currentQuestionIndex: currentQuestionIndex });
      
      // Auto-advance to next item if we have a list; otherwise fall back
      const questions = this.data.questions;
      if (questions.length > 0) {
        if (currentQuestionIndex >= questions.length) {
          await this.completeSession();
          this.setData({ currentQuestion: null });
        } else {
          this.setCurrentQuestionFromIndex();
        }
      } else {
        await this.loadNextQuestion();
      }
      
    } catch (error) {
      console.error('Failed to submit answer:', error);
      wx.showToast({
        title: 'Failed to submit answer',
        icon: 'none'
      });
    } finally {
      this.setData({ submittingAnswer: false });
    }
  },

  // Complete session
  async completeSession() {
    try {
      const session = this.data.session;
      if (!session) return;
      
      await reviewService.completeSession(session.id);
      
      const sessionData = { ...session };
      sessionData.completed = true;
      sessionData.endTime = new Date();
      this.setData({ session: sessionData });
    } catch (error) {
      console.error('Failed to complete session:', error);
    }
  },

  // Toggle pause
  togglePause() {
    this.setData({ sessionPaused: !this.data.sessionPaused });
  },

  // Go to previous question
  goPrev() {
    const questions = this.data.questions;
    if (questions.length === 0) return;
    
    const currentQuestionIndex = this.data.currentQuestionIndex;
    if (currentQuestionIndex <= 0) return;
    
    this.setData({ currentQuestionIndex: currentQuestionIndex - 1 });
    this.setCurrentQuestionFromIndex();
  },

  // Go to next question
  goNext() {
    const questions = this.data.questions;
    if (questions.length === 0) return;
    
    const currentQuestionIndex = this.data.currentQuestionIndex;
    if (currentQuestionIndex >= questions.length - 1) return;
    
    this.setData({ currentQuestionIndex: currentQuestionIndex + 1 });
    this.setCurrentQuestionFromIndex();
  },

  // Confirm end session
  async confirmEndSession() {
    const confirmed = await this.confirmSessionEnd();
    if (confirmed) {
      await this.completeSession();
    }
  },

  // Confirm session end
  confirmSessionEnd() {
    return new Promise((resolve) => {
      wx.showModal({
        title: 'End Session',
        content: 'Are you sure you want to end this session?',
        success: (res) => {
          resolve(res.confirm);
        },
        fail: () => {
          resolve(false);
        }
      });
    });
  },

  // Calculate accuracy
  calculateAccuracy() {
    const session = this.data.session;
    if (!session || session.totalQuestions === 0) return 0;
    return Math.round((session.correctAnswers / session.totalQuestions) * 100);
  },

  // Format duration
  formatDuration() {
    const session = this.data.session;
    const sessionStartTime = this.data.sessionStartTime;
    if (!session || !sessionStartTime) return '0m';
    
    const endTime = session.endTime || new Date();
    const duration = Math.floor((endTime - sessionStartTime) / 1000 / 60);
    
    if (duration < 60) {
      return `${duration}m`;
    } else {
      const hours = Math.floor(duration / 60);
      const minutes = duration % 60;
      return `${hours}h ${minutes}m`;
    }
  },

  // Get strong areas
  get strongAreas() {
    const answerHistory = this.data.answerHistory;
    const correctAnswers = answerHistory.filter(a => 
      a && (a.quality === 'PERFECT' || a.quality === 'DIFFICULT')
    );
    // Group by material or category if available
    return [...new Set(correctAnswers.map(a => a && a.text ? a.text.substring(0, 20) + '...' : 'General'))];
  },

  // Get weak areas
  get weakAreas() {
    const answerHistory = this.data.answerHistory;
    const incorrectAnswers = answerHistory.filter(a => 
      a && (a.quality === 'BLACKOUT' || a.quality === 'REMEMBERED')
    );
    return [...new Set(incorrectAnswers.map(a => a && a.text ? a.text.substring(0, 20) + '...' : 'General'))];
  },

  // Show add to todo modal
  showAddToTodoModal() {
    this.closeAddToTodoModal(); // Reset form values
    this.setData({ showAddToTodoModal: true });
  },

  // Close add to todo modal
  closeAddToTodoModal() {
    this.setData({ showAddToTodoModal: false });
    
    const session = this.data.session;
    let todoForm = {
      title: `Review Session - ${new Date().toLocaleDateString()}`,
      description: '',
      dueDate: ''
    };
    
    if (session && session.completed) {
      todoForm.description = `Review session completed with ${session.correctAnswers}/${session.totalQuestions || 0} correct answers (${this.calculateAccuracy()}% accuracy).`;
    } else if (session) {
      todoForm.description = `Active review session in progress: ${this.data.currentQuestionIndex + 1}/${session.totalQuestions || 0} questions completed.`;
    }
    
    this.setData({ todoForm: todoForm });
  },

  // Add to todo list
  async addToTodoList() {
    try {
      this.setData({ addingToTodo: true });
      const todoForm = this.data.todoForm;
      const session = this.data.session;
      
      const todoData = {
        title: todoForm.title,
        description: todoForm.description,
        dueDate: todoForm.dueDate,
        type: 'REVIEW_SESSION',
        relatedSessionId: session?.id
      };
      
      await todoService.createTodo(todoData);
      this.closeAddToTodoModal();
      wx.showToast({ 
        title: 'Added to todo list!', 
        icon: 'success' 
      });
    } catch (error) {
      console.error('Error adding to todo list:', error);
      wx.showToast({ 
        title: 'Failed to add to todo list', 
        icon: 'none' 
      });
    } finally {
      this.setData({ addingToTodo: false });
    }
  },

  // Handle todo form changes
  onTodoTitleChange(e) {
    this.setData({ 'todoForm.title': e.detail.value });
  },

  onTodoDescriptionChange(e) {
    this.setData({ 'todoForm.description': e.detail.value });
  },

  onTodoDueDateChange(e) {
    this.setData({ 'todoForm.dueDate': e.detail.value });
  },

  // Go back
  goBack() {
    wx.navigateBack({ delta: 1 });
  },

  // Go back to review
  goBackToReview() {
    wx.navigateTo({ url: '/pages/review/review' });
  },

  // Go to vocabulary
  goToVocabulary() {
    wx.navigateTo({ url: '/pages/vocabulary/vocabulary' });
  },

  // Flip card
  flipCard() {
    this.setData({ isFlipped: !this.data.isFlipped });
  },

  // Toggle comment
  toggleComment() {
    this.setData({ showComment: !this.data.showComment });
  },

  // Speak text
  speakText(e) {
    const text = e.currentTarget.dataset.text;
    if (text) {
      wx.setInnerAudioOption({ obeyMuteSwitch: false });
      const innerAudioContext = wx.createInnerAudioContext();
      innerAudioContext.text = text;
      innerAudioContext.lang = 'en-US';
      innerAudioContext.volume = 1;
      innerAudioContext.speak();
    }
  },

  // Get question header
  getQuestionHeader() {
    return 'What does this word/phrase mean?';
  },

  // Format due date
  formatDueDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
});