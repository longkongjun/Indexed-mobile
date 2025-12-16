package com.pusu.indexed.jikan.network

/**
 * Result 扩展函数
 * 提供便捷的 Result 操作方法
 */

/**
 * 在成功时执行操作
 * 
 * @param action 成功时执行的操作
 * @return 原始 Result
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (isSuccess) {
        action(getOrNull()!!)
    }
    return this
}

/**
 * 在失败时执行操作
 * 
 * @param action 失败时执行的操作
 * @return 原始 Result
 */
inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    exceptionOrNull()?.let(action)
    return this
}

/**
 * 在 HTTP 错误时执行操作
 * 
 * @param action 处理 HTTP 错误的操作
 * @return 原始 Result
 */
inline fun <T> Result<T>.onHttpError(action: (HttpException) -> Unit): Result<T> {
    exceptionOrNull()?.let { exception ->
        if (exception is HttpException) {
            action(exception)
        }
    }
    return this
}

/**
 * 在网络错误时执行操作
 * 
 * @param action 处理网络错误的操作
 * @return 原始 Result
 */
inline fun <T> Result<T>.onNetworkError(action: (Throwable) -> Unit): Result<T> {
    exceptionOrNull()?.let { exception ->
        if (exception !is HttpException) {
            action(exception)
        }
    }
    return this
}

/**
 * 转换 Result 中的数据
 * 
 * @param transform 转换函数
 * @return 转换后的 Result
 */
inline fun <T, R> Result<T>.mapResult(transform: (T) -> R): Result<R> {
    return try {
        if (isSuccess) {
            Result.success(transform(getOrThrow()))
        } else {
            Result.failure(exceptionOrNull()!!)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 扁平化嵌套的 Result
 * 
 * @param transform 转换函数
 * @return 扁平化后的 Result
 */
inline fun <T, R> Result<T>.flatMapResult(transform: (T) -> Result<R>): Result<R> {
    return try {
        if (isSuccess) {
            transform(getOrThrow())
        } else {
            Result.failure(exceptionOrNull()!!)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 获取数据或提供默认值
 * 
 * @param defaultValue 默认值提供函数
 * @return 数据或默认值
 */
inline fun <T> Result<T>.getOrDefault(defaultValue: () -> T): T {
    return getOrNull() ?: defaultValue()
}

/**
 * 获取数据或抛出自定义异常
 * 
 * @param exceptionMapper 异常映射函数
 * @return 数据
 * @throws Throwable 映射后的异常
 */
inline fun <T> Result<T>.getOrThrow(exceptionMapper: (Throwable) -> Throwable): T {
    return getOrElse { throw exceptionMapper(it) }
}

