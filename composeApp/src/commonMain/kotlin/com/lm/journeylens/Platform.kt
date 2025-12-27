package com.lm.journeylens

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform