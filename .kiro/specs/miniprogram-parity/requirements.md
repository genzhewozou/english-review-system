# Requirements Document

## Introduction

本需求文档旨在实现微信小程序（miniprogram）与PC端（frontend）功能的完全对等，确保用户在不同平台上获得一致的学习体验。当前小程序存在功能缺失、页面适配问题，特别是review页面的显示和交互体验不佳。

## Glossary

- **Miniprogram**: 微信小程序端，使用WXML/WXSS/JS开发
- **Frontend**: PC端Web应用，使用Vue.js 3开发
- **Review_Session**: 复习会话，包含一组待复习的卡片
- **Card**: 闪卡，包含正面（问题）和背面（答案）
- **Deck**: 卡片组，用于组织相关的卡片
- **Spaced_Repetition**: 间隔重复算法，用于优化记忆效果
- **Material**: 学习材料，包括文档、视频、文章
- **Vocabulary**: 词汇卡片系统
- **Todo**: 待办事项系统
- **Responsive_Design**: 响应式设计，适配不同屏幕尺寸

## Requirements

### Requirement 1: Review页面功能对等

**User Story:** 作为小程序用户，我希望Review页面具有与PC端相同的功能，以便我可以完整地使用复习系统。

#### Acceptance Criteria

1. WHEN用户访问Review页面，THE Miniprogram SHALL显示与Frontend相同的三种会话类型选择（All Vocabulary、From Deck、Custom Selection）
2. WHEN用户选择"All Vocabulary"模式，THE Miniprogram SHALL启动包含所有词汇的复习会话
3. WHEN用户选择"From Deck"模式，THE Miniprogram SHALL显示用户的所有Deck列表供选择
4. WHEN用户选择"Custom Selection"模式，THE Miniprogram SHALL允许用户选择学习材料和具体卡片
5. WHEN用户在Custom模式下选择材料，THE Miniprogram SHALL显示该材料的所有卡片供选择
6. WHEN用户在Custom模式下搜索卡片，THE Miniprogram SHALL支持精确搜索和相似词搜索
7. WHEN用户查看统计信息，THE Miniprogram SHALL显示待复习数量、总词汇数、Deck数量
8. WHEN用户点击开始按钮，THE Miniprogram SHALL根据选择的模式创建相应的复习会话

### Requirement 2: Review页面响应式设计

**User Story:** 作为小程序用户，我希望Review页面在小屏幕上显示友好，以便我可以舒适地使用。

#### Acceptance Criteria

1. WHEN页面在小程序中渲染，THE Miniprogram SHALL使用适合移动设备的布局和字体大小
2. WHEN用户滚动页面，THE Miniprogram SHALL确保所有内容可见且可访问
3. WHEN用户点击交互元素，THE Miniprogram SHALL提供足够大的点击区域（至少44rpx x 44rpx）
4. WHEN显示卡片列表，THE Miniprogram SHALL使用适合小屏幕的滚动容器
5. WHEN显示统计信息，THE Miniprogram SHALL使用垂直或网格布局而非水平布局
6. WHEN显示按钮，THE Miniprogram SHALL确保按钮宽度适合小屏幕且易于点击
7. WHEN显示表单元素，THE Miniprogram SHALL使用小程序原生组件以获得最佳体验

### Requirement 3: ReviewSession页面功能对等

**User Story:** 作为小程序用户，我希望ReviewSession页面具有与PC端相同的功能，以便我可以完整地进行复习。

#### Acceptance Criteria

1. WHEN用户进入复习会话，THE Miniprogram SHALL显示当前问题和进度信息
2. WHEN用户查看卡片，THE Miniprogram SHALL支持翻转查看答案
3. WHEN用户回答问题，THE Miniprogram SHALL提供四个质量选项（Again、Hard、Good、Easy）
4. WHEN用户提交答案，THE Miniprogram SHALL记录答案并自动加载下一个问题
5. WHEN用户暂停会话，THE Miniprogram SHALL显示暂停状态并允许恢复
6. WHEN用户导航问题，THE Miniprogram SHALL支持前进和后退按钮
7. WHEN会话完成，THE Miniprogram SHALL显示统计信息（正确率、用时、强弱项分析）
8. WHEN会话完成，THE Miniprogram SHALL提供添加到Todo列表的功能
9. WHEN用户查看卡片评论，THE Miniprogram SHALL显示用户添加的评论内容
10. WHEN用户需要语音朗读，THE Miniprogram SHALL支持文本转语音功能

### Requirement 4: ReviewSession页面响应式设计

**User Story:** 作为小程序用户，我希望ReviewSession页面在小屏幕上显示友好，以便我可以专注于复习。

#### Acceptance Criteria

1. WHEN显示卡片内容，THE Miniprogram SHALL使用适合阅读的字体大小和行高
2. WHEN显示答案选项，THE Miniprogram SHALL使用垂直布局或2x2网格布局
3. WHEN显示进度条，THE Miniprogram SHALL确保进度信息清晰可见
4. WHEN显示统计图表，THE Miniprogram SHALL使用适合小屏幕的图表尺寸
5. WHEN显示完成页面，THE Miniprogram SHALL使用垂直布局展示所有统计信息
6. WHEN用户翻转卡片，THE Miniprogram SHALL提供流畅的动画效果
7. WHEN显示导航按钮，THE Miniprogram SHALL确保按钮不遮挡内容

### Requirement 5: Materials页面功能对等

**User Story:** 作为小程序用户，我希望Materials页面具有与PC端相同的功能，以便我可以管理学习材料。

#### Acceptance Criteria

1. WHEN用户访问Materials页面，THE Miniprogram SHALL显示所有学习材料列表
2. WHEN用户查看材料，THE Miniprogram SHALL显示材料类型、标题、上传时间
3. WHEN用户点击材料，THE Miniprogram SHALL导航到材料查看器页面
4. WHEN用户上传材料，THE Miniprogram SHALL支持选择文件并上传到服务器
5. WHEN用户删除材料，THE Miniprogram SHALL显示确认对话框并执行删除
6. WHEN用户搜索材料，THE Miniprogram SHALL根据标题过滤材料列表
7. WHEN用户按类型筛选，THE Miniprogram SHALL只显示选定类型的材料

### Requirement 6: MaterialViewer页面功能对等

**User Story:** 作为小程序用户，我希望MaterialViewer页面具有与PC端相同的功能，以便我可以查看和标注学习材料。

#### Acceptance Criteria

1. WHEN用户查看文档材料，THE Miniprogram SHALL显示文档的文本内容
2. WHEN用户查看视频材料，THE Miniprogram SHALL使用小程序视频组件播放视频
3. WHEN用户查看文章材料，THE Miniprogram SHALL渲染HTML内容
4. WHEN用户选择文本，THE Miniprogram SHALL支持文本高亮功能
5. WHEN用户高亮文本，THE Miniprogram SHALL允许添加评论和创建卡片
6. WHEN用户查看已有高亮，THE Miniprogram SHALL显示高亮标记和相关评论
7. WHEN用户从高亮创建卡片，THE Miniprogram SHALL自动填充卡片内容

### Requirement 7: Vocabulary页面功能对等

**User Story:** 作为小程序用户，我希望Vocabulary页面具有与PC端相同的功能，以便我可以管理词汇卡片。

#### Acceptance Criteria

1. WHEN用户访问Vocabulary页面，THE Miniprogram SHALL显示所有词汇卡片列表
2. WHEN用户查看卡片，THE Miniprogram SHALL显示正面、背面、来源材料
3. WHEN用户创建卡片，THE Miniprogram SHALL提供表单输入正面和背面内容
4. WHEN用户编辑卡片，THE Miniprogram SHALL加载现有内容并允许修改
5. WHEN用户删除卡片，THE Miniprogram SHALL显示确认对话框并执行删除
6. WHEN用户搜索卡片，THE Miniprogram SHALL根据内容过滤卡片列表
7. WHEN用户按材料筛选，THE Miniprogram SHALL只显示来自选定材料的卡片
8. WHEN用户按Deck筛选，THE Miniprogram SHALL只显示属于选定Deck的卡片

### Requirement 8: Decks页面功能对等

**User Story:** 作为小程序用户，我希望Decks页面具有与PC端相同的功能，以便我可以组织卡片。

#### Acceptance Criteria

1. WHEN用户访问Decks页面，THE Miniprogram SHALL显示所有Deck列表
2. WHEN用户查看Deck，THE Miniprogram SHALL显示Deck名称、描述、卡片数量
3. WHEN用户创建Deck，THE Miniprogram SHALL提供表单输入名称和描述
4. WHEN用户编辑Deck，THE Miniprogram SHALL加载现有内容并允许修改
5. WHEN用户删除Deck，THE Miniprogram SHALL显示确认对话框并执行删除
6. WHEN用户点击Deck，THE Miniprogram SHALL显示该Deck的所有卡片
7. WHEN用户向Deck添加卡片，THE Miniprogram SHALL更新Deck的卡片列表
8. WHEN用户从Deck移除卡片，THE Miniprogram SHALL更新Deck的卡片列表

### Requirement 9: Todo页面功能对等

**User Story:** 作为小程序用户，我希望Todo页面具有与PC端相同的功能，以便我可以管理学习任务。

#### Acceptance Criteria

1. WHEN用户访问Todo页面，THE Miniprogram SHALL显示所有待办事项列表
2. WHEN用户查看Todo，THE Miniprogram SHALL显示标题、描述、截止日期、状态
3. WHEN用户创建Todo，THE Miniprogram SHALL提供表单输入所有必要信息
4. WHEN用户编辑Todo，THE Miniprogram SHALL加载现有内容并允许修改
5. WHEN用户删除Todo，THE Miniprogram SHALL显示确认对话框并执行删除
6. WHEN用户标记Todo完成，THE Miniprogram SHALL更新Todo状态
7. WHEN用户按状态筛选，THE Miniprogram SHALL只显示选定状态的Todo
8. WHEN用户按类型筛选，THE Miniprogram SHALL只显示选定类型的Todo

### Requirement 10: Dashboard页面功能对等

**User Story:** 作为小程序用户，我希望Dashboard页面具有与PC端相同的功能，以便我可以查看学习概览。

#### Acceptance Criteria

1. WHEN用户访问Dashboard，THE Miniprogram SHALL显示学习统计概览
2. WHEN用户查看统计，THE Miniprogram SHALL显示今日复习数、总词汇数、学习天数
3. WHEN用户查看最近活动，THE Miniprogram SHALL显示最近的复习会话和学习材料
4. WHEN用户查看待办事项，THE Miniprogram SHALL显示即将到期的Todo
5. WHEN用户查看学习进度，THE Miniprogram SHALL显示本周/本月的学习趋势
6. WHEN用户点击快捷操作，THE Miniprogram SHALL导航到相应的功能页面

### Requirement 11: 统一的UI组件库

**User Story:** 作为开发者，我希望小程序使用统一的UI组件，以便保持与PC端一致的视觉风格。

#### Acceptance Criteria

1. WHEN渲染按钮，THE Miniprogram SHALL使用与Frontend一致的颜色方案和样式
2. WHEN渲染卡片容器，THE Miniprogram SHALL使用与Frontend一致的圆角、阴影、间距
3. WHEN渲染表单元素，THE Miniprogram SHALL使用与Frontend一致的输入框样式
4. WHEN渲染列表项，THE Miniprogram SHALL使用与Frontend一致的布局和间距
5. WHEN显示加载状态，THE Miniprogram SHALL使用与Frontend一致的加载动画
6. WHEN显示错误信息，THE Miniprogram SHALL使用与Frontend一致的提示样式
7. WHEN使用颜色，THE Miniprogram SHALL遵循Frontend定义的颜色变量

### Requirement 12: API集成一致性

**User Story:** 作为开发者，我希望小程序使用与PC端相同的API接口，以便确保数据一致性。

#### Acceptance Criteria

1. WHEN调用API，THE Miniprogram SHALL使用与Frontend相同的端点URL
2. WHEN发送请求，THE Miniprogram SHALL使用与Frontend相同的请求格式
3. WHEN接收响应，THE Miniprogram SHALL使用与Frontend相同的响应处理逻辑
4. WHEN处理错误，THE Miniprogram SHALL使用与Frontend相同的错误处理机制
5. WHEN进行身份验证，THE Miniprogram SHALL使用与Frontend相同的JWT令牌机制
6. WHEN刷新令牌，THE Miniprogram SHALL使用与Frontend相同的刷新逻辑
7. WHEN缓存数据，THE Miniprogram SHALL使用与Frontend相似的缓存策略

### Requirement 13: 性能优化

**User Story:** 作为小程序用户，我希望应用响应迅速，以便获得流畅的使用体验。

#### Acceptance Criteria

1. WHEN加载页面，THE Miniprogram SHALL在2秒内显示主要内容
2. WHEN切换页面，THE Miniprogram SHALL使用页面栈管理避免重复加载
3. WHEN加载列表，THE Miniprogram SHALL使用虚拟滚动或分页加载大量数据
4. WHEN渲染图片，THE Miniprogram SHALL使用懒加载和图片压缩
5. WHEN缓存数据，THE Miniprogram SHALL使用本地存储减少网络请求
6. WHEN更新数据，THE Miniprogram SHALL使用增量更新而非全量刷新
7. WHEN执行动画，THE Miniprogram SHALL使用CSS动画而非JS动画以提高性能

### Requirement 14: 离线支持

**User Story:** 作为小程序用户，我希望在网络不佳时仍能查看已加载的内容，以便不中断学习。

#### Acceptance Criteria

1. WHEN网络断开，THE Miniprogram SHALL显示已缓存的学习材料
2. WHEN网络断开，THE Miniprogram SHALL显示已缓存的词汇卡片
3. WHEN网络断开，THE Miniprogram SHALL允许用户继续进行离线复习
4. WHEN网络恢复，THE Miniprogram SHALL自动同步离线期间的数据
5. WHEN同步失败，THE Miniprogram SHALL保留本地数据并提示用户
6. WHEN存储空间不足，THE Miniprogram SHALL清理最旧的缓存数据
7. WHEN用户手动刷新，THE Miniprogram SHALL强制从服务器获取最新数据

### Requirement 15: 用户体验增强

**User Story:** 作为小程序用户，我希望获得原生小程序的体验优势，以便更好地使用应用。

#### Acceptance Criteria

1. WHEN用户下拉页面，THE Miniprogram SHALL支持下拉刷新功能
2. WHEN用户上拉页面，THE Miniprogram SHALL支持上拉加载更多
3. WHEN用户分享内容，THE Miniprogram SHALL支持分享到微信好友和朋友圈
4. WHEN用户收到通知，THE Miniprogram SHALL使用小程序订阅消息
5. WHEN用户扫描二维码，THE Miniprogram SHALL支持扫码快速添加材料
6. WHEN用户使用语音，THE Miniprogram SHALL支持语音输入和语音朗读
7. WHEN用户切换深色模式，THE Miniprogram SHALL自动适配深色主题
