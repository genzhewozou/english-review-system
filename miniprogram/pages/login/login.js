// pages/login/login.js
Page({
  data: {
    form: {
      username: '',
      password: ''
    },
    loading: false,
    error: ''
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`form.${field}`]: e.detail.value
    });
  },

  async handleLogin(e) {
    this.setData({
      loading: true,
      error: ''
    });

    try {
      const { username, password } = this.data.form;

      const response = await new Promise((resolve, reject) => {
        wx.request({
          url: 'http://localhost:3000/api/auth/login',
          method: 'POST',
          header: {
            'Content-Type': 'application/json'
          },
          data: this.data.form,
          success: resolve,
          fail: reject
        });
      });

      if (response.statusCode === 200) {
        const data = response.data;
        
        wx.setStorageSync('user', data);
        wx.setStorageSync('authToken', data.token);
        wx.setStorageSync('isAuthenticated', 'true');

        wx.switchTab({
          url: '/pages/dashboard/dashboard'
        });
      } else {
        let errorMessage = 'Login failed';
        if (response.data) {
          if (typeof response.data === 'string') {
            errorMessage = response.data;
          } else if (response.data.message) {
            errorMessage = response.data.message;
          }
        }
        this.setData({
          error: errorMessage
        });
      }
    } catch (err) {
      console.error('Login error:', err);
      this.setData({
        error: 'An error occurred. Please try again.'
      });
    } finally {
      this.setData({
        loading: false
      });
    }
  }
});