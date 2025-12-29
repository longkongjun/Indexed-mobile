package com.pusu.indexed.comics

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform