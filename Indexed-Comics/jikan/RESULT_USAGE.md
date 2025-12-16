# Kotlin Result 使用指南

本文档说明如何使用集成的 Kotlin Result 支持。

## 概述

所有 Jikan API 接口现在都返回 `Result` 类型，提供统一的错误处理机制。

## 基本用法

### 1. 简单调用

```kotlin
// 获取动漫信息
val result = JikanApiClient.animeApi.getAnimeById(1)

result
    .onSuccess { response ->
        // 处理成功响应
        val anime = response.data
        println("动漫名称: ${anime?.title}")
    }
    .onFailure { exception ->
        // 处理失败
        println("错误: ${exception.message}")
    }
```

### 2. 使用 when 表达式

```kotlin
when {
    result.isSuccess -> {
        val response = result.getOrNull()
        // 处理响应
    }
    result.isFailure -> {
        val exception = result.exceptionOrNull()
        // 处理异常
    }
}
```

### 3. 区分 HTTP 错误和网络错误

```kotlin
result
    .onHttpError { httpException ->
        // 处理 HTTP 错误（4xx, 5xx）
        when (httpException.code) {
            404 -> println("资源未找到")
            401 -> println("未授权")
            500 -> println("服务器错误")
            else -> println("HTTP 错误: ${httpException.code}")
        }
        println("错误详情: ${httpException.errorBody}")
    }
    .onNetworkError { exception ->
        // 处理网络错误（连接超时、网络不可用等）
        println("网络错误: ${exception.message}")
    }
```

### 4. 数据转换

```kotlin
// 转换 Result 中的数据
val titleResult = result.mapResult { response ->
    response.data?.title ?: "未知"
}

// 扁平化嵌套的 Result
val detailResult = result.flatMapResult { response ->
    val id = response.data?.malId
    if (id != null) {
        JikanApiClient.animeApi.getAnimeFullById(id)
    } else {
        Result.failure(IllegalStateException("无效的 ID"))
    }
}
```

### 5. 获取数据或默认值

```kotlin
// 获取数据，失败时返回 null
val anime = result.getOrNull()?.data

// 获取数据，失败时返回默认值
val anime = result.getOrDefault {
    JikanResponse(data = Anime(...)) // 默认动漫对象
}.data

// 获取数据，失败时抛出自定义异常
val anime = result.getOrThrow { exception ->
    CustomException("获取动漫失败", exception)
}.data
```

## 协程中的使用

### 1. 基本协程调用

```kotlin
viewModelScope.launch {
    try {
        val result = JikanApiClient.animeApi.getAnimeById(1)
        result
            .onSuccess { response ->
                _uiState.value = UiState.Success(response.data)
            }
            .onFailure { exception ->
                _uiState.value = UiState.Error(exception.message)
            }
    } catch (e: Exception) {
        // 处理协程取消或其他异常
        _uiState.value = UiState.Error(e.message)
    }
}
```

### 2. 使用 Flow

```kotlin
fun getAnimeFlow(id: Int): Flow<Result<JikanResponse<Anime>>> = flow {
    val result = JikanApiClient.animeApi.getAnimeById(id)
    emit(result)
}.flowOn(Dispatchers.IO)

// 使用
viewModelScope.launch {
    getAnimeFlow(1).collect { result ->
        result.onSuccess { response ->
            // 更新 UI
        }
    }
}
```

### 3. 组合多个请求

```kotlin
suspend fun getAnimeWithCharacters(id: Int): Result<AnimeWithCharacters> {
    val animeResult = JikanApiClient.animeApi.getAnimeById(id)
    if (animeResult.isFailure) {
        return Result.failure(animeResult.exceptionOrNull()!!)
    }
    
    val charactersResult = JikanApiClient.animeApi.getAnimeCharacters(id)
    if (charactersResult.isFailure) {
        return Result.failure(charactersResult.exceptionOrNull()!!)
    }
    
    return Result.success(
        AnimeWithCharacters(
            anime = animeResult.getOrNull()?.data,
            characters = charactersResult.getOrNull()?.data
        )
    )
}
```

