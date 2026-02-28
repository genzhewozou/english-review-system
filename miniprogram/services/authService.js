const api = require('../utils/api');

/**
 * 认证服务 - 处理用户登录、注册和认证相关操作
 */
const authService = {
  /**
   * 用户登录
   * @param {Object} credentials - 登录凭证
   * @param {string} credentials.username - 用户名
   * @param {string} credentials.password - 密码
   * @returns {Promise<Object>} 登录响应数据
   */
  async login(credentials) {
    try {
      const response = await api.post('/auth/login', credentials);
      
      // 保存token到本地存储
      if (response.token) {
        wx.setStorageSync('authToken', response.token);
      }
      
      // 保存用户信息
      if (response.user) {
        wx.setStorageSync('userInfo', response.user);
      }
      
      return response;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  },

  /**
   * 用户注册
   * @param {Object} userData - 注册数据
   * @param {string} userData.username - 用户名
   * @param {string} userData.password - 密码
   * @param {string} userData.email - 邮箱
   * @returns {Promise<Object>} 注册响应数据
   */
  async register(userData) {
    try {
      const response = await api.post('/auth/register', userData);
      
      // 注册成功后自动登录
      if (response.token) {
        wx.setStorageSync('authToken', response.token);
      }
      
      if (response.user) {
        wx.setStorageSync('userInfo', response.user);
      }
      
      return response;
    } catch (error) {
      console.error('Register error:', error);
      throw error;
    }
  },

  /**
   * 用户登出
   * @returns {Promise<void>}
   */
  async logout() {
    try {
      // 清除本地存储
      wx.removeStorageSync('authToken');
      wx.removeStorageSync('userInfo');
      
      // 可选：调用后端登出接口
      // await api.post('/auth/logout');
      
      return true;
    } catch (error) {
      console.error('Logout error:', error);
      throw error;
    }
  },

  /**
   * 获取当前用户信息
   * @returns {Object|null} 用户信息
   */
  getCurrentUser() {
    try {
      return wx.getStorageSync('userInfo');
    } catch (error) {
      console.error('Get current user error:', error);
      return null;
    }
  },

  /**
   * 检查是否已登录
   * @returns {boolean} 是否已登录
   */
  isAuthenticated() {
    const token = wx.getStorageSync('authToken');
    return !!token;
  },

  /**
   * 获取认证token
   * @returns {string|null} 认证token
   */
  getToken() {
    try {
      return wx.getStorageSync('authToken');
    } catch (error) {
      console.error('Get token error:', error);
      return null;
    }
  },

  /**
   * 更新用户信息
   * @param {Object} userData - 用户数据
   * @returns {Promise<Object>} 更新后的用户信息
   */
  async updateProfile(userData) {
    try {
      const response = await api.put('/auth/profile', userData);
      
      // 更新本地存储的用户信息
      if (response.user) {
        wx.setStorageSync('userInfo', response.user);
      }
      
      return response;
    } catch (error) {
      console.error('Update profile error:', error);
      throw error;
    }
  },

  /**
   * 修改密码
   * @param {Object} passwordData - 密码数据
   * @param {string} passwordData.oldPassword - 旧密码
   * @param {string} passwordData.newPassword - 新密码
   * @returns {Promise<Object>} 响应数据
   */
  async changePassword(passwordData) {
    try {
      const response = await api.put('/auth/password', passwordData);
      return response;
    } catch (error) {
      console.error('Change password error:', error);
      throw error;
    }
  }
};

module.exports = authService;
