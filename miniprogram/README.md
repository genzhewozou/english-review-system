# 小程序配置说明 / Mini Program Setup Guide

## 🚨 重要：网络配置

小程序**不支持 localhost**！必须使用实际 IP 地址或域名。

### 步骤 1：获取你的电脑 IP 地址

#### Windows 系统：
```bash
# 打开命令提示符 (cmd)，输入：
ipconfig

# 查找 "IPv4 地址"，例如：192.168.1.100
```

#### Mac/Linux 系统：
```bash
# 打开终端，输入：
ifconfig
# 或
ip addr

# 查找 "inet" 地址，例如：192.168.1.100
```

### 步骤 2：修改配置文件

打开 `miniprogram/config/index.js`，修改 `apiBaseURL`：

```javascript
development: {
  // 将 IP 地址改为你的实际 IP
  apiBaseURL: 'http://192.168.1.100:2001/api',  // ← 修改这里
  // ...
}
```

### 步骤 3：配置微信开发者工具

1. 打开微信开发者工具
2. 点击右上角 "详情"
3. 勾选 "不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书"
4. 勾选 "启用调试"

### 步骤 4：确保后端服务运行

```bash
# 在项目根目录运行后端
./mvnw spring-boot:run

# 或使用 Maven
mvn spring-boot:run

# 确认后端运行在 http://0.0.0.0:2001 或 http://localhost:2001
```

### 步骤 5：测试连接

方法 1 - 使用诊断页面（推荐）：
1. 在微信开发者工具中，点击 "编译" 按钮旁边的下拉菜单
2. 选择 "添加编译模式"
3. 输入模式名称: `诊断页面`，启动页面: `pages/debug/debug`
4. 点击 "确定"，然后编译
5. 点击 "测试连接" 按钮查看结果

方法 2 - 在任意页面测试：
打开任意页面，查看控制台是否有网络错误。

### 步骤 6：遇到问题？

如果仍然显示 "Failed to fetch"，请查看详细的排查指南：
📖 [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)

该指南包含：
- 详细的错误诊断步骤
- 常见错误及解决方案
- 防火墙配置说明
- 快速检查清单

## 📱 常见问题

### Q1: 仍然显示 "Failed to fetch"
**解决方案：**
- 确认后端服务正在运行
- 确认 IP 地址正确（电脑和手机/模拟器在同一网络）
- 确认防火墙没有阻止 2001 端口
- 在微信开发者工具中勾选"不校验合法域名"

### Q2: 真机调试无法连接
**解决方案：**
- 确保手机和电脑在同一 WiFi 网络
- 使用电脑的局域网 IP（192.168.x.x），不要使用 127.0.0.1
- 检查路由器是否开启了 AP 隔离

### Q3: 生产环境部署
**解决方案：**
- 修改 `config/index.js` 中的 `env` 为 `'production'`
- 配置 `production.apiBaseURL` 为你的 HTTPS 域名
- 在微信公众平台配置服务器域名白名单

## 🔧 项目结构

```
miniprogram/
├── config/
│   └── index.js          # 配置文件（修改 API 地址）
├── utils/
│   └── api.js            # API 服务
├── services/             # 业务服务层
│   ├── materialService.js
│   ├── vocabularyService.js
│   ├── reviewService.js
│   ├── todoService.js
│   └── deckService.js
├── pages/                # 页面
│   ├── dashboard/
│   ├── materials/
│   ├── review/
│   └── ...
└── README.md            # 本文件
```

## 📝 开发注意事项

1. **所有 API 调用都通过 service 层**，不要直接使用 `wx.request`
2. **使用 async/await** 处理异步操作
3. **错误处理**：所有 service 方法都会抛出错误，需要 try-catch
4. **认证**：token 存储在 `wx.storage` 中，key 为 `authToken`

## 🚀 快速开始

```bash
# 1. 获取 IP 地址
ipconfig  # Windows
ifconfig  # Mac/Linux

# 2. 修改 miniprogram/config/index.js
# 将 apiBaseURL 改为 http://你的IP:2001/api

# 3. 启动后端
./mvnw spring-boot:run

# 4. 打开微信开发者工具
# 导入项目，选择 miniprogram 目录

# 5. 配置开发者工具
# 详情 → 不校验合法域名 ✓
```

## 📞 需要帮助？

如果遇到问题：
1. 检查后端是否运行：浏览器访问 `http://localhost:2001/api/health`
2. 检查 IP 配置是否正确
3. 查看微信开发者工具控制台的错误信息
4. 确认防火墙设置

---

**重要提醒：** 每次更改配置后，需要在微信开发者工具中点击"编译"重新加载小程序。
