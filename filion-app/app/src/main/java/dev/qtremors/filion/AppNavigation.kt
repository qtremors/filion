package dev.qtremors.filion

enum class AppDestination {
    HOME,
    SETTINGS,
    ABOUT,
    LICENSES
}

fun List<AppDestination>.push(destination: AppDestination): List<AppDestination> =
    if (lastOrNull() == destination) this else this + destination

fun List<AppDestination>.pop(): List<AppDestination> =
    if (size <= 1) listOf(AppDestination.HOME) else dropLast(1)
