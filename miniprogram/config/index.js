/**
 * 小程序配置文件
 * Mini Program Configuration
 * 
 * ⚠️ 重要：请修改下面的 YOUR_IP_HERE
 * 
 * 获取 IP 地址：
 * 1. 打开命令提示符 (cmd)
 * 2. 输入: ipconfig
 * 3. 找到 "IPv4 地址"，例如：192.168.1.100
 * 4. 把下面的 YOUR_IP_HERE 替换成你的 IP 地址
 */

const config = {
  // 开发环境配置
  development: {
    // 你的 IP 地址：10.131.25.42
    apiBaseURL: 'http://10.131.25.42:2001/api',
    
    // 如果后端运行在不同端口，修改这里
    apiPort: 2001,
    
    // 超时时间（毫秒）
    timeout: 30000,
    
    // 是否启用调试模式
    debug: true
  },
  
  // 生产环境配置
  production: {
    // 生产环境使用 HTTPS 域名
    apiBaseURL: 'https://api.yourdomain.com/api',
    timeout: 30000,
    debug: false
  }
}

// 根据环境选择配置
const env = 'development' // 'development' 或 'production'
const currentConfig = config[env]

module.exports = currentConfig
