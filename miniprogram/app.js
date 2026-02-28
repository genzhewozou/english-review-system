// app.js
App({
  onLaunch() {
    // 初始化登录状态
    this.checkAuthStatus();
    
    // 加载主题偏好
    this.loadThemePreference();
  },
  
  globalData: {
    userInfo: null,
    isAuthenticated: false,
    isDarkMode: false
  },
  
  checkAuthStatus() {
    const userInfo = wx.getStorageSync('user');
    const authToken = wx.getStorageSync('authToken');
    this.globalData.isAuthenticated = !!authToken;
    this.globalData.userInfo = userInfo;
  },
  
  handleLogout() {
    wx.removeStorageSync('isAuthenticated');
    wx.removeStorageSync('user');
    wx.removeStorageSync('authToken');
    this.globalData.isAuthenticated = false;
    this.globalData.userInfo = null;
    
    // 跳转到登录页面
    wx.redirectTo({
      url: '/pages/login/login'
    });
  },
  
  loadThemePreference() {
    const savedTheme = wx.getStorageSync('darkMode');
    this.globalData.isDarkMode = savedTheme === 'true';
  },
  
  toggleTheme() {
    this.globalData.isDarkMode = !this.globalData.isDarkMode;
    wx.setStorageSync('darkMode', this.globalData.isDarkMode);
  }
});