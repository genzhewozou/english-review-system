/**
 * API Service for WeChat Mini Program
 * Provides unified API access matching frontend implementation
 */

const config = require('../config/index')

// API Configuration
const API_CONFIG = {
  baseURL: config.apiBaseURL,
  timeout: config.timeout
}

/**
 * Make authenticated API request
 * @param {string} url - API endpoint
 * @param {string} method - HTTP method
 * @param {Object} data - Request data
 * @param {Object} options - Additional options
 * @returns {Promise} API response
 */
function request(url, method = 'GET', data = {}, options = {}) {
  const token = wx.getStorageSync('authToken')
  
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${API_CONFIG.baseURL}${url}`,
      method: method,
      data: method === 'GET' ? undefined : data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.headers
      },
      timeout: options.timeout || API_CONFIG.timeout,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res)
        } else if (res.statusCode === 401) {
          // Unauthorized - clear token
          wx.removeStorageSync('authToken')
          wx.showToast({ title: 'Please log in', icon: 'none' })
          reject(new Error('Unauthorized'))
        } else if (res.statusCode === 404) {
          // Not found
          reject(new Error('Resource not found'))
        } else {
          reject(new Error(res.data?.message || 'Request failed'))
        }
      },
      fail: (err) => {
        console.error('API Request failed:', err)
        wx.showToast({ title: 'Network error', icon: 'none' })
        reject(err)
      }
    })
  })
}

/**
 * Upload file to server
 * @param {string} url - Upload endpoint
 * @param {string} filePath - Local file path
 * @param {Object} formData - Additional form data
 * @param {Function} onProgress - Progress callback
 * @returns {Promise} Upload response
 */
function upload(url, filePath, formData = {}, onProgress = null) {
  const token = wx.getStorageSync('authToken')
  
  return new Promise((resolve, reject) => {
    const uploadTask = wx.uploadFile({
      url: `${API_CONFIG.baseURL}${url}`,
      filePath: filePath,
      name: 'file',
      formData: formData,
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          try {
            const data = JSON.parse(res.data)
            resolve({ data, statusCode: res.statusCode })
          } catch (e) {
            resolve({ data: res.data, statusCode: res.statusCode })
          }
        } else {
          reject(new Error('Upload failed'))
        }
      },
      fail: (err) => {
        console.error('Upload failed:', err)
        reject(err)
      }
    })
    
    if (onProgress) {
      uploadTask.onProgressUpdate((res) => {
        onProgress(res.progress)
      })
    }
  })
}

/**
 * Download file from server
 * @param {string} url - Download endpoint
 * @returns {Promise} Downloaded file path
 */
function download(url) {
  const token = wx.getStorageSync('authToken')
  
  return new Promise((resolve, reject) => {
    wx.downloadFile({
      url: `${API_CONFIG.baseURL}${url}`,
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.tempFilePath)
        } else {
          reject(new Error('Download failed'))
        }
      },
      fail: (err) => {
        console.error('Download failed:', err)
        reject(err)
      }
    })
  })
}

// Export API methods
module.exports = {
  // HTTP Methods
  get: (url, params = {}, options = {}) => {
    const queryString = Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
    const fullUrl = queryString ? `${url}?${queryString}` : url
    return request(fullUrl, 'GET', {}, options)
  },
  
  post: (url, data = {}, options = {}) => {
    return request(url, 'POST', data, options)
  },
  
  put: (url, data = {}, options = {}) => {
    return request(url, 'PUT', data, options)
  },
  
  patch: (url, data = {}, options = {}) => {
    return request(url, 'PATCH', data, options)
  },
  
  delete: (url, options = {}) => {
    return request(url, 'DELETE', {}, options)
  },
  
  // File operations
  upload: upload,
  download: download,
  
  // Configuration
  setBaseURL: (baseURL) => {
    API_CONFIG.baseURL = baseURL
  },
  
  getBaseURL: () => {
    return API_CONFIG.baseURL
  }
}
