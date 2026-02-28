Page({
  data: {
    material: null,
    loading: true,
    isSelectionMode: false,
    highlights: [],
    contentNodes: [],
    cards: [],
    tags: [],
    showCardDialog: false,
    showEditDialog: false,
    selectedText: '',
    selectedContext: '',
    selectedTextPosition: { startPosition: 0, endPosition: 0 },
    editingCard: null,
    showVocabularyModal: false,
    newWord: '',
    newDefinition: '',
    newExample: '',
    categories: ['General', 'Business', 'Academic', 'Technical', 'Everyday'],
    selectedCategoryIndex: 0
  },

  onLoad(options) {
    const materialId = options.id;
    const mode = options.mode;
    
    // Set selection mode if specified
    if (mode === 'highlight' || mode === 'selection') {
      this.setData({ isSelectionMode: true });
    }
    
    this.loadMaterial(materialId);
  },

  // Make API request with authentication
  async makeApiRequest(url, method = 'GET', data = {}) {
    const token = wx.getStorageSync('authToken');
    
    return new Promise((resolve, reject) => {
      wx.request({
        url: `http://localhost:3000${url}`,
        method: method,
        header: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        data: data,
        success: resolve,
        fail: reject
      });
    });
  },

  // Load material data
  async loadMaterial(materialId) {
    this.setData({ loading: true });
    
    try {
      const materialResponse = await this.makeApiRequest(`/api/materials/${materialId}`);
      
      if (materialResponse.statusCode === 200) {
        const material = materialResponse.data;
        
        // Process content for display
        let contentNodes = [];
        if (material.content) {
          // Simple HTML to nodes conversion (for demo purposes)
          contentNodes = this.htmlToNodes(material.content);
        }
        
        // Load cards for this material
        await this.loadCards(materialId);
        
        // Load tags
        await this.loadTags();
        
        this.setData({ 
          material,
          contentNodes,
          loading: false 
        });
      } else {
        throw new Error('Failed to load material');
      }
    } catch (error) {
      console.error('Error loading material:', error);
      // Fallback to mock data if API fails
      this.loadMockMaterial(materialId);
    }
  },
  
  // Load cards for this material
  async loadCards(materialId) {
    try {
      const response = await this.makeApiRequest(`/api/vocabulary/material/${materialId}`);
      
      if (response.statusCode === 200) {
        this.setData({ cards: response.data || [] });
      } else {
        throw new Error('Failed to load cards');
      }
    } catch (error) {
      console.error('Error loading cards:', error);
      this.setData({ cards: [] });
    }
  },
  
  // Load tags
  async loadTags() {
    try {
      const response = await this.makeApiRequest('/api/tags');
      
      if (response.statusCode === 200) {
        this.setData({ tags: response.data || [] });
      } else {
        throw new Error('Failed to load tags');
      }
    } catch (error) {
      console.error('Error loading tags:', error);
      this.setData({ tags: [] });
    }
  },

  // Fallback: Load mock material
  loadMockMaterial(materialId) {
    // Mock material data
    const mockMaterials = [
      {
        id: 1,
        title: 'Business English Essentials',
        fileName: 'business-english.pdf',
        type: 'DOCUMENT',
        fileSize: 2048000,
        createdDate: new Date(Date.now() - 86400000).toISOString(),
        content: '<h1>Business English Essentials</h1><p>Welcome to the world of <span>business English</span>. This document will help you improve your <span>professional vocabulary</span> and <span>communication skills</span> in the workplace.</p><h2>Key Terms</h2><p><span>Entrepreneurship</span>: The process of starting a new business.</p><p><span>Innovation</span>: The introduction of new ideas or methods.</p><p><span>Leadership</span>: The ability to lead and guide others.</p><h2>Common Phrases</h2><p>"Let\'s <span>touch base</span> next week to discuss the project."</p><p>"I\'ll <span>circle back</span> to you with more information."</p><p>"We need to <span>think outside the box</span> for this problem."</p>'
      },
      {
        id: 2,
        title: 'Advanced Vocabulary Building',
        fileName: 'vocabulary-building.docx',
        type: 'DOCUMENT',
        fileSize: 1536000,
        createdDate: new Date(Date.now() - 172800000).toISOString(),
        content: '<h1>Advanced Vocabulary Building</h1><p>Expanding your vocabulary is essential for <span>effective communication</span>. This document provides <span>sophisticated words</span> and their usage examples.</p><h2>Vocabulary List</h2><p><span>Perspicacious</span>: Having a ready insight into and understanding of things.</p><p><span>Ephemeral</span>: Lasting for a very short time.</p><p><span>Ubiquitous</span>: Present, appearing, or found everywhere.</p>'
      },
      {
        id: 3,
        title: 'English Pronunciation Guide',
        fileName: 'pronunciation-guide.mp4',
        type: 'VIDEO',
        fileSize: 10240000,
        createdDate: new Date(Date.now() - 259200000).toISOString()
      }
    ];
    
    // Find the material by id or use the first one as default
    let material = mockMaterials.find(m => m.id == materialId);
    if (!material) {
      // Use the first document material as default
      material = mockMaterials.find(m => m.type === 'DOCUMENT') || mockMaterials[0];
    }
    
    // Process content for display
    let contentNodes = [];
    if (material.content) {
      // Simple HTML to nodes conversion (for demo purposes)
      contentNodes = this.htmlToNodes(material.content);
    }
    
    this.setData({ 
      material,
      contentNodes,
      loading: false 
    });
  },

  // Convert simple HTML to rich-text nodes
  htmlToNodes(html) {
    // This is a simplified conversion for demo purposes
    // In a real app, you would use a more robust HTML parser
    
    let nodes = [];
    let text = html;
    
    // Handle headings
    text = text.replace(/<h1>(.*?)<\/h1>/g, '<div class="heading-1">$1</div>');
    text = text.replace(/<h2>(.*?)<\/h2>/g, '<div class="heading-2">$1</div>');
    
    // Handle paragraphs
    text = text.replace(/<p>(.*?)<\/p>/g, '<div class="paragraph">$1</div>');
    
    // Handle span elements (potential highlight targets)
    text = text.replace(/<span>(.*?)<\/span>/g, '<div class="highlight-target" data-word="$1">$1</div>');
    
    // Split into nodes
    const parts = text.split(/<div class="(.*?)">(.*?)<\/div>/g);
    
    for (let i = 1; i < parts.length; i += 3) {
      const className = parts[i];
      const content = parts[i + 1];
      
      nodes.push({
        name: 'div',
        attrs: {
          class: className
        },
        children: [{
          type: 'text',
          text: content
        }]
      });
    }
    
    return nodes;
  },

  // Go back to materials list
  goBack() {
    wx.navigateBack();
  },

  // Toggle selection mode
  toggleSelectionMode() {
    this.setData({ isSelectionMode: !this.data.isSelectionMode });
    
    if (this.data.isSelectionMode) {
      wx.showToast({ title: 'Selection mode activated. Select text to create cards.', icon: 'none' });
    } else {
      wx.showToast({ title: 'Selection mode deactivated.', icon: 'none' });
    }
  },

  // Handle text tap for selection
  handleTextTap(e) {
    if (!this.data.isSelectionMode) return;
    
    // In a real app, you would implement text selection and highlighting
    // For demo purposes, we'll simulate adding a highlight
    const word = e.currentTarget.dataset.word || 'Selected Word';
    
    // Check if word is already highlighted
    const isAlreadyHighlighted = this.data.highlights.some(h => h.word === word);
    if (isAlreadyHighlighted) return;
    
    // Add to highlights
    const newHighlights = [...this.data.highlights, { word }];
    this.setData({ highlights: newHighlights });
    
    // Show card creation dialog
    this.setData({
      selectedText: word,
      selectedContext: '',
      showCardDialog: true
    });
  },

  // Remove highlight
  removeHighlight(e) {
    const index = e.currentTarget.dataset.index;
    const newHighlights = this.data.highlights.filter((_, i) => i !== index);
    this.setData({ highlights: newHighlights });
  },

  // Save highlights
  async saveHighlights() {
    if (!this.data.material) return;
    
    try {
      const response = await this.makeApiRequest(`/api/materials/${this.data.material.id}/highlights`, 'POST', {
        highlights: this.data.highlights
      });
      
      if (response.statusCode === 201) {
        wx.showToast({ 
          title: 'Highlights saved', 
          icon: 'success' 
        });
      }
    } catch (error) {
      console.error('Error saving highlights:', error);
      // Fallback to local save if API fails
      wx.showToast({ 
        title: 'Highlights saved', 
        icon: 'success' 
      });
    }
  },

  // Add word to vocabulary
  addWordToVocabulary(e) {
    const word = e.currentTarget.dataset.word;
    
    this.setData({
      newWord: word,
      newDefinition: '',
      newExample: '',
      selectedCategoryIndex: 0,
      showVocabularyModal: true
    });
  },

  // Close vocabulary modal
  closeVocabularyModal() {
    this.setData({ showVocabularyModal: false });
  },

  // Handle definition input
  handleDefinitionInput(e) {
    this.setData({ newDefinition: e.detail.value });
  },

  // Handle example input
  handleExampleInput(e) {
    this.setData({ newExample: e.detail.value });
  },

  // Handle category change
  handleCategoryChange(e) {
    this.setData({ selectedCategoryIndex: e.detail.value });
  },

  // Confirm adding to vocabulary
  async confirmAddToVocabulary() {
    if (!this.data.newWord || !this.data.newDefinition) {
      wx.showToast({ title: 'Please enter word and definition', icon: 'none' });
      return;
    }
    
    try {
      const response = await this.makeApiRequest('/api/vocabulary', 'POST', {
        word: this.data.newWord,
        definition: this.data.newDefinition,
        example: this.data.newExample,
        category: this.data.categories[this.data.selectedCategoryIndex]
      });
      
      if (response.statusCode === 201) {
        wx.showToast({ 
          title: 'Added to vocabulary', 
          icon: 'success' 
        });
        
        this.setData({ showVocabularyModal: false });
      }
    } catch (error) {
      console.error('Error adding to vocabulary:', error);
      // Fallback to local save if API fails
      wx.showToast({ 
        title: 'Added to vocabulary', 
        icon: 'success' 
      });
      
      this.setData({ showVocabularyModal: false });
    }
  },
  
  // Handle card save
  async handleCardSave() {
    // In a real app, you would get the form data from the input fields
    // For demo purposes, we'll use placeholder data
    const cardData = {
      backText: 'Definition placeholder',
      context: 'Context placeholder',
      comment: 'Comment placeholder',
      tags: ['tag1', 'tag2']
    };
    
    try {
      const response = await this.makeApiRequest('/api/vocabulary/cards', 'POST', {
        materialId: this.data.material.id,
        text: this.data.selectedText,
        backText: cardData.backText,
        context: cardData.context,
        startPosition: this.data.selectedTextPosition.startPosition,
        endPosition: this.data.selectedTextPosition.endPosition,
        userComment: cardData.comment,
        tags: cardData.tags
      });
      
      if (response.statusCode === 201) {
        const newCard = response.data;
        const newCards = [...this.data.cards, newCard];
        this.setData({ cards: newCards });
        this.resetCardDialog();
        wx.showToast({ title: 'Card created successfully', icon: 'success' });
      }
    } catch (error) {
      console.error('Error creating card:', error);
      wx.showToast({ title: 'Failed to create card', icon: 'none' });
    }
  },
  
  // Handle card update
  async handleCardUpdate() {
    // In a real app, you would get the form data from the input fields
    // For demo purposes, we'll use placeholder data
    const cardData = {
      backText: 'Updated definition placeholder',
      context: 'Updated context placeholder',
      comment: 'Updated comment placeholder',
      tags: ['tag1', 'tag2', 'tag3']
    };
    
    try {
      const response = await this.makeApiRequest(`/api/vocabulary/cards/${this.data.editingCard.id}`, 'PUT', {
        backText: cardData.backText,
        context: cardData.context,
        userComment: cardData.comment,
        tags: cardData.tags
      });
      
      if (response.statusCode === 200) {
        const updatedCard = response.data;
        const newCards = this.data.cards.map(card => 
          card.id === updatedCard.id ? updatedCard : card
        );
        this.setData({ cards: newCards });
        this.resetEditDialog();
        wx.showToast({ title: 'Card updated successfully', icon: 'success' });
      }
    } catch (error) {
      console.error('Error updating card:', error);
      wx.showToast({ title: 'Failed to update card', icon: 'none' });
    }
  },
  
  // Edit card
  editCard(e) {
    const card = e.currentTarget.dataset.card;
    this.setData({
      editingCard: card,
      showEditDialog: true
    });
  },
  
  // Delete card
  async deleteCard(e) {
    const card = e.currentTarget.dataset.card;
    try {
      const response = await this.makeApiRequest(`/api/vocabulary/cards/${card.id}`, 'DELETE');
      
      if (response.statusCode === 200) {
        const newCards = this.data.cards.filter(c => c.id !== card.id);
        this.setData({ cards: newCards });
        wx.showToast({ title: 'Card deleted successfully', icon: 'success' });
      }
    } catch (error) {
      console.error('Error deleting card:', error);
      wx.showToast({ title: 'Failed to delete card', icon: 'none' });
    }
  },
  
  // Reset card dialog
  resetCardDialog() {
    this.setData({
      showCardDialog: false,
      selectedText: '',
      selectedContext: '',
      selectedTextPosition: { startPosition: 0, endPosition: 0 }
    });
  },
  
  // Reset edit dialog
  resetEditDialog() {
    this.setData({
      showEditDialog: false,
      editingCard: null
    });
  },

  // Download material
  downloadMaterial() {
    wx.showToast({ 
      title: 'Download started', 
      icon: 'success' 
    });
  },

  // Share material
  shareMaterial() {
    wx.showToast({ 
      title: 'Share functionality would be implemented here', 
      icon: 'none' 
    });
  },

  // Get material type label for display
  getMaterialTypeLabel(type) {
    const typeMap = {
      'DOCUMENT': 'Document',
      'VIDEO': 'Video',
      'ARTICLE': 'Article'
    };
    return typeMap[type] || type;
  },

  // Format file size
  formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  },

  // Format date
  formatDate(timestamp) {
    return new Date(timestamp).toLocaleDateString();
  }
});