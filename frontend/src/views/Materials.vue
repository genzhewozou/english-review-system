<template>
  <div class="materials">
    <div class="materials-header">
      <h2>Study Materials</h2>
      <el-button type="primary" @click="showUploadModal = true" :icon="Plus">
        Upload Material
      </el-button>
    </div>

    <!-- Search and Filter Controls -->
    <div class="materials-controls">
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          placeholder="Search materials by title or filename..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>
      
      <div class="filter-section">
        <el-select
          v-model="selectedType"
          placeholder="Filter by type"
          clearable
          class="type-filter"
        >
          <el-option label="All Types" value="" />
          <el-option label="Documents" value="DOCUMENT" />
          <el-option label="Videos" value="VIDEO" />
          <el-option label="Articles" value="ARTICLE" />
        </el-select>
        
        <el-select
          v-model="sortBy"
          placeholder="Sort by"
          class="sort-select"
        >
          <el-option label="Newest First" value="newest" />
          <el-option label="Oldest First" value="oldest" />
          <el-option label="Name A-Z" value="name-asc" />
          <el-option label="Name Z-A" value="name-desc" />
          <el-option label="Largest First" value="size-desc" />
          <el-option label="Smallest First" value="size-asc" />
        </el-select>
      </div>
    </div>

    <!-- Upload Modal -->
    <el-dialog
      v-model="showUploadModal"
      title="Upload Study Materials"
      width="90%"
      :max-width="800"
      @close="handleUploadModalClose"
    >
      <FileUpload
        @upload-complete="handleUploadComplete"
        @upload-error="handleUploadError"
      />
    </el-dialog>
    
    <!-- Materials List -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="filteredMaterials.length === 0" class="empty-state">
      <el-empty
        :description="materials.length === 0 ? 'No materials uploaded yet' : 'No materials match your search criteria'"
      >
        <el-button v-if="materials.length === 0" type="primary" @click="showUploadModal = true">
          Upload Your First Material
        </el-button>
        <el-button v-else @click="clearFilters">
          Clear Filters
        </el-button>
      </el-empty>
    </div>
    
    <div v-else class="materials-grid">
      <MaterialCard
        v-for="material in paginatedMaterials"
        :key="material.id"
        :material="material"
        @view="viewMaterial"
        @delete="deleteMaterial"
        @highlight="goToHighlighting"
      />
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="filteredMaterials.length"
        layout="prev, pager, next, jumper, total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useApiService } from '../composables/useApiService'
import { useMaterialService } from '../services/materialService'
import FileUpload from '../components/FileUpload.vue'
import MaterialCard from '../components/MaterialCard.vue'

