const deckService = require('../../services/deckService')
const vocabularyService = require('../../services/vocabularyService')

Page({
  data: {
    decks: [],
    publicDecks: [],
    availableCards: [],
    loading: false,
    loadingPublic: false,
    saving: false,
    deleting: false,
    showCreateModal: false,
    showEditModal: false,
    showDeleteModal: false,
    showOptionsModal: false,
    showAddCardModal: false,
    selectedDeck: null,
    
    deckForm: {
      name: '',
      description: '',
      isPublic: false
    },
    
    deckOptions: {
      newCardsPerDay: 20,
      maxReviewsPerDay: 100,
      easyInterval: 4,
      easyBonus: 1.3,
      intervalModifier: 1.0,
      startingEase: 2.5,
      steps: 1
    },
    
    addCardForm: {
      cardId: ''
    }
  },

  onLoad() {
    this.loadDecks()
    this.loadPublicDecks()
  },

  onShow() {
    // Refresh decks when returning to page
    this.loadDecks()
  },

  // Load user's decks
  async loadDecks() {
    this.setData({ loading: true })
    
    try {
      const decks = await deckService.getAllDecks()
      this.setData({ decks })
    } catch (error) {
      console.error('Error loading decks:', error)
      wx.showToast({ title: 'Failed to load decks', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  // Load public decks
  async loadPublicDecks() {
    this.setData({ loadingPublic: true })
    
    try {
      const publicDecks = await deckService.getPublicDecks()
      this.setData({ publicDecks })
    } catch (error) {
      console.error('Error loading public decks:', error)
      this.setData({ publicDecks: [] })
    } finally {
      this.setData({ loadingPublic: false })
    }
  },

  // Load available cards for adding to deck
  async loadCards() {
    try {
      const cards = await vocabularyService.getAllCards()
      this.setData({ availableCards: cards })
    } catch (error) {
      console.error('Error loading cards:', error)
      this.setData({ availableCards: [] })
    }
  },

  // Create new deck
  createDeck() {
    this.setData({
      selectedDeck: null,
      deckForm: {
        name: '',
        description: '',
        isPublic: false
      },
      showCreateModal: true,
      showEditModal: false
    })
  },

  // Edit deck
  editDeck(e) {
    const deck = e.currentTarget.dataset.deck
    
    this.setData({
      selectedDeck: deck,
      deckForm: {
        name: deck.name,
        description: deck.description || '',
        isPublic: deck.isPublic || false
      },
      showEditModal: true,
      showCreateModal: false
    })
  },

  // Close create/edit modal
  closeModal() {
    this.setData({
      showCreateModal: false,
      showEditModal: false,
      selectedDeck: null
    })
  },

  // Handle deck name input
  handleDeckNameInput(e) {
    this.setData({ 'deckForm.name': e.detail.value })
  },

  // Handle deck description input
  handleDeckDescriptionInput(e) {
    this.setData({ 'deckForm.description': e.detail.value })
  },

  // Handle deck public checkbox
  handleDeckPublicChange(e) {
    this.setData({ 'deckForm.isPublic': e.detail.value })
  },

  // Save deck (create or update)
  async saveDeck() {
    if (!this.data.deckForm.name.trim()) {
      wx.showToast({ title: 'Please enter deck name', icon: 'none' })
      return
    }
    
    this.setData({ saving: true })
    
    try {
      if (this.data.showEditModal && this.data.selectedDeck) {
        // Update existing deck
        await deckService.updateDeck(this.data.selectedDeck.id, this.data.deckForm)
        wx.showToast({ title: 'Deck updated successfully!', icon: 'success' })
      } else {
        // Create new deck
        await deckService.createDeck(this.data.deckForm)
        wx.showToast({ title: 'Deck created successfully!', icon: 'success' })
      }
      
      this.closeModal()
      await this.loadDecks()
      await this.loadPublicDecks()
    } catch (error) {
      console.error('Error saving deck:', error)
      wx.showToast({ title: 'Failed to save deck', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  },

  // Confirm delete deck
  confirmDeleteDeck(e) {
    const deck = e.currentTarget.dataset.deck
    
    this.setData({
      selectedDeck: deck,
      showDeleteModal: true
    })
  },

  // Close delete modal
  closeDeleteModal() {
    this.setData({
      showDeleteModal: false,
      selectedDeck: null
    })
  },

  // Delete deck
  async deleteDeck() {
    if (!this.data.selectedDeck) return
    
    this.setData({ deleting: true })
    
    try {
      await deckService.deleteDeck(this.data.selectedDeck.id)
      wx.showToast({ title: 'Deck deleted successfully!', icon: 'success' })
      this.closeDeleteModal()
      await this.loadDecks()
      await this.loadPublicDecks()
    } catch (error) {
      console.error('Error deleting deck:', error)
      wx.showToast({ title: 'Failed to delete deck', icon: 'none' })
    } finally {
      this.setData({ deleting: false })
    }
  },

  // View deck cards
  viewDeckCards(e) {
    const deck = e.currentTarget.dataset.deck
    wx.navigateTo({
      url: `/pages/card-browser/card-browser?deckId=${deck.id}`
    })
  },

  // Start deck review
  startDeckReview(e) {
    const deck = e.currentTarget.dataset.deck
    wx.navigateTo({
      url: `/pages/review/review?deckId=${deck.id}`
    })
  },

  // Edit deck options
  editDeckOptions(e) {
    const deck = e.currentTarget.dataset.deck
    
    this.setData({
      selectedDeck: deck,
      deckOptions: {
        newCardsPerDay: deck.newCardsPerDay || 20,
        maxReviewsPerDay: deck.maxReviewsPerDay || 100,
        easyInterval: deck.easyInterval || 4,
        easyBonus: deck.easyBonus || 1.3,
        intervalModifier: deck.intervalModifier || 1.0,
        startingEase: deck.startingEase || 2.5,
        steps: deck.steps || 1
      },
      showOptionsModal: true
    })
  },

  // Close options modal
  closeOptionsModal() {
    this.setData({
      showOptionsModal: false,
      selectedDeck: null
    })
  },

  // Handle deck options input
  handleNewCardsInput(e) {
    this.setData({ 'deckOptions.newCardsPerDay': parseInt(e.detail.value) })
  },

  handleMaxReviewsInput(e) {
    this.setData({ 'deckOptions.maxReviewsPerDay': parseInt(e.detail.value) })
  },

  handleEasyIntervalInput(e) {
    this.setData({ 'deckOptions.easyInterval': parseInt(e.detail.value) })
  },

  handleEasyBonusInput(e) {
    this.setData({ 'deckOptions.easyBonus': parseFloat(e.detail.value) })
  },

  handleIntervalModifierInput(e) {
    this.setData({ 'deckOptions.intervalModifier': parseFloat(e.detail.value) })
  },

  handleStartingEaseInput(e) {
    this.setData({ 'deckOptions.startingEase': parseFloat(e.detail.value) })
  },

  handleStepsInput(e) {
    this.setData({ 'deckOptions.steps': parseInt(e.detail.value) })
  },

  // Save deck options
  async saveDeckOptions() {
    if (!this.data.selectedDeck) return
    
    this.setData({ saving: true })
    
    try {
      const updateData = {
        ...this.data.deckForm,
        ...this.data.deckOptions
      }
      
      await deckService.updateDeck(this.data.selectedDeck.id, updateData)
      wx.showToast({ title: 'Deck options updated!', icon: 'success' })
      this.closeOptionsModal()
      await this.loadDecks()
    } catch (error) {
      console.error('Error saving deck options:', error)
      wx.showToast({ title: 'Failed to save options', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  },

  // Add card to deck
  async addCardToDeck(e) {
    const deck = e.currentTarget.dataset.deck
    
    this.setData({
      selectedDeck: deck,
      addCardForm: { cardId: '' },
      showAddCardModal: true
    })
    
    await this.loadCards()
  },

  // Close add card modal
  closeAddCardModal() {
    this.setData({
      showAddCardModal: false,
      selectedDeck: null,
      addCardForm: { cardId: '' }
    })
  },

  // Handle card selection
  handleCardSelect(e) {
    this.setData({ 'addCardForm.cardId': e.detail.value })
  },

  // Save card to deck
  async saveCardToDeck() {
    if (!this.data.addCardForm.cardId) {
      wx.showToast({ title: 'Please select a card', icon: 'none' })
      return
    }
    
    this.setData({ saving: true })
    
    try {
      await deckService.addCardToDeck(
        this.data.selectedDeck.id,
        parseInt(this.data.addCardForm.cardId)
      )
      wx.showToast({ title: 'Card added to deck!', icon: 'success' })
      this.closeAddCardModal()
    } catch (error) {
      console.error('Error adding card to deck:', error)
      wx.showToast({ title: 'Failed to add card', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  },

  // Duplicate deck
  async duplicateDeck(e) {
    const deck = e.currentTarget.dataset.deck
    
    try {
      await deckService.duplicateDeck(deck.id)
      wx.showToast({ title: 'Deck duplicated!', icon: 'success' })
      await this.loadDecks()
    } catch (error) {
      console.error('Error duplicating deck:', error)
      wx.showToast({ title: 'Failed to duplicate deck', icon: 'none' })
    }
  },

  // Export deck
  exportDeck(e) {
    const deck = e.currentTarget.dataset.deck
    wx.showToast({ title: 'Export feature coming soon', icon: 'none' })
  },

  // Export all decks
  exportAllDecks() {
    wx.showToast({ title: 'Export feature coming soon', icon: 'none' })
  },

  // Import decks
  importDecks() {
    wx.showToast({ title: 'Import feature coming soon', icon: 'none' })
  },

  // View public deck cards
  viewPublicDeckCards(e) {
    const deck = e.currentTarget.dataset.deck
    wx.navigateTo({
      url: `/pages/card-browser/card-browser?deckId=${deck.id}&isPublic=true`
    })
  },

  // Start public deck review
  startPublicDeckReview(e) {
    const deck = e.currentTarget.dataset.deck
    wx.navigateTo({
      url: `/pages/review/review?deckId=${deck.id}&isPublic=true`
    })
  },

  // Duplicate public deck
  async duplicatePublicDeck(e) {
    const deck = e.currentTarget.dataset.deck
    
    try {
      await deckService.duplicateDeck(deck.id)
      wx.showToast({ title: 'Deck copied to your collection!', icon: 'success' })
      await this.loadDecks()
    } catch (error) {
      console.error('Error copying public deck:', error)
      wx.showToast({ title: 'Failed to copy deck', icon: 'none' })
    }
  }
})
