# Discover 功能完整示例

这是一个从 UI 到 API 的完整依赖链示例，展示了如何使用 Clean Architecture + MVI 架构实现一个跨平台功能。

---

## 📊 完整依赖链

```
┌─────────────────────────────────────────┐
│  App Layer (Android/Desktop)            │
│  ┌─────────────┐                        │
│  │ HttpClient  │ (OkHttp/CIO)           │
│  └──────┬──────┘                        │
│         ↓                               │
│  ┌──────────────────┐                   │
│  │DependencyContainer│                  │
│  └──────┬───────────┘                   │
└─────────┼───────────────────────────────┘
          ↓ createDiscoverViewModel(scope)
┌─────────────────────────────────────────┐
│  Feature Layer                          │
│  DiscoverScreen → DiscoverViewModel     │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│  Domain Layer                           │
│  GetTrendingAnimeUseCase                │
│       ↓                                 │
│  DiscoverRepository (接口)              │
└──────────────┬──────────────────────────┘
               ↑ 实现
┌─────────────────────────────────────────┐
│  Data Layer                             │
│  JikanDiscoverRepository                │
│       ↓                                 │
│  JikanApi → HTTP Request                │
└─────────────────────────────────────────┘
```

---

## 📁 文件结构

```
Indexed-Comics/
├── apps/
│   ├── android/src/main/kotlin/com/pusu/indexed/androidapp/di/
│   │   └── DependencyContainer.kt            # Android DI 容器 ✅
│   │
│   ├── desktop/src/desktopMain/kotlin/com/pusu/indexed/desktopapp/di/
│   │   └── DependencyContainer.kt            # Desktop DI 容器 ✅
│   │
├── shared/
│   ├── domain/discover/
│   │   └── src/commonMain/kotlin/
│   │       ├── model/
│   │       │   └── AnimeItem.kt              # 领域模型 ✅
│   │       ├── repository/
│   │       │   └── DiscoverRepository.kt     # 仓库接口 ✅
│   │       └── usecase/
│   │           └── GetTrendingAnimeUseCase.kt # 用例 ✅
│   │
│   ├── data/jikan/
│   │   └── src/commonMain/kotlin/
│   │       ├── repository/
│   │       │   └── JikanDiscoverRepository.kt # 仓库实现 ✅
│   │       └── mapper/
│   │           └── JikanToDiscoverMapper.kt   # 数据转换 ✅
│   │
│   └── feature/discover/
│       └── src/commonMain/kotlin/
│           ├── DiscoverScreen.kt              # UI 组件 ✅
│           └── presentation/
│               ├── DiscoverViewModel.kt       # ViewModel ✅
│               ├── DiscoverUiState.kt         # UI 状态 ✅
│               ├── DiscoverIntent.kt          # 用户意图 ✅
│               └── DiscoverUiEvent.kt         # UI 事件 ✅
│
└── apps/
    ├── android/src/demo/java/com/pusu/indexed/comics/
    │   ├── App.kt                             # Application 类 ✅
    │   └── DiscoverActivity.kt                # 示例 Activity ✅
    │
    └── desktop/src/desktopMain/kotlin/com/pusu/indexed/desktopapp/
        └── DiscoverMain.kt                    # Desktop 入口 ✅
```

---

## 🔧 核心代码

### 1. DependencyContainer（依赖注入容器）

```kotlin
// apps/android/src/main/kotlin/com/pusu/indexed/androidapp/di/DependencyContainer.kt
class DependencyContainer(httpClient: HttpClient) {
    
    private val httpClient: HttpClient = httpClient
    
    private val jikanClient: JikanClient by lazy {
        JikanClient(
            baseUrl = "https://api.jikan.moe/v4",
            httpClient = httpClient
        )
    }
    
    private val jikanApi: JikanApi by lazy {
        createJikanApi(jikanClient)
    }
    
    private val jikanToDiscoverMapper: JikanToDiscoverMapper by lazy {
        JikanToDiscoverMapper()
    }
    
    private val discoverRepository: DiscoverRepository by lazy {
        JikanDiscoverRepository(
            jikanApi = jikanApi,
            mapper = jikanToDiscoverMapper
        )
    }
    
    private val getTrendingAnimeUseCase: GetTrendingAnimeUseCase by lazy {
        GetTrendingAnimeUseCase(repository = discoverRepository)
    }
    
    // 创建 ViewModel
    fun createDiscoverViewModel(
        coroutineScope: CoroutineScope
    ): DiscoverViewModel {
        return DiscoverViewModel(
            getTrendingAnimeUseCase = getTrendingAnimeUseCase,
            coroutineScope = coroutineScope
        )
    }
}
```

### 2. Android 集成

#### App.kt（Application 类）

```kotlin
// apps/android/src/demo/java/com/pusu/indexed/comics/App.kt
class App : Application() {
    
    lateinit var dependencyContainer: DependencyContainer
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // 创建 HttpClient (Android 使用 OkHttp)
        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.ANDROID
                level = LogLevel.INFO
            }
        }
        
        // 创建依赖容器
        dependencyContainer = DependencyContainer(httpClient)
        instance = this
    }
    
    companion object {
        lateinit var instance: App
            private set
    }
}
```

#### DiscoverActivity.kt

```kotlin
// apps/android/src/demo/java/com/pusu/indexed/comics/DiscoverActivity.kt
class DiscoverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CoooomicsTheme {
                // 1. 获取 CoroutineScope
                val scope = rememberCoroutineScope()
                
                // 2. 从 DI 容器创建 ViewModel
                val viewModel = remember {
                    App.instance.dependencyContainer
                        .createDiscoverViewModel(scope)
                }
                
                // 3. 使用 ViewModel
                DiscoverScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { animeId ->
                        println("Navigate to detail: $animeId")
                    }
                )
            }
        }
    }
}
```

#### AndroidManifest.xml

```xml
<application
    android:name=".App">  <!-- 指定 Application 类 -->
    
    <activity
        android:name=".DiscoverActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

### 3. Desktop 集成

```kotlin
// apps/desktop/src/desktopMain/kotlin/com/pusu/indexed/desktopapp/DiscoverMain.kt
fun main() = application {
    // 1. 创建 HttpClient (Desktop 使用 CIO)
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
    
    // 2. 创建依赖容器
    val dependencyContainer = DependencyContainer(httpClient)
    
    // 3. 创建 ViewModel
    val viewModel = dependencyContainer.createDiscoverViewModel(
        coroutineScope = CoroutineScope(Dispatchers.Default)
    )
    
    // 4. 显示窗口
    Window(
        onCloseRequest = ::exitApplication,
        title = "Indexed Comics - Discover"
    ) {
        DiscoverScreen(
            viewModel = viewModel,
            onNavigateToDetail = { animeId ->
                println("Navigate to detail: $animeId")
            }
        )
    }
}
```

---

## 📊 数据流示例

### 用户打开 Discover 页面

```
1. Activity/Main 创建
   ↓
2. 创建 HttpClient (平台特定)
   ↓
3. 创建 DependencyContainer(httpClient)
   ↓
4. 调用 container.createDiscoverViewModel(scope)
   ├─ 创建 JikanClient
   ├─ 创建 JikanApi
   ├─ 创建 Mapper
   ├─ 创建 JikanDiscoverRepository (实现 DiscoverRepository)
   ├─ 创建 GetTrendingAnimeUseCase
   └─ 创建 DiscoverViewModel
   ↓
5. DiscoverScreen 显示
   ↓
6. ViewModel.init() 自动调用 LoadContent
   ↓
7. getTrendingAnimeUseCase(page=1, limit=10)
   ↓
8. repository.getTrendingAnime(1, 10)
   ↓
9. jikanApi.top.getTopAnime(...)
   ↓
10. HTTP GET https://api.jikan.moe/v4/top/anime
    ↓
11. 返回数据 List<Anime>
    ↓
12. mapper.mapToAnimeItemList() → List<AnimeItem>
    ↓
13. ViewModel 更新 _uiState
    ↓
14. UI 重组显示动漫列表
```

---

## 🎯 关键架构原则

### 1. 依赖方向

```
Feature → Domain ← Data
   ↑         ↑        ↑
  (UI)   (业务逻辑) (数据源)
```

- ✅ Feature 依赖 Domain
- ✅ Data 依赖 Domain（实现接口）
- ❌ Domain 不依赖 Data 和 Feature

### 2. DI 配置位置

```
❌ 错误：在 Feature 层配置
   shared/feature/discover/di/  ← 不要在这里！
   shared/core/utils/di/        ← 也不要在这里（utils 是纯工具）

