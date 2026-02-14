package com.pusu.indexed.core.utils

actual object TimeUtils {
    actual fun currentTimeMillis(): Long = java.lang.System.currentTimeMillis()
}
