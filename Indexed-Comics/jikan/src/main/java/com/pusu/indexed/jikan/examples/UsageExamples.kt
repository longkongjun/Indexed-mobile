package com.pusu.indexed.jikan.examples

import com.pusu.indexed.jikan.JikanApiClient
import com.pusu.indexed.jikan.network.onHttpError
import com.pusu.indexed.jikan.network.onNetworkError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Jikan API 使用示例
 * 
 * 这些示例展示了如何使用 Result 类型处理 API 响应
 */
object UsageExamples {
    
    /**
     * 示例 1: 基本用法
     * 获取动漫信息并处理成功和失败情况
     */
    fun example1BasicUsage() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = JikanApiClient.animeApi.getAnimeById(1)
            
            result
                .onSuccess { response ->
                    // 处理成功响应
                    val anime = response.data
                    println("✅ 动漫名称: ${anime?.title}")
                    println("   评分: ${anime?.score}")
                    println("   集数: ${anime?.episodes}")
                }
                .onFailure { exception ->
                    // 处理失败
                    println("❌ 错误: ${exception.message}")
                }
        }
    }
    
    /**
     * 示例 2: 区分 HTTP 错误和网络错误
     * 根据不同的错误类型采取不同的处理措施
     */
    fun example2ErrorHandling() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = JikanApiClient.animeApi.getAnimeById(999999)
            
            result
                .onSuccess { response ->
                    println("✅ 成功获取数据")
                }
                .onHttpError { httpException ->
                    // 处理 HTTP 错误（4xx, 5xx）
                    when (httpException.code) {
                        404 -> println("❌ 动漫未找到")
                        401 -> println("❌ 未授权访问")
                        500 -> println("❌ 服务器内部错误")
                        else -> println("❌ HTTP 错误: ${httpException.code}")
                    }
                    println("   错误详情: ${httpException.errorBody}")
                }
                .onNetworkError { exception ->
                    // 处理网络错误（连接超时、网络不可用等）
                    println("❌ 网络错误: ${exception.message}")
                    println("   建议: 请检查网络连接")
                }
        }
    }
    
    /**
     * 示例 3: 搜索动漫
     * 展示如何使用搜索 API 并处理分页响应
     */
    fun example3SearchAnime() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = JikanApiClient.animeApi.searchAnime(
                query = "Naruto",
                type = "tv",
                page = 1,
                limit = 10
            )
            
            result.onSuccess { response ->
                val animeList = response.data
                val pagination = response.pagination
                
                println("✅ 搜索结果:")
                println("   找到 ${animeList?.size ?: 0} 部动漫")
                println("   当前页: ${pagination?.currentPage}")
                println("   是否有下一页: ${pagination?.hasNextPage}")
                
                animeList?.forEach { anime ->
                    println("   - ${anime.title} (评分: ${anime.score})")
                }
            }
        }
    }
    
    /**
     * 示例 4: 组合多个请求
     * 获取动漫信息和角色信息
     */
    fun example4CombineRequests() {
        CoroutineScope(Dispatchers.IO).launch {
            val animeId = 1
            
            // 获取动漫信息
            val animeResult = JikanApiClient.animeApi.getAnimeById(animeId)
            if (animeResult.isFailure) {
                println("❌ 获取动漫信息失败")
                return@launch
            }
            
            // 获取角色信息
            val charactersResult = JikanApiClient.animeApi.getAnimeCharacters(animeId)
            if (charactersResult.isFailure) {
                println("❌ 获取角色信息失败")
                return@launch
            }
            
            // 处理数据
            val anime = animeResult.getOrNull()?.data
            val characters = charactersResult.getOrNull()?.data
            
            println("✅ 动漫: ${anime?.title}")
            println("   角色数量: ${characters?.size ?: 0}")
            characters?.take(5)?.forEach { character ->
                println("   - ${character.character?.name} (${character.role})")
            }
        }
    }
    
    /**
     * 示例 5: 使用 mapResult 转换数据
     * 从响应中提取特定数据
     */
    fun example5DataTransformation() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = JikanApiClient.animeApi.getAnimeById(1)
            
            // 转换为只包含标题的 Result
            val titleResult = result.mapResult { response ->
                response.data?.title ?: "未知标题"
            }
            
            titleResult.onSuccess { title ->
                println("✅ 动漫标题: $title")
            }
        }
    }
    
    /**
     * 示例 6: 获取排行榜
     * 展示如何使用排行榜 API
     */
    fun example6TopAnime() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = JikanApiClient.topApi.getTopAnime(
                type = "tv",
                filter = "bypopularity",
                page = 1,
                limit = 10
            )
            
            result.onSuccess { response ->
                println("✅ 热门动漫排行榜:")
                response.data?.forEachIndexed { index, anime ->
                    println("   ${index + 1}. ${anime.title}")
                    println("      人气: #${anime.popularity}")
                    println("      评分: ${anime.score}")
                }
            }
        }
    }
    
    /**
     * 示例 7: 获取当前季度动漫
     * 展示如何使用季度 API
     */
    fun example7CurrentSeason() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = JikanApiClient.seasonsApi.getCurrentSeasonAnime(
                page = 1,
                limit = 10
            )
            
            result.onSuccess { response ->
                println("✅ 本季度动漫:")
                response.data?.forEach { anime ->
                    println("   - ${anime.title}")
                    println("     类型: ${anime.type}")
                    println("     播放时间: ${anime.broadcast?.string}")
                }
            }
        }
    }
    
    /**
     * 示例 8: 使用 getOrDefault 提供默认值
     * 处理可能失败的请求并提供备用数据
     */
    fun example8GetOrDefault() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = JikanApiClient.animeApi.getAnimeById(1)
            
            // 获取数据，失败时使用默认值
            val anime = result.getOrNull()?.data
            
            val title = anime?.title ?: "未知动漫"
            val score = anime?.score ?: 0.0
            
            println("动漫: $title")
            println("评分: $score")
        }
    }
    
    /**
     * 示例 9: 随机获取动漫
     * 展示如何使用随机 API
     */
    fun example9RandomAnime() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = JikanApiClient.randomApi.getRandomAnime()
            
            result.onSuccess { response ->
                val anime = response.data
                println("🎲 随机动漫:")
                println("   标题: ${anime?.title}")
                println("   类型: ${anime?.type}")
                println("   评分: ${anime?.score}")
                println("   简介: ${anime?.synopsis?.take(100)}...")
            }
        }
    }
    
    /**
     * 示例 10: 搜索角色
     * 展示如何使用角色搜索 API
     */
    fun example10SearchCharacters() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = JikanApiClient.charactersApi.searchCharacters(
                query = "Naruto",
                page = 1,
                limit = 5
            )
            
            result.onSuccess { response ->
                println("✅ 搜索角色:")
                response.data?.forEach { character ->
                    println("   - ${character.name}")
                    println("     收藏数: ${character.favorites}")
                }
            }
        }
    }
}

/**
 * 运行所有示例
 */
fun main() {
    println("=== Jikan API 使用示例 ===\n")
    
    println("示例 1: 基本用法")
    UsageExamples.example1BasicUsage()
    Thread.sleep(1000)
    
    println("\n示例 2: 错误处理")
    UsageExamples.example2ErrorHandling()
    Thread.sleep(1000)
    
    println("\n示例 3: 搜索动漫")
    UsageExamples.example3SearchAnime()
    Thread.sleep(1000)
    
    println("\n示例 4: 组合多个请求")
    UsageExamples.example4CombineRequests()
    Thread.sleep(1000)
    
    println("\n示例 5: 数据转换")
    UsageExamples.example5DataTransformation()
    Thread.sleep(1000)
    
    // 等待所有协程完成
    Thread.sleep(5000)
}

