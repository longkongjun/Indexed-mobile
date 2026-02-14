package com.pusu.indexed.core.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual object TimeUtils {
    @OptIn(ExperimentalForeignApi::class)
    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
}
