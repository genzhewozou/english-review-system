// pages/register/register.js
Page({
  data: {
    form: {
      username: '',
      email: '',
      password: ''
    },
    loading: false,
    error: '',
    success: ''
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`form.${field}`]: e.detail.value
    });
  },

  async handleRegister(e) {
    this.setData({
      loading: true,
      error: '',
      success: ''
    });

    try {
      const response = await new Promise((resolve, reject) => {
        wx.request({
          url: 'http://localhost:3000/api/auth/register',
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
        this.setData({
          success: 'Registration successful! Redirecting to login...'
        });
        
        setTimeout(() => {
          wx.redirectTo({
            url: '/pages/login/login'
          });
        }, 2000);
      } else {
        let errorMessage = 'Registration failed';
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
      console.error('Register error:', err);
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