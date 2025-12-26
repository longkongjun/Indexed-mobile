package com.pusu.indexed.shared.feature.discover.presentation

import com.pusu.indexed.domain.discover.usecase.GetTrendingAnimeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Discover 页面的 ViewModel
 * 
 * 职责：
 * 1. 管理 UI 状态（UiState）
 * 2. 处理用户意图（Intent）
 * 3. 调用 UseCase（业务逻辑）
 * 4. 发送 UI 事件（UiEvent）
 * 
 * 依赖链：
 * UI → ViewModel → UseCase → Repository → API
 * 
 * 注意：这里使用 CoroutineScope 而不是 ViewModel，因为我们需要跨平台支持。
 * 在实际使用中：
 * - Android: 可以继承 androidx.lifecycle.ViewModel
 * - iOS/Desktop/Web: 手动管理生命周期
 */
class DiscoverViewModel(
    private val getTrendingAnimeUseCase: GetTrendingAnimeUseCase,
    private val coroutineScope: CoroutineScope
) {
    // UI 状态流
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState = _uiState.asStateFlow()
    
    // UI 事件流（一次性事件）
    private val _uiEvent = MutableSharedFlow<DiscoverUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()
    
    init {
        // 初始化时自动加载内容
        handleIntent(DiscoverIntent.LoadContent)
    }
    
    /**
     * 处理用户意图
     */
    fun handleIntent(intent: DiscoverIntent) {
        when (intent) {
            is DiscoverIntent.LoadContent -> loadContent()
            is DiscoverIntent.Refresh -> refresh()
            is DiscoverIntent.GetRandomPick -> getRandomPick()
            is DiscoverIntent.OnAnimeClick -> navigateToDetail(intent.animeId)
            is DiscoverIntent.Retry -> retry()
        }
    }
    
    /**
     * 加载内容
     * 
     * 依赖链：
     * ViewModel → UseCase → Repository → Jikan API
     */
    private fun loadContent() {
        coroutineScope.launch {
            // 1. 设置加载状态
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 2. 调用 UseCase 获取热门动漫
            getTrendingAnimeUseCase(page = 1, limit = 10)
                .onSuccess { animeList ->
                    // 3. 成功：更新 UI 状态
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            trendingAnime = animeList,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    // 4. 失败：显示错误
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "未知错误"
                        )
                    }
                    // 发送错误事件
                    _uiEvent.emit(
                        DiscoverUiEvent.ShowError(error.message ?: "加载失败")
                    )
                }
        }
    }
    
    /**
     * 刷新内容
     */
    private fun refresh() {
        coroutineScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            
            getTrendingAnimeUseCase(page = 1, limit = 10)
                .onSuccess { animeList ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            trendingAnime = animeList,
                            error = null
                        )
                    }
                    _uiEvent.emit(DiscoverUiEvent.ShowSuccess("刷新成功"))
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isRefreshing = false, error = error.message)
                    }
                    _uiEvent.emit(
                        DiscoverUiEvent.ShowError(error.message ?: "刷新失败")
                    )
                }
        }
    }
    
    /**
     * 获取随机推荐
     * 
     * TODO: 实现 GetRandomAnimeUseCase
     */
    private fun getRandomPick() {
        // 暂未实现
    }
    
    /**
     * 导航到详情页
     */
    private fun navigateToDetail(animeId: Int) {
        coroutineScope.launch {
            _uiEvent.emit(DiscoverUiEvent.NavigateToDetail(animeId))
        }
    }
    
    /**
     * 重试加载
     */
    private fun retry() {
        loadContent()
    }
    
    /**
     * 清理资源
     * 
     * 在 ViewModel 销毁时调用（如果有生命周期管理的话）
     */
    fun onCleared() {
        // 取消所有协程
        // 注意：在实际使用中，coroutineScope 应该在这里被取消
    }
}

