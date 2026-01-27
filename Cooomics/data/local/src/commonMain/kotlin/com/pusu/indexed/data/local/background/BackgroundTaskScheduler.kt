package com.pusu.indexed.data.local.background

/**
 * 后台任务调度器（跨平台接口）
 * 
 * 封装平台特定的后台任务调度实现：
 * - Android: WorkManager
 * - iOS: BGTaskScheduler
 */
expect class BackgroundTaskScheduler {
    /**
     * 调度周期性同步任务
     * 
     * @param intervalMinutes 间隔时间（分钟）
     * @param constraints 任务约束
     */
    fun scheduleSyncTask(
        intervalMinutes: Long,
        constraints: TaskConstraints
    )

    /**
     * 调度一次性同步任务
     * 
     * @param delayMinutes 延迟时间（分钟）
     * @param constraints 任务约束
     */
    fun scheduleOneTimeSyncTask(
        delayMinutes: Long = 0,
        constraints: TaskConstraints
    )

    /**
     * 取消所有同步任务
     */
    fun cancelAllSyncTasks()

    /**
     * 取消特定任务
     * 
     * @param taskId 任务 ID
     */
    fun cancelTask(taskId: String)

    /**
     * 检查任务是否已调度
     * 
     * @param taskId 任务 ID
     * @return 是否已调度
     */
    fun isTaskScheduled(taskId: String): Boolean
}

/**
 * 任务约束
 */
data class TaskConstraints(
    /**
     * 是否需要网络连接
     * 刮削任务需要网络，纯扫描可选
     */
    val requiresNetwork: Boolean = false,

    /**
     * 需要的网络类型
     */
    val networkType: NetworkType = NetworkType.ANY,

    /**
     * 是否需要设备充电
     * 可选约束，避免消耗电池
     */
    val requiresCharging: Boolean = false,

    /**
     * 是否需要设备空闲
     * Android Doze 模式相关
     */
    val requiresDeviceIdle: Boolean = false,

    /**
     * 是否需要电池不低
     * 避免在低电量时执行
     */
    val requiresBatteryNotLow: Boolean = true
) {
    companion object {
        /**
         * 默认约束（适合常规同步）
         */
        val DEFAULT = TaskConstraints(
            requiresNetwork = false,
            networkType = NetworkType.ANY,
            requiresCharging = false,
            requiresDeviceIdle = false,
            requiresBatteryNotLow = true
        )

        /**
         * 宽松约束（尽快执行）
         */
        val RELAXED = TaskConstraints(
            requiresNetwork = false,
            networkType = NetworkType.ANY,
            requiresCharging = false,
            requiresDeviceIdle = false,
            requiresBatteryNotLow = false
        )

        /**
         * 严格约束（仅在充电且有网络时）
         */
        val STRICT = TaskConstraints(
            requiresNetwork = true,
            networkType = NetworkType.UNMETERED,
            requiresCharging = true,
            requiresDeviceIdle = false,
            requiresBatteryNotLow = true
        )

        /**
         * 刮削任务约束（需要网络）
         */
        val SCRAPING = TaskConstraints(
            requiresNetwork = true,
            networkType = NetworkType.ANY,
            requiresCharging = false,
            requiresDeviceIdle = false,
            requiresBatteryNotLow = true
        )
    }
}

/**
 * 网络类型
 */
enum class NetworkType {
    /**
     * 不需要网络
     */
    NONE,

    /**
     * 任何网络（包括移动网络）
     */
    ANY,

    /**
     * 仅 Wi-Fi 或其他非计量网络
     */
    UNMETERED
}
