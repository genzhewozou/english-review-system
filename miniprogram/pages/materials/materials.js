const materialService = require('../../services/materialService');

Page({
  data: {
    materials: [],
    loading: false,
    showUploadModal: false,
    
    // Search and filter state
    searchQuery: '',
    selectedType: '',
    sortBy: 'newest',
    
    // Pagination state
    currentPage: 1,
    pageSize: 12,
    
    // Options for filters
    typeOptions: [
      { label: 'All Types', value: '' },
      { label: 'Documents', value: 'DOCUMENT' },
      { label: 'Videos', value: 'VIDEO' },
      { label: 'Articles', value: 'ARTICLE' }
    ],
    sortOptions: [
      { label: 'Newest First', value: 'newest' },
      { label: 'Oldest First', value: 'oldest' },
      { label: 'Name A-Z', value: 'name-asc' },
      { label: 'Name Z-A', value: 'name-desc' },
      { label: 'Largest First', value: 'size-desc' },
      { label: 'Smallest First', value: 'size-asc' }
    ],
    
    // Selected indices for pickers
    selectedTypeIndex: 0,
    sortByIndex: 0,
    
    // Upload modal state
    selectedFiles: [],
    
    // Computed properties (simulated)
    filteredMaterials: [],
    totalPages: 1,
    paginatedMaterials: []
  },

  onLoad() {
    this.loadMaterials();
  },

  // Load materials data
  async loadMaterials() {
    this.setData({ loading: true });
    
    try {
      const materials = await materialService.getAllMaterials();
      this.setData({ materials });
      this.updateFilteredMaterials();
    } catch (error) {
      console.error('Error loading materials:', error);
      // Set empty data if API fails
      this.setData({ materials: [] });
      this.updateFilteredMaterials();
      wx.showToast({ 
        title: 'Failed to load materials', 
        icon: 'none' 
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  // Update filtered materials based on search and filters
  updateFilteredMaterials() {
    const { materials, searchQuery, selectedType, sortBy, currentPage, pageSize } = this.data;
    
    let filtered = [...materials];
    
    // Apply search filter
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase().trim();
      filtered = filtered.filter(material =>
        material.title.toLowerCase().includes(query) ||
        material.fileName.toLowerCase().includes(query)
      );
    }
    
    // Apply type filter
    if (selectedType) {
      filtered = filtered.filter(material => material.type === selectedType);
    }
    
    // Apply sorting
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'newest':
          return new Date(b.createdDate) - new Date(a.createdDate);
        case 'oldest':
          return new Date(a.createdDate) - new Date(b.createdDate);
        case 'name-asc':
          return a.title.localeCompare(b.title);
        case 'name-desc':
          return b.title.localeCompare(a.title);
        case 'size-desc':
          return (b.fileSize || 0) - (a.fileSize || 0);
        case 'size-asc':
          return (a.fileSize || 0) - (b.fileSize || 0);
        default:
          return 0;
      }
    });
    
    // Calculate pagination
    const totalPages = Math.ceil(filtered.length / pageSize);
    const start = (currentPage - 1) * pageSize;
    const end = start + pageSize;
    const paginated = filtered.slice(start, end);
    
    this.setData({
      filteredMaterials: filtered,
      totalPages,
      paginatedMaterials: paginated
    });
  },

  // Search input handler
  handleSearchInput(e) {
    this.setData({ searchQuery: e.detail.value });
    this.setData({ currentPage: 1 });
    this.updateFilteredMaterials();
  },

  // Clear search
  clearSearch() {
    this.setData({ searchQuery: '' });
    this.setData({ currentPage: 1 });
    this.updateFilteredMaterials();
  },

  // Type filter change handler
  handleTypeChange(e) {
    const index = e.detail.value;
    const selectedType = this.data.typeOptions[index].value;
    this.setData({ 
      selectedType, 
      selectedTypeIndex: index,
      currentPage: 1 
    });
    this.updateFilteredMaterials();
  },

  // Sort change handler
  handleSortChange(e) {
    const index = e.detail.value;
    const sortBy = this.data.sortOptions[index].value;
    this.setData({ 
      sortBy, 
      sortByIndex: index,
      currentPage: 1 
    });
    this.updateFilteredMaterials();
  },

  // Get type label for display
  getTypeLabel(value) {
    const option = this.data.typeOptions.find(opt => opt.value === value);
    return option ? option.label : 'All Types';
  },

  // Get sort label for display
  getSortLabel(value) {
    const option = this.data.sortOptions.find(opt => opt.value === value);
    return option ? option.label : 'Newest First';
  },

  // Get item type label for display
  getItemTypeLabel(type) {
    const option = this.data.typeOptions.find(opt => opt.value === type);
    return option ? option.label : type;
  },

  // Pagination handlers
  prevPage() {
    if (this.data.currentPage > 1) {
      this.setData({ currentPage: this.data.currentPage - 1 });
      this.updateFilteredMaterials();
    }
  },

  nextPage() {
    if (this.data.currentPage < this.data.totalPages) {
      this.setData({ currentPage: this.data.currentPage + 1 });
      this.updateFilteredMaterials();
    }
  },

  // Modal handlers
  showUploadModal() {
    this.setData({ showUploadModal: true });
  },

  hideUploadModal() {
    this.setData({ showUploadModal: false });
  },

  // File upload handlers
  chooseFile() {
    const that = this;
    
    wx.chooseMessageFile({
      count: 9,
      type: 'all',
      success(res) {
        const newFiles = res.tempFiles.map(file => ({
          name: file.name,
          size: file.size,
          path: file.path
        }));
        
        that.setData({
          selectedFiles: [...that.data.selectedFiles, ...newFiles]
        });
      }
    });
  },

  removeFile(e) {
    const index = e.currentTarget.dataset.index;
    const files = [...this.data.selectedFiles];
    files.splice(index, 1);
    this.setData({ selectedFiles: files });
  },

  // Upload files
  async uploadFiles() {
    const that = this;
    
    // Simulate file upload for now
    wx.showLoading({ title: 'Uploading...' });
    
    try {
      // In a real implementation, we would upload files using wx.uploadFile
      // For now, we'll simulate the upload and then add the files to the list
      
      // Simulate API call to add materials
      const newMaterials = that.data.selectedFiles.map(file => ({
        id: Date.now() + Math.random(),
        title: file.name.replace(/\.[^/.]+$/, ''),
        fileName: file.name,
        type: that.getFileTypeFromName(file.name),
        fileSize: file.size,
        createdDate: new Date().toISOString()
      }));
      
      // Add new materials to the list
      const updatedMaterials = [...newMaterials, ...that.data.materials];
      that.setData({ materials: updatedMaterials });
      that.setData({ selectedFiles: [] });
      that.setData({ showUploadModal: false });
      
      wx.showToast({ title: 'Upload successful', icon: 'success' });
      that.updateFilteredMaterials();
    } catch (error) {
      console.error('Error uploading files:', error);
      wx.showToast({ title: 'Upload failed', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  // Get file type from filename
  getFileTypeFromName(filename) {
    const ext = filename.split('.').pop().toLowerCase();
    if (['pdf', 'docx', 'doc', 'txt'].includes(ext)) {
      return 'DOCUMENT';
    } else if (['mp4', 'avi', 'mov'].includes(ext)) {
      return 'VIDEO';
    } else {
      return 'ARTICLE';
    }
  },

  // Material actions
  viewMaterial(e) {
    const material = e.currentTarget.dataset.material;
    wx.navigateTo({
      url: `/pages/material-viewer/material-viewer?id=${material.id}`
    });
  },

  goToHighlighting(e) {
    const material = e.currentTarget.dataset.material;
    wx.navigateTo({
      url: `/pages/material-viewer/material-viewer?id=${material.id}&mode=highlight`
    });
  },

  // Delete material
  async deleteMaterial(e) {
    const material = e.currentTarget.dataset.material;
    const that = this;
    
    wx.showModal({
      title: 'Confirm Deletion',
      content: `Are you sure you want to delete "${material.title}"? This action cannot be undone.`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          try {
            await materialService.deleteMaterial(material.id);
            await that.loadMaterials();
            wx.showToast({ 
              title: 'Material deleted successfully', 
              icon: 'success' 
            });
          } catch (error) {
            console.error('Error deleting material:', error);
            wx.showToast({ 
              title: 'Failed to delete material', 
              icon: 'none' 
            });
          }
        }
      }
    });
  },

  // Clear all filters
  clearFilters() {
    this.setData({
      searchQuery: '',
      selectedType: '',
      sortBy: 'newest',
      selectedTypeIndex: 0,
      sortByIndex: 0,
      currentPage: 1
    });
    this.updateFilteredMaterials();
  },

  // Utility functions
  formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  },

  formatDate(timestamp) {
    return new Date(timestamp).toLocaleDateString();
  },

  getMaterialIcon(type) {
    const iconMap = {
      'DOCUMENT': '📄',
      'VIDEO': '🎥',
      'ARTICLE': '📝'
    };
    return iconMap[type] || '📄';
  }
});