## ViewModel 集成示例

```kotlin
class AnimeDetailViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<Anime>>(UiState.Loading)
    val uiState: StateFlow<UiState<Anime>> = _uiState.asStateFlow()
    
    fun loadAnime(id: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            val result = JikanApiClient.animeApi.getAnimeById(id)
            
            result
                .onSuccess { response ->
                    response.data?.let { anime ->
                        _uiState.value = UiState.Success(anime)
                    } ?: run {
                        _uiState.value = UiState.Error("数据为空")
                    }
                }
                .onHttpError { httpException ->
                    _uiState.value = UiState.Error(
                        when (httpException.code) {
                            404 -> "动漫未找到"
                            else -> "服务器错误: ${httpException.code}"
                        }
                    )
                }
                .onNetworkError { exception ->
                    _uiState.value = UiState.Error("网络连接失败")
                }
        }
    }
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

## Composable 中的使用

```kotlin
@Composable
fun AnimeDetailScreen(animeId: Int, viewModel: AnimeDetailViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(animeId) {
        viewModel.loadAnime(animeId)
    }
    
    when (val state = uiState) {
        is UiState.Loading -> {
            CircularProgressIndicator()
        }
        is UiState.Success -> {
            AnimeDetailContent(anime = state.data)
        }
        is UiState.Error -> {
            ErrorMessage(message = state.message) {
                viewModel.loadAnime(animeId)
            }
        }
    }
}
```

## 错误处理最佳实践

### 1. 统一错误处理

```kotlin
fun <T> Result<T>.handleCommonErrors(
    onSuccess: (T) -> Unit,
    onCustomError: ((Throwable) -> Unit)? = null
) {
    onHttpError { httpException ->
        when (httpException.code) {
            401 -> // 导航到登录页
            403 -> // 显示无权限提示
            404 -> // 显示资源不存在
            500, 502, 503 -> // 显示服务器错误
            else -> onCustomError?.invoke(httpException)
        }
    }
    .onNetworkError { exception ->
        // 显示网络错误提示
        // 可能触发重试机制
    }
    .onSuccess(onSuccess)
}

// 使用
result.handleCommonErrors(
    onSuccess = { response ->
        // 处理成功
    },
    onCustomError = { exception ->
        // 处理特定错误
    }
)
```

### 2. 重试机制

```kotlin
suspend fun <T> retryOnFailure(
    times: Int = 3,
    delayMillis: Long = 1000,
    block: suspend () -> Result<T>
): Result<T> {
    repeat(times - 1) { attempt ->
        val result = block()
        if (result.isSuccess) return result
        delay(delayMillis * (attempt + 1))
    }
    return block()
}

// 使用
val result = retryOnFailure(times = 3) {
    JikanApiClient.animeApi.getAnimeById(1)
}
```

### 3. 缓存策略

```kotlin
class AnimeRepository {
    private val cache = mutableMapOf<Int, Anime>()
    
    suspend fun getAnime(id: Int, forceRefresh: Boolean = false): Result<Anime> {
        if (!forceRefresh && cache.containsKey(id)) {
            return Result.success(cache[id]!!)
        }
        
        return JikanApiClient.animeApi.getAnimeById(id)
            .mapResult { response ->
                response.data ?: throw NullPointerException("数据为空")
            }
            .onSuccess { anime ->
                cache[id] = anime
            }
    }
}
```

## 注意事项

1. **空值处理**: `response.data` 可能为 null，使用前需要检查
2. **异常传播**: Result 会捕获所有异常，不会抛出到外部
3. **协程取消**: 协程取消不会被 Result 捕获，需要单独处理
4. **性能考虑**: Result 有轻微的性能开销，但在网络请求场景下可以忽略

## API 返回类型

所有 API 接口现在返回以下类型之一：
- `Result<JikanResponse<T>>` - 单个对象响应
- `Result<JikanPageResponse<T>>` - 分页列表响应

其中：
- `JikanResponse<T>` 包含 `data: T?` 字段
- `JikanPageResponse<T>` 包含 `data: List<T>?` 和 `pagination: Pagination?` 字段

