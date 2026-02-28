/**
 * Material Service for WeChat Mini Program
 * Matches frontend/src/services/materialService.js
 */

const api = require('../utils/api')

/**
 * Upload a new study material
 */
function uploadMaterial(filePath, title, type, onProgress = null) {
  const formData = {
    title: title,
    type: type
  }
  
  return api.upload('/materials', filePath, formData, onProgress)
    .then(res => res.data)
    .catch(err => {
      console.error('Failed to upload material:', err)
      throw new Error('Failed to upload material')
    })
}

/**
 * Get all study materials
 */
function getAllMaterials() {
  return api.get('/materials')
    .then(res => res.data || [])
    .catch(err => {
      console.error('Failed to fetch materials:', err)
      throw new Error('Failed to fetch materials')
    })
}

/**
 * Get a specific material by ID
 */
function getMaterial(id) {
  return api.get(`/materials/${id}`)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to fetch material ${id}:`, err)
      throw new Error('Failed to fetch material')
    })
}

/**
 * Delete a material by ID
 */
function deleteMaterial(id) {
  return api.delete(`/materials/${id}`)
    .catch(err => {
      console.error(`Failed to delete material ${id}:`, err)
      throw new Error('Failed to delete material')
    })
}

/**
 * Download a material file
 */
function downloadMaterial(id) {
  return api.download(`/materials/${id}/download`)
    .catch(err => {
      console.error(`Failed to download material ${id}:`, err)
      throw new Error('Failed to download material')
    })
}

/**
 * Get material content for viewing
 */
function getMaterialContent(id) {
  return api.get(`/materials/${id}/content`)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to fetch material content ${id}:`, err)
      throw new Error('Failed to fetch material content')
    })
}

module.exports = {
  uploadMaterial,
  getAllMaterials,
  getMaterial,
  deleteMaterial,
  downloadMaterial,
  getMaterialContent
}
