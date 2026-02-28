const deckService = require('../../services/deckService')
const materialService = require('../../services/materialService')
const vocabularyService = require('../../services/vocabularyService')
const reviewService = require('../../services/reviewService')

Page({
  data: {
    // Review session types
    sessionType: 'all', // 'all', 'deck', 'custom'
    selectedDeckId: '',
    selectedMaterialId: '',
    selectedCardIds: [],
    searchQuery: '',
    similarSearchEnabled: false,
    starting: false,
    
    // Data
    pendingReviews: 0,
    totalCards: 0,
    materials: [],
    cards: [],
    materialCards: [],
    userDecks: [],
    filteredCards: []
  },

  onLoad(options) {
    console.log('Review page loaded')
    this.loadReviewData()
  },

  onShow() {
    // Refresh data when page is shown
    this.loadReviewData()
  },

  /**
   * Load all review data
   */
  async loadReviewData() {
    try {
      wx.showLoading({ title: 'Loading...' })
      
      // Load materials
      const materials = await materialService.getAllMaterials()
      
      // Load all cards
      const cards = await vocabularyService.getAllCards()
      
      // Load user decks
      const userDecks = await deckService.getAllDecks()
      
      // Get cards due for review
      let pendingReviews = 0
      try {
        const dueCards = await vocabularyService.getCardsDueForReview()
        pendingReviews = dueCards.length
      } catch (error) {
        console.error('Error loading pending reviews:', error)
      }
      
      this.setData({
        materials,
        cards,
        userDecks,
        pendingReviews,
        totalCards: cards.length
      })
      
      wx.hideLoading()
    } catch (error) {
      wx.hideLoading()
      console.error('Error loading review data:', error)
      wx.showToast({ title: 'Failed to load data', icon: 'none' })
    }
  },

  /**
   * Handle session type change
   */
  handleSessionTypeChange(e) {
    const sessionType = e.currentTarget.dataset.type
    this.setData({ 
      sessionType,
      selectedDeckId: '',
      selectedMaterialId: '',
      selectedCardIds: [],
      materialCards: [],
      filteredCards: []
    })
  },

  /**
   * Handle deck selection change
   */
  handleDeckSelectionChange(e) {
    this.setData({ selectedDeckId: e.detail.value })
  },

  /**
   * Handle material selection change
   */
  async handleMaterialSelectionChange(e) {
    const materialId = e.detail.value
    this.setData({ 
      selectedMaterialId: materialId,
      selectedCardIds: [],
      materialCards: [],
      filteredCards: []
    })
    
    if (materialId) {
      await this.loadMaterialCards(materialId)
    }
  },

  /**
   * Load cards for selected material
   */
  async loadMaterialCards(materialId) {
    try {
      wx.showLoading({ title: 'Loading cards...' })
      const cardsData = await vocabularyService.getCardsByMaterial(materialId)
      
      // Add material information to each card
      const cardsWithMaterial = cardsData.map(c => ({
        ...c,
        materialId: materialId
      }))
      
      this.setData({ 
        materialCards: cardsWithMaterial,
        filteredCards: cardsWithMaterial
      })
      
      wx.hideLoading()
    } catch (error) {
      wx.hideLoading()
      console.error('Error loading material cards:', error)
      wx.showToast({ title: 'Failed to load cards', icon: 'none' })
    }
  },

  /**
   * Handle card selection change
   */
  handleCardSelectionChange(e) {
    this.setData({ selectedCardIds: e.detail.value })
  },

  /**
   * Handle search input change
   */
  handleSearchInputChange(e) {
    const searchQuery = e.detail.value
    this.setData({ searchQuery })
    this.filterCards()
  },

  /**
   * Handle similar search toggle
   */
  handleSimilarSearchToggle(e) {
    const similarSearchEnabled = e.detail.value.length > 0
    this.setData({ similarSearchEnabled })
    this.filterCards()
  },

  /**
   * Filter cards based on search query
   */
  filterCards() {
    const { materialCards, searchQuery, similarSearchEnabled } = this.data
    
    if (!searchQuery) {
      this.setData({ filteredCards: materialCards })
      return
    }
    
    const query = searchQuery.toLowerCase()
    
    const filtered = materialCards.filter(card => {
      const cardText = card.text.toLowerCase()
      
      // Exact match
      if (cardText.includes(query)) {
        return true
      }
      
      // Similar word search if enabled
      if (similarSearchEnabled) {
        const words = cardText.split(/\s+/)
        return words.some(word => {
          const similarity = this.getSimilarity(query, word)
          return similarity > 0.6
        })
      }
      
      return false
    })
    
    this.setData({ filteredCards: filtered })
  },

  /**
   * Calculate string similarity (Levenshtein distance)
   */
  getSimilarity(str1, str2) {
    const len1 = str1.length
    const len2 = str2.length
    const matrix = Array(len1 + 1).fill().map(() => Array(len2 + 1).fill(0))
    
    for (let i = 0; i <= len1; i++) matrix[i][0] = i
    for (let j = 0; j <= len2; j++) matrix[0][j] = j
    
    for (let i = 1; i <= len1; i++) {
      for (let j = 1; j <= len2; j++) {
        const cost = str1[i - 1] === str2[j - 1] ? 0 : 1
        matrix[i][j] = Math.min(
          matrix[i - 1][j] + 1,
          matrix[i][j - 1] + 1,
          matrix[i - 1][j - 1] + cost
        )
      }
    }
    
    const maxLength = Math.max(len1, len2)
    return (maxLength - matrix[len1][len2]) / maxLength
  },

  /**
   * Get filtered cards for display
   */
  getFilteredCards() {
    return this.data.filteredCards
  },

  /**
   * Get selected cards with material information
   */
  getSelectedCardsList() {
    const { selectedCardIds, cards, materials } = this.data
    
    return selectedCardIds.map(id => {
      const card = cards.find(c => c.id == id)
      if (card) {
        const material = materials.find(m => m.id == card.materialId)
        return {
          ...card,
          materialTitle: material ? material.title : 'Unknown'
        }
      }
      return null
    }).filter(c => c !== null)
  },

  /**
   * Delete card from selection
   */
  deleteCard(e) {
    const cardId = e.currentTarget.dataset.id
    const selectedCardIds = this.data.selectedCardIds.filter(id => id !== cardId.toString())
    this.setData({ selectedCardIds })
  },

  /**
   * Check if session can start
   */
  canStartSession() {
    const { sessionType, selectedDeckId, selectedMaterialId, selectedCardIds, totalCards } = this.data
    
    switch (sessionType) {
      case 'all':
        return totalCards > 0
      case 'deck':
        return selectedDeckId !== ''
      case 'custom':
        return selectedMaterialId === '' || selectedCardIds.length > 0
      default:
        return false
    }
  },

  /**
   * Get start button text
   */
  getStartButtonText() {
    const { sessionType, selectedCardIds } = this.data
    
    switch (sessionType) {
      case 'all':
        return 'Start Full Review Session'
      case 'deck':
        return 'Start Deck Review Session'
      case 'custom':
        return selectedCardIds.length > 0 ? 'Start Selected Review Session' : 'Start Review Session'
      default:
        return 'Start Review Session'
    }
  },

  /**
   * Start review session
   */
  async startReviewSession() {
    if (!this.canStartSession()) return
    
    this.setData({ starting: true })
    
    try {
      let session
      
      switch (this.data.sessionType) {
        case 'deck':
          // Start deck review session
          session = await reviewService.startDeckReviewSession(this.data.selectedDeckId)
          break
        case 'custom':
          // Start custom review session
          if (this.data.selectedCardIds.length > 0) {
            session = await reviewService.startCustomReviewSession(this.data.selectedCardIds)
          } else {
            session = await reviewService.startReviewSession()
          }
          break
        case 'all':
        default:
          // Start full review session
          session = await reviewService.startReviewSession()
          break
      }
      
      if (session && session.id) {
        // Navigate to review session
        wx.navigateTo({
          url: `/pages/review-session/review-session?id=${session.id}`
        })
      } else {
        wx.showToast({ title: 'No cards to review', icon: 'none' })
      }
    } catch (error) {
      console.error('Error starting review session:', error)
      wx.showToast({ title: 'Failed to start session', icon: 'none' })
    } finally {
      this.setData({ starting: false })
    }
  }
})
