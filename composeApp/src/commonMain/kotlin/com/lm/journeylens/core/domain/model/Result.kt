package com.lm.journeylens.core.domain.model

/**
 * 统一结果类型
 * 用于 UseCase 返回值，封装成功/失败状态
 */
sealed class Result<out T> {
    /**
     * 成功结果
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * 失败结果
     */
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error"
    ) : Result<Nothing>()
    
    /**
     * 是否成功
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * 是否失败
     */
    val isError: Boolean get() = this is Error
    
    /**
     * 获取数据（仅成功时）
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    /**
     * 获取数据或默认值
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> default
    }
    
    /**
     * 获取数据或抛出异常
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }
    
    /**
     * 映射成功值
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    /**
     * 处理结果
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }
    
    companion object {
        /**
         * 安全执行，捕获异常
         */
        inline fun <T> runCatching(block: () -> T): Result<T> = try {
            Success(block())
        } catch (e: Throwable) {
            Error(e)
        }
    }
}
