# 启动页设计文档

## 概述

启动页（Splash Screen）是应用启动时的第一个页面，用于：
1. 展示品牌 Logo
2. 初始化应用资源
3. 提供流畅的启动体验

## 实现方案

### 1. 基础实现（已完成）

**位置**: `composeApp/src/commonMain/kotlin/com/pusu/indexed/comics/splash/SplashScreen.kt`

**特点**:
- 简单的渐显动画
- 2 秒总时长（800ms 动画 + 1200ms 停留）
- 跨平台统一体验

**使用方法**:
```kotlin
SplashScreen(
    onSplashFinished = {
        // 跳转到主页
    }
)
```

### 2. 增强功能（可选）

#### 2.1 初始化逻辑

在启动页期间执行初始化：

```kotlin
@Composable
fun SplashScreen(
    dependencyContainer: DependencyContainer,
    onSplashFinished: () -> Unit
) {
    var initializationComplete by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // 1. 预加载数据
        dependencyContainer.preloadData()
        
        // 2. 检查网络连接
        checkNetworkConnection()
        
        // 3. 标记完成
        initializationComplete = true
    }
    
    LaunchedEffect(initializationComplete) {
        if (initializationComplete) {
            delay(500) // 额外停留，确保用户看到 Logo
            onSplashFinished()
        }
    }
    
    // UI 显示...
}
```

#### 2.2 错误处理

处理初始化失败的情况：

```kotlin
sealed class SplashState {
    object Loading : SplashState()
    object Success : SplashState()
    data class Error(val message: String) : SplashState()
}

@Composable
fun SplashScreen(...) {
    var state by remember { mutableStateOf<SplashState>(SplashState.Loading) }
    
    when (val currentState = state) {
        is SplashState.Loading -> {
            // 显示加载动画
        }
        is SplashState.Success -> {
            // 自动跳转
        }
        is SplashState.Error -> {
            // 显示错误和重试按钮
            ErrorView(message = currentState.message) {
                state = SplashState.Loading
            }
        }
    }
}
```

#### 2.3 自定义 Logo

替换 Emoji 为真实图片：

```kotlin
// 方法 1: 使用 Compose Resources
AsyncImage(
    model = Res.drawable.app_logo,
    contentDescription = "Logo"
)

// 方法 2: 使用 Painter
Image(
    painter = painterResource(Res.drawable.app_logo),
    contentDescription = "Logo",
    modifier = Modifier.size(120.dp)
)
```

### 3. Android 原生启动页（可选）

对于 Android 平台，可以使用原生启动页减少白屏时间：

**步骤**:
1. 创建 `res/drawable/splash_background.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@color/splash_background"/>
    <item>
        <bitmap
            android:gravity="center"
            android:src="@drawable/ic_logo"/>
    </item>
</layer-list>
```

2. 创建 Splash Theme (`res/values/themes.xml`):
```xml
<style name="Theme.Splash" parent="android:Theme">
    <item name="android:windowBackground">@drawable/splash_background</item>
    <item name="android:windowNoTitle">true</item>
    <item name="android:windowFullscreen">true</item>
</style>
```

3. 在 `AndroidManifest.xml` 中使用:
```xml
<activity
    android:name=".MainActivity"
    android:theme="@style/Theme.Splash">
    <!-- ... -->
</activity>
```

4. 在 `MainActivity.onCreate()` 中切换主题:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // 切换到正常主题
    setTheme(R.style.Theme_IndexedComics)
    super.onCreate(savedInstanceState)
}
```

### 4. 时长控制

根据用途调整启动页时长：

| 场景 | 建议时长 | 说明 |
|------|---------|------|
| 纯展示 | 1-2 秒 | 仅显示品牌 |
| 轻量初始化 | 2-3 秒 | 加载配置 |
| 重度初始化 | 3-5 秒 | 预加载数据 |
| 动态时长 | 不定 | 根据初始化完成时间 |

**当前实现**: 2 秒（800ms 动画 + 1200ms 停留）

### 5. 设计建议

1. **保持简洁**: 启动页不是广告位
2. **统一品牌**: 与应用主题风格一致
3. **避免过长**: 用户耐心有限
4. **提供进度**: 如果时间较长，显示进度条
5. **优雅过渡**: 使用淡入淡出等过渡动画

## 文件结构

```
composeApp/src/commonMain/kotlin/
├── com/pusu/indexed/comics/
│   ├── splash/
│   │   └── SplashScreen.kt          # 启动页 UI
│   └── navigation/
│       └── AppNavigation.kt         # 包含 Screen.Splash
```

## 页面流程

```
App 启动
    ↓
SplashScreen (2s)
    ├── 渐显动画 (800ms)
    ├── 停留展示 (1200ms)
    └── onSplashFinished()
        ↓
    DiscoverScreen (主页)
```

## 跨平台行为

| 平台 | 行为 |
|------|------|
| Android | Compose 启动页 + 可选原生启动页 |
| iOS | Compose 启动页 + LaunchScreen.storyboard |
| Desktop | Compose 启动页 |
| Web | Compose 启动页 + HTML loading |

## 自定义配置

在 `AppNavigation.kt` 中可以调整启动页行为：

```kotlin
// 禁用启动页（直接进入主页）
var currentScreen by remember { 
    mutableStateOf<Screen>(Screen.Discover) 
}

// 启用启动页（默认）
var currentScreen by remember { 
    mutableStateOf<Screen>(Screen.Splash) 
}
```

## 性能考虑

1. **避免阻塞**: 初始化逻辑应在后台线程执行
2. **按需加载**: 不要在启动页加载所有资源
3. **缓存策略**: 合理使用缓存减少加载时间
4. **监控时长**: 追踪启动页时长，优化用户体验

## 测试建议

1. 测试不同网络条件下的启动时间
2. 测试初始化失败的降级方案
3. 测试跨平台一致性
4. 测试动画流畅度

## 后续优化

- [ ] 添加加载进度指示器
- [ ] 支持跳过启动页
- [ ] 添加启动页配置项（时长、动画等）
- [ ] 优化启动性能
- [ ] A/B 测试不同设计方案