export default {
  name: 'Materials',
  components: {
    FileUpload,
    MaterialCard
  },
  setup() {
    const router = useRouter()
    const materials = ref([])
    const loading = ref(false)
    const showUploadModal = ref(false)
    
    // Search and filter state
    const searchQuery = ref('')
    const selectedType = ref('')
    const sortBy = ref('newest')
    
    // Pagination state
    const currentPage = ref(1)
    const pageSize = ref(12)
    
    const { getAllMaterials, deleteMaterial: deleteMaterialApi } = useMaterialService()
    
    // Computed properties
    const filteredMaterials = computed(() => {
      let filtered = [...materials.value]
      
      // Apply search filter
      if (searchQuery.value.trim()) {
        const query = searchQuery.value.toLowerCase().trim()
        filtered = filtered.filter(material =>
          material.title.toLowerCase().includes(query) ||
          material.fileName.toLowerCase().includes(query)
        )
      }
      
      // Apply type filter
      if (selectedType.value) {
        filtered = filtered.filter(material => material.type === selectedType.value)
      }
      
      // Apply sorting
      filtered.sort((a, b) => {
        switch (sortBy.value) {
          case 'newest':
            return new Date(b.createdDate) - new Date(a.createdDate)
          case 'oldest':
            return new Date(a.createdDate) - new Date(b.createdDate)
          case 'name-asc':
            return a.title.localeCompare(b.title)
          case 'name-desc':
            return b.title.localeCompare(a.title)
          case 'size-desc':
            return (b.fileSize || 0) - (a.fileSize || 0)
          case 'size-asc':
            return (a.fileSize || 0) - (b.fileSize || 0)
          default:
            return 0
        }
      })
      
      return filtered
    })
    
    const totalPages = computed(() => {
      return Math.ceil(filteredMaterials.value.length / pageSize.value)
    })
    
    const paginatedMaterials = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filteredMaterials.value.slice(start, end)
    })
    
    // Methods
    const loadMaterials = async () => {
      loading.value = true
      try {
        materials.value = await getAllMaterials()
      } catch (error) {
        console.error('Error loading materials:', error)
        ElMessage.error('Failed to load materials')
        materials.value = []
      } finally {
        loading.value = false
      }
    }
    
    const handleUploadComplete = async (uploadedFiles) => {
      showUploadModal.value = false
      await loadMaterials()
      ElMessage.success(`Successfully uploaded ${uploadedFiles.length} file(s)`)
    }
    
    const handleUploadError = (error) => {
      console.error('Upload error:', error)
      ElMessage.error('Upload failed. Please try again.')
    }
    
    const handleUploadModalClose = () => {
      showUploadModal.value = false
    }
    
    const viewMaterial = (material) => {
      router.push(`/materials/${material.id}`)
    }
    
    const goToHighlighting = (material) => {
      router.push(`/materials/${material.id}?mode=highlight`)
    }
    
    const deleteMaterial = async (material) => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete "${material.title}"? This action cannot be undone.`,
          'Confirm Deletion',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning',
            confirmButtonClass: 'el-button--danger'
          }
        )
        
        await deleteMaterialApi(material.id)
        await loadMaterials()
        ElMessage.success('Material deleted successfully')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error deleting material:', error)
          ElMessage.error('Failed to delete material')
        }
      }
    }
    
    const clearFilters = () => {
      searchQuery.value = ''
      selectedType.value = ''
      sortBy.value = 'newest'
      currentPage.value = 1
    }
    
    const handlePageChange = (page) => {
      currentPage.value = page
      // Scroll to top of materials list
      document.querySelector('.materials-grid')?.scrollIntoView({ behavior: 'smooth' })
    }
    
    // Watch for filter changes to reset pagination
    watch([searchQuery, selectedType, sortBy], () => {
      currentPage.value = 1
    })
    
    onMounted(() => {
      loadMaterials()
    })
    
    return {
      materials,
      loading,
      showUploadModal,
      searchQuery,
      selectedType,
      sortBy,
      currentPage,
      pageSize,
      filteredMaterials,
      totalPages,
      paginatedMaterials,
      handleUploadComplete,
      handleUploadError,
      handleUploadModalClose,
      viewMaterial,
      goToHighlighting,
      deleteMaterial,
      clearFilters,
      handlePageChange,
      Plus,
      Search
    }
  }
}
</script>

<style scoped>
.materials {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
}

.materials-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.materials-header h2 {
  margin: 0;
  color: #303133;
  font-size: 1.75rem;
  font-weight: 600;
}

.materials-controls {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.search-section {
  width: 100%;
}

.search-input {
  max-width: 400px;
}

.filter-section {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.type-filter,
.sort-select {
  min-width: 150px;
}

.loading-container {
  padding: 2rem;
}

.empty-state {
  padding: 3rem 1rem;
  text-align: center;
}

.materials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 2rem 0;
}

/* Responsive design */
@media (min-width: 768px) {
  .materials-controls {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-section {
    flex: 1;
    max-width: 400px;
  }
  
  .filter-section {
    flex-shrink: 0;
  }
}

@media (max-width: 768px) {
  .materials {
    padding: 0.5rem;
  }
  
  .materials-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .materials-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-section {
    flex-direction: column;
  }
  
  .type-filter,
  .sort-select {
    width: 100%;
  }
}
</style>