const vocabularyService = require('../../services/vocabularyService')
const materialService = require('../../services/materialService')

Page({
  data: {
    cards: [],
    materials: [],
    loading: false,
    updating: false,
    saving: false,
    showEditModal: false,
    showAddCardModal: false,
    showTagGuideModal: false,
    isDropdownOpen: false,
    selectedMaterial: '',
    selectedTag: '',
    searchTerm: '',
    selectedMaterialIndex: 0,
    selectedTagIndex: 0,
    
    editForm: {
      id: null,
      text: '',
      userComment: '',
      tags: []
    },
    
    tagForm: {
      name: '',
      description: ''
    },
    
    cardForm: {
      text: '',
      context: '',
      userComment: '',
      materialId: '',
      tags: []
    },
    
    tags: [],
    selectedTags: [],
    showTagManagementModal: false,
    cardFormMaterialIndex: 0,
    
    // Computed properties (simulated)
    filteredCards: []
  },

  onLoad() {
    this.loadMaterials()
    this.loadCards()
    this.loadTags()
  },

  // Load materials data
  async loadMaterials() {
    try {
      const materials = await materialService.getAllMaterials()
      this.setData({ materials })
    } catch (error) {
      console.error('Error loading materials:', error)
      this.setData({ materials: [] })
      wx.showToast({ title: 'Failed to load materials', icon: 'none' })
    }
  },

  // Load cards data
  async loadCards() {
    this.setData({ loading: true })
    
    try {
      let cards
      if (this.data.selectedMaterial) {
        cards = await vocabularyService.getCardsByMaterial(this.data.selectedMaterial)
      } else {
        cards = await vocabularyService.getAllCards()
      }
      this.setData({ cards })
      this.updateFilteredCards()
    } catch (error) {
      console.error('Error loading cards:', error)
      this.setData({ cards: [] })
      this.updateFilteredCards()
      wx.showToast({ title: 'Failed to load cards', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  // Load tags data
  async loadTags() {
    try {
      const tags = await vocabularyService.getAllTags()
      this.setData({ tags })
    } catch (error) {
      console.error('Error loading tags:', error)
      this.setData({ tags: [] })
      wx.showToast({ title: 'Failed to load tags', icon: 'none' })
    }
  },

  // Update filtered cards based on filters
  updateFilteredCards() {
    let filtered = [...this.data.cards];
    
    if (this.data.selectedMaterial) {
      const materialId = parseInt(this.data.selectedMaterial);
      filtered = filtered.filter(c => c.materialId === materialId);
    }
    
    if (this.data.selectedTag) {
      const tagId = parseInt(this.data.selectedTag);
      filtered = filtered.filter(c => {
        return c.tags && c.tags.includes(tagId);
      });
    }
    
    if (this.data.searchTerm) {
      const term = this.data.searchTerm.toLowerCase();
      filtered = filtered.filter(c => {
        const textMatch = c.text && c.text.toLowerCase().includes(term);
        const commentMatch = c.userComment && c.userComment.toLowerCase().includes(term);
        const contextMatch = c.context && c.context.toLowerCase().includes(term);
        return textMatch || commentMatch || contextMatch;
      });
    }
    
    this.setData({ filteredCards: filtered });
  },

  // Handle material change
  handleMaterialChange(e) {
    const index = e.detail.value
    const materialId = index > 0 ? this.data.materials[index - 1].id : ''
    this.setData({ 
      selectedMaterial: materialId,
      selectedMaterialIndex: index
    })
    this.loadCards()
  },

  // Handle tag change
  handleTagChange(e) {
    const index = e.detail.value;
    const tagId = index > 0 ? this.data.tags[index - 1].id : '';
    this.setData({ 
      selectedTag: tagId,
      selectedTagIndex: index
    });
    this.updateFilteredCards();
  },

  // Handle search input
  handleSearchInput(e) {
    this.setData({ searchTerm: e.detail.value });
    this.updateFilteredCards();
  },

  // Clear search
  clearSearch() {
    this.setData({ searchTerm: '' });
    this.updateFilteredCards();
  },

  // Clear all filters
  clearFilters() {
    this.setData({
      selectedMaterial: '',
      selectedTag: '',
      searchTerm: '',
      selectedMaterialIndex: 0,
      selectedTagIndex: 0
    })
    this.loadCards()
  },

  // Toggle dropdown
  toggleDropdown() {
    this.setData({ isDropdownOpen: !this.data.isDropdownOpen });
  },

  // Show add card modal
  showAddCardModal() {
    this.setData({
      cardForm: {
        text: '',
        context: '',
        userComment: '',
        materialId: '',
        tags: []
      },
      cardFormMaterialIndex: 0,
      showAddCardModal: true
    });
  },

  // Close add card modal
  closeAddCardModal() {
    this.setData({ showAddCardModal: false });
  },

  // Handle card text input
  handleCardTextInput(e) {
    this.setData({ 'cardForm.text': e.detail.value });
  },

  // Handle card context input
  handleCardContextInput(e) {
    this.setData({ 'cardForm.context': e.detail.value });
  },

  // Handle card comment input
  handleCardCommentInput(e) {
    this.setData({ 'cardForm.userComment': e.detail.value });
  },

  // Handle card material change
  handleCardMaterialChange(e) {
    const index = e.detail.value;
    const materialId = index > 0 ? this.data.materials[index - 1].id : '';
    this.setData({ 
      'cardForm.materialId': materialId,
      cardFormMaterialIndex: index
    });
  },

  // Handle card tag checkbox change
  handleCardTagCheckboxChange(e) {
    const tagId = parseInt(e.currentTarget.dataset.tagid);
    const tags = [...this.data.cardForm.tags];
    
    if (e.detail.value.includes(tagId.toString())) {
      if (!tags.includes(tagId)) {
        tags.push(tagId);
      }
    } else {
      const index = tags.indexOf(tagId);
      if (index > -1) {
        tags.splice(index, 1);
      }
    }
    
    this.setData({ 'cardForm.tags': tags });
  },

  // Add new card
  async addCard() {
    if (!this.data.cardForm.text || !this.data.cardForm.materialId) {
      wx.showToast({ title: 'Please fill in all required fields', icon: 'none' })
      return
    }
    
    this.setData({ saving: true })
    
    try {
      const cardData = {
        text: this.data.cardForm.text,
        context: this.data.cardForm.context,
        userComment: this.data.cardForm.userComment,
        materialId: parseInt(this.data.cardForm.materialId),
        tags: this.data.cardForm.tags
      }
      
      await vocabularyService.createCard(cardData)
      
      this.setData({ showAddCardModal: false })
      await this.loadCards()
      wx.showToast({ title: 'Card added successfully!', icon: 'success' })
    } catch (error) {
      console.error('Error adding card:', error)
      wx.showToast({ title: 'Failed to add card', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  },

  // Edit card
  editCard(e) {
    const card = e.currentTarget.dataset.card;
    
    this.setData({
      editForm: {
        id: card.id,
        text: card.text,
        userComment: card.userComment || '',
        tags: card.tags || []
      },
      showEditModal: true
    });
  },

  // Close edit modal
  closeEditModal() {
    this.setData({ 
      showEditModal: false,
      editForm: {
        id: null,
        text: '',
        userComment: '',
        tags: []
      }
    });
  },

  // Handle edit comment input
  handleEditCommentInput(e) {
    this.setData({ 'editForm.userComment': e.detail.value });
  },

  // Handle tag checkbox change for edit form
  handleTagCheckboxChange(e) {
    const tagId = parseInt(e.currentTarget.dataset.tagid);
    const tags = [...this.data.editForm.tags];
    
    if (e.detail.value.includes(tagId.toString())) {
      if (!tags.includes(tagId)) {
        tags.push(tagId);
      }
    } else {
      const index = tags.indexOf(tagId);
      if (index > -1) {
        tags.splice(index, 1);
      }
    }
    
    this.setData({ 'editForm.tags': tags });
  },

  // Update card
  async updateCard() {
    this.setData({ updating: true })
    
    try {
      const updateData = {
        userComment: this.data.editForm.userComment,
        tags: this.data.editForm.tags
      }
      
      await vocabularyService.updateCard(this.data.editForm.id, updateData)
      
      this.setData({ showEditModal: false })
      await this.loadCards()
      wx.showToast({ title: 'Card updated successfully!', icon: 'success' })
    } catch (error) {
      console.error('Error updating card:', error)
      wx.showToast({ title: 'Failed to update card', icon: 'none' })
    } finally {
      this.setData({ updating: false })
    }
  },

  // Delete card
  async deleteCard(e) {
    const cardId = e.currentTarget.dataset.id
    const card = this.data.cards.find(c => c.id === cardId)
    
    wx.showModal({
      title: 'Confirm Delete',
      content: `Are you sure you want to delete "${card.text}"?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          try {
            await vocabularyService.deleteCard(cardId)
            await this.loadCards()
            wx.showToast({ title: 'Card deleted successfully!', icon: 'success' })
          } catch (error) {
            console.error('Error deleting card:', error)
            wx.showToast({ title: 'Failed to delete card', icon: 'none' })
          }
        }
      }
    })
  },

  // Open tag management
  openTagManagement() {
    this.setData({ showTagManagementModal: true });
  },

  // Close tag management modal
  closeTagManagementModal() {
    this.setData({ 
      showTagManagementModal: false,
      tagForm: {
        name: '',
        description: ''
      }
    });
  },

  // Handle tag name input
  handleTagNameInput(e) {
    this.setData({ 'tagForm.name': e.detail.value });
  },

  // Handle tag description input
  handleTagDescriptionInput(e) {
    this.setData({ 'tagForm.description': e.detail.value });
  },

  // Add tag in management
  async addTagInManagement() {
    if (!this.data.tagForm.name) {
      wx.showToast({ title: 'Please enter tag name', icon: 'none' })
      return
    }
    
    this.setData({ saving: true })
    
    try {
      await vocabularyService.createTag(this.data.tagForm)
      
      this.setData({ 
        tagForm: {
          name: '',
          description: ''
        }
      })
      
      await this.loadTags()
      wx.showToast({ title: 'Tag added successfully!', icon: 'success' })
    } catch (error) {
      console.error('Error adding tag:', error)
      this.setData({ 
        tagForm: {
          name: '',
          description: ''
        }
      })
      wx.showToast({ title: 'Failed to add tag', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  },

  // Check if tag is used
  isTagUsed(tagId) {
    return this.data.cards.some(card => {
      return card.tags && card.tags.includes(tagId);
    });
  },

  // Delete tag
  async deleteTag(e) {
    const tagId = parseInt(e.currentTarget.dataset.id)
    const tagName = e.currentTarget.dataset.name
    
    if (this.isTagUsed(tagId)) {
      wx.showToast({ title: `Cannot delete tag "${tagName}" because it is used by some cards.`, icon: 'none' })
      return
    }
    
    wx.showModal({
      title: 'Confirm Delete',
      content: `Are you sure you want to delete tag "${tagName}"?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          try {
            await vocabularyService.deleteTag(tagId)
            await this.loadTags()
            wx.showToast({ title: 'Tag deleted successfully!', icon: 'success' })
          } catch (error) {
            console.error('Error deleting tag:', error)
            wx.showToast({ title: 'Failed to delete tag', icon: 'none' })
          }
        }
      }
    })
  },

  // Open tag guide
  openTagGuide() {
    this.setData({ showTagGuideModal: true });
  },

  // Close tag guide modal
  closeTagGuideModal() {
    this.setData({ showTagGuideModal: false });
  },

  // Get material title by id
  getMaterialTitle(materialId) {
    if (!materialId) return 'All Materials';
    const material = this.data.materials.find(m => m.id === materialId);
    return material ? material.title : 'Unknown Material';
  },

  // Get tag name by id
  getTagName(tagId) {
    if (!tagId) return 'All Tags';
    const tag = this.data.tags.find(t => t.id === tagId);
    return tag ? tag.name : 'Unknown Tag';
  },

  // Format date
  formatDate(dateString) {
    return new Date(dateString).toLocaleDateString();
  }
});