✅ 正确：在 App 层配置
   apps/android/di/DependencyContainer.kt     ← Android DI 容器
   apps/desktop/di/DependencyContainer.kt     ← Desktop DI 容器
   apps/android/App.kt                        ← 在这里初始化
   apps/desktop/Main.kt                       ← 在这里初始化
```

### 3. 完全隔离

**替换数据源只需修改 1 行**：

```kotlin
// DependencyContainer.kt
private val discoverRepository: DiscoverRepository by lazy {
    // JikanDiscoverRepository(...)  // 旧的
    AniListDiscoverRepository(...)    // 新的 ✅
}

// ✅ Domain 层：0 行修改
// ✅ Feature 层：0 行修改
// ✅ ViewModel：0 行修改
// ✅ UI：0 行修改
```

---

## 🔧 Gradle 配置

### apps/android/build.gradle.kts

```kotlin
dependencies {
    // Shared modules
    implementation(project(":shared:core:ui"))
    implementation(project(":shared:feature:discover"))
    implementation(project(":shared:domain:discover"))
    implementation(project(":shared:data:jikan"))
    
    // Ktor (用于 DI 容器中创建 HttpClient)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    
    // Android
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
}
```

### apps/desktop/build.gradle.kts

```kotlin
dependencies {
    val commonMain by getting {
        dependencies {
            // Shared modules
            implementation(project(":shared:core:ui"))
            implementation(project(":shared:feature:discover"))
            implementation(project(":shared:domain:discover"))
            implementation(project(":shared:data:jikan"))
            
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
    }
    val desktopMain by getting {
        dependencies {
            implementation(compose.desktop.currentOs)
            
            // Ktor (用于 DI 容器中创建 HttpClient)
            implementation("io.ktor:ktor-client-cio:3.3.3")
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
        }
    }
}
```

---

## 🚀 运行应用

### Android

```bash
# 编译
./gradlew :apps:android:assembleDemoDebug

# 安装
./gradlew :apps:android:installDemoDebug

# 启动
adb shell am start -n com.pusu.indexed.comics/.DiscoverActivity
```

### Desktop

```bash
# 编译
./gradlew :apps:desktop:build

# 运行
./gradlew :apps:desktop:run
```

---

## ✅ 编译状态

```bash
# Feature Discover
./gradlew :shared:feature:discover:build
✅ BUILD SUCCESSFUL in 5s

# Android
./gradlew :apps:android:assembleDemoDebug
✅ BUILD SUCCESSFUL in 15s

# Desktop
./gradlew :apps:desktop:build
✅ BUILD SUCCESSFUL in 3s
```

---

## 🎯 关键优势

### 1. 跨平台

- ✅ 核心逻辑完全共享
- ✅ 只需提供平台特定的 HttpClient
- ✅ ViewModel、UseCase、Repository 都是跨平台的

### 2. 简单直观

- ✅ 不依赖第三方 DI 框架
- ✅ 所有依赖关系清晰可见
- ✅ 便于调试和理解

### 3. 易于测试

```kotlin
// 测试 ViewModel
val mockUseCase = mockk<GetTrendingAnimeUseCase>()
val viewModel = DiscoverViewModel(mockUseCase, testScope)

// 测试 DI
val mockClient = mockk<HttpClient>()
val container = DependencyContainer(mockClient)
```

### 4. 易于扩展

```kotlin
// 添加新功能只需在 DependencyContainer 中添加
class DependencyContainer(httpClient: HttpClient) {
    // 现有代码...
    
    private val searchAnimeUseCase by lazy {
        SearchAnimeUseCase(repository = discoverRepository)
    }
    
    fun createSearchViewModel(scope: CoroutineScope): SearchViewModel {
        return SearchViewModel(searchAnimeUseCase, scope)
    }
}
```

---

## 📚 相关文档

- `shared/feature/discover/README.md` - Feature 层使用说明
- `shared/feature/discover/ARCHITECTURE.md` - 详细架构文档

---

**创建日期**: 2025-12-26  
**状态**: ✅ 完整实现  
**编译**: ✅ 所有平台通过  
**可运行**: ✅ Android, Desktop

