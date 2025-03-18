package cz.hlinka.kmpintegrationdemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform