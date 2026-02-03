# Indexed Comics 项目文档

## 📚 文档索引

### 核心架构文档

#### 🏗️ [本地存储管理架构详解](./local_storage_architecture.md)
**完整的架构设计文档，包含详细的流程图和说明**

内容涵盖：
- 整体架构图（分层架构、模块依赖）
- 核心组件详解（LibraryRoot、SyncOrchestrator、LocalScanner 等）
- 完整的数据流和流程图（同步流程、扫描流程、索引更新等）
- 类图和关系（领域模型、服务层、后台任务）
- 平台实现（Android、iOS）
- 使用示例和代码片段

**适合**：需要深入理解整体架构的开发者

---

#### ⚡ [快速参考指南](./local_storage_quick_reference.md)
**简化的速查文档，包含关键信息和速查表**

内容涵盖：
- 核心流程速览（5 步骤同步流程）
- 三种来源对比表
- 关键组件速查表
- 任务状态机
- 后台调度对比
- 典型使用场景
- 性能优化建议
- 调试命令

**适合**：快速查阅和日常开发参考

---

### 专项文档

#### 📖 [本地漫画设计规范](./local_comics_design.md)
定义本地漫画的文件结构和格式规范

- 文件与图片格式（jpg/png/webp）
- CBZ 结构约定
- 目录组织规范（漫画/章节/页面）
- 识别与解析策略
- 元数据策略
- 平台访问约束（Android SAF、iOS Files）

---

#### 💾 [索引缓存与同步](./local_cache_sync.md)
数据库索引结构和同步策略

- 索引表结构（Comics、Chapters、Pages）
- 约束与去重规则
- 扫描与缓存更新策略
- 增量更新逻辑
- 平台同步策略（Android 实时监听、iOS 轮询）

---

#### 🔄 [扫描-索引-刮削流程](../Cooomics/data/local/SYNC_FLOW.md)
任务编排流程的详细说明

- 核心组件职责
- 三个阶段详解（扫描、索引、刮削）
- 完整流程图
- 错误处理策略
- 性能优化建议
- 触发方式（手动、自动、后台）
- 数据一致性保证

---

#### ⏰ [后台任务调度](../Cooomics/data/local/BACKGROUND_SCHEDULING.md)
Android 和 iOS 的后台任务调度实现

- 跨平台抽象设计
- Android WorkManager 实现
- iOS BGTaskScheduler 实现
- 约束条件配置
- 调度策略建议
- 配置要求
- 最佳实践

---

#### 📋 [模块规划](./module_plan.md)
项目整体模块结构

- 工程结构（KMP + Compose）
- 分层职责
- 依赖方向
- 模块列表

---

## 📊 架构可视化

### 整体分层架构

```
┌─────────────────────────────────────────┐
│        Feature Layer (功能层)            │
│  - local-library (本地库 UI)             │
│  - discover, search, settings...        │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│        Domain Layer (领域层)             │
│  - domain:comic (漫画领域模型)           │
│  - domain:anime (动漫领域模型)           │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Data Layer (数据层)              │
│  - data:local (本地数据源)               │
│  - data:jikan (在线数据源)               │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Core Layer (核心层)              │
│  - core:network (网络客户端)             │
│  - core:utils (工具类)                   │
└─────────────────────────────────────────┘
```

### 本地存储核心流程

```
用户操作
    ↓
[添加根目录] → [扫描文件系统] → [更新索引] → [入队刮削] → [完成]
    ↓              ↓               ↓            ↓
LibraryRoot   ScanResult    UpdateResult   ScrapeTask
```

### 三种来源

```
┌────────────────────┬──────────────────────┬─────────────────────┐
│  DOWNLOADED        │  IMPORTED_INTERNAL   │  IMPORTED_EXTERNAL  │
│  应用内下载         │  应用内导入           │  应用外导入          │
├────────────────────┼──────────────────────┼─────────────────────┤
│  应用私有目录       │  应用私有目录         │  外部存储            │
│  完全控制           │  完全控制             │  需要授权            │
│  无平台差异         │  无平台差异           │  Android: SAF        │
│                    │                      │  iOS: Files          │
└────────────────────┴──────────────────────┴─────────────────────┘
```

## 🚀 快速导航

### 我想了解...

- **整体架构** → [本地存储管理架构详解](./local_storage_architecture.md)
- **快速上手** → [快速参考指南](./local_storage_quick_reference.md)
- **文件格式** → [本地漫画设计规范](./local_comics_design.md)
- **数据库设计** → [索引缓存与同步](./local_cache_sync.md)
- **任务流程** → [扫描-索引-刮削流程](../Cooomics/data/local/SYNC_FLOW.md)
- **后台任务** → [后台任务调度](../Cooomics/data/local/BACKGROUND_SCHEDULING.md)

### 我想实现...

- **添加新根目录** → [架构详解 - 使用示例](./local_storage_architecture.md#使用示例)
- **扫描文件系统** → [同步流程文档](../Cooomics/data/local/SYNC_FLOW.md)
- **配置后台任务** → [后台调度文档](../Cooomics/data/local/BACKGROUND_SCHEDULING.md)
- **Android 平台代码** → [架构详解 - Android 实现](./local_storage_architecture.md#android-平台特性)
- **iOS 平台代码** → [架构详解 - iOS 实现](./local_storage_architecture.md#ios-平台特性)

### 我遇到了问题...

- **权限问题** → [本地漫画设计 - 平台访问约束](./local_comics_design.md#平台访问约束)
- **扫描不到文件** → [本地漫画设计 - 识别与解析策略](./local_comics_design.md#识别与解析策略)
- **数据库错误** → [索引缓存 - 约束与去重](./local_cache_sync.md#约束与去重)
- **后台任务不执行** → [后台调度 - 平台限制](../Cooomics/data/local/BACKGROUND_SCHEDULING.md)
- **性能问题** → [同步流程 - 性能优化](../Cooomics/data/local/SYNC_FLOW.md#性能优化)

## 🔍 关键概念速查

### 核心实体

| 实体 | 说明 | 文件 |
|------|------|------|
| **LibraryRoot** | 库根目录，代表一个本地库的入口 | `domain/comic/model/LibraryRoot.kt` |
| **Comic** | 漫画，对应一级文件夹 | `domain/comic/model/Comic.kt` |
| **Chapter** | 章节，对应子文件夹或 CBZ | `domain/comic/model/Chapter.kt` |
| **Page** | 页面，对应图片文件 | `domain/comic/model/Page.kt` |

### 核心服务

| 服务 | 说明 | 文件 |
|------|------|------|
| **SyncOrchestrator** | 同步编排器，协调整个流程 | `data/local/orchestrator/SyncOrchestrator.kt` |
| **LocalScanner** | 本地扫描器，识别文件结构 | `data/local/scanner/LocalScanner.kt` |
| **IndexUpdateManager** | 索引更新管理器，更新数据库 | `data/local/orchestrator/IndexUpdateManager.kt` |
| **LocalScrapeQueue** | 刮削队列，管理元数据获取 | `data/local/scrape/LocalScrapeQueue.kt` |
| **BackgroundTaskScheduler** | 后台任务调度器 | `data/local/background/BackgroundTaskScheduler.kt` |

### 任务类型

| 任务 | 说明 | 文件 |
|------|------|------|
| **ScanTask** | 扫描任务，支持全量/增量 | `domain/comic/model/ScanTask.kt` |
| **ScrapeTask** | 刮削任务，支持重试、优先级 | `domain/comic/model/ScrapeTask.kt` |

## 📁 项目结构

```
Indexed/
├── docs/                                    # 📚 文档目录
│   ├── README.md                            # 本文件
│   ├── local_storage_architecture.md        # 完整架构文档
│   ├── local_storage_quick_reference.md     # 快速参考
│   ├── local_comics_design.md              # 文件格式规范
│   ├── local_cache_sync.md                 # 索引设计
│   └── module_plan.md                      # 模块规划
│
└── Cooomics/                               # 🎯 项目代码
    ├── domain/
    │   └── comic/                          # 漫画领域层
    │       └── src/commonMain/kotlin/com/pusu/indexed/domain/comic/
    │           ├── model/                   # 领域模型
    │           │   ├── LibraryRoot.kt
    │           │   ├── LibraryRootSource.kt
    │           │   ├── LibraryRootPermission.kt
    │           │   ├── Comic.kt
    │           │   ├── Chapter.kt
    │           │   ├── Page.kt
    │           │   ├── ScanTask.kt
    │           │   └── ScrapeTask.kt
    │           ├── repository/              # 仓库接口
    │           │   └── LibraryRootRepository.kt
    │           └── usecase/                # 用例
    │               ├── AddLibraryRootUseCase.kt
    │               ├── ScanLibraryUseCase.kt
    │               └── EnqueueScrapeUseCase.kt
    │
    └── data/
        └── local/                          # 本地数据源
            ├── SYNC_FLOW.md                # 同步流程文档
            ├── BACKGROUND_SCHEDULING.md    # 后台调度文档
            └── src/
                ├── commonMain/kotlin/com/pusu/indexed/data/local/
                │   ├── scanner/
                │   │   └── LocalScanner.kt
                │   ├── scrape/
                │   │   └── LocalScrapeQueue.kt
                │   ├── orchestrator/
                │   │   ├── SyncOrchestrator.kt
                │   │   └── IndexUpdateManager.kt
                │   ├── store/
                │   │   └── LocalLibraryRootStore.kt
                │   └── background/
                │       ├── BackgroundTaskScheduler.kt (expect)
                │       └── BackgroundSyncWorker.kt
                ├── androidMain/            # Android 实现
                │   └── kotlin/com/pusu/indexed/data/local/background/
                │       └── BackgroundTaskScheduler.android.kt
                └── iosMain/               # iOS 实现
                    └── kotlin/com/pusu/indexed/data/local/background/
                        └── BackgroundTaskScheduler.ios.kt
```

## 🎯 实现进度

### ✅ 已完成

- [x] 领域模型定义（LibraryRoot、Comic、Chapter、Page）
- [x] 任务模型（ScanTask、ScrapeTask）
- [x] 仓库接口定义
- [x] 核心服务接口（Scanner、Queue、Orchestrator、IndexManager）
- [x] 后台调度抽象（expect/actual）
- [x] 用例层定义
- [x] 完整文档和流程图

### 🚧 待实现

- [ ] SQLDelight 数据库表定义
- [ ] 平台特定扫描器实现（Android SAF、iOS Files）
- [ ] 索引存储实现（LocalLibraryRootStore）
- [ ] 同步编排器实现（SyncOrchestrator）
- [ ] 刮削队列实现（LocalScrapeQueue）
- [ ] Android WorkManager Worker
- [ ] iOS BGTaskScheduler 注册
- [ ] UI 功能模块（feature:local-library）
- [ ] 单元测试
- [ ] 集成测试

## 🤝 贡献指南

### 添加新功能

1. 阅读相关架构文档
2. 在对应的模块（domain/data/feature）中实现
3. 遵循现有的接口和模式
4. 添加单元测试
5. 更新文档

### 修改现有功能

1. 确认影响范围（是否影响其他模块）
2. 更新接口定义（如果需要）
3. 更新实现
4. 更新测试
5. 更新文档

## 📝 文档约定

- **架构文档**：使用 Mermaid 绘制流程图和类图
- **代码示例**：使用 Kotlin 代码块
- **注释**：中文注释，关键概念提供英文对照
- **版本**：文档随代码同步更新

## 📞 联系方式

如有问题或建议，请：

1. 查阅相关文档
2. 检查代码注释
3. 提交 Issue 或 PR

---

**最后更新**: 2026-01-27
**版本**: 1.0.0
