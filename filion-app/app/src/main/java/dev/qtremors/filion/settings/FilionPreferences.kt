package dev.qtremors.filion.settings

import android.content.Context
import android.net.Uri

private const val PREFS_NAME = "filion_prefs"
private const val KEY_FOLDERS = "scanned_folders"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_DYNAMIC_COLOR = "dynamic_color"

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromStoredValue(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

fun ThemeMode.resolveDarkTheme(systemDark: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

class FilionPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() = ThemeMode.fromStoredValue(preferences.getString(KEY_THEME_MODE, null))
        set(value) {
            preferences.edit().putString(KEY_THEME_MODE, value.name).apply()
        }

    var dynamicColor: Boolean
        get() = preferences.getBoolean(KEY_DYNAMIC_COLOR, true)
        set(value) {
            preferences.edit().putBoolean(KEY_DYNAMIC_COLOR, value).apply()
        }

    fun folders(): List<Uri> = preferences
        .getStringSet(KEY_FOLDERS, emptySet())
        .orEmpty()
        .map(Uri::parse)
        .sortedBy(Uri::toString)

    fun addFolder(uri: Uri) {
        updateFolders { it.add(uri.toString()) }
    }

    fun removeFolder(uri: Uri) {
        updateFolders { it.remove(uri.toString()) }
    }

    private fun updateFolders(change: (MutableSet<String>) -> Unit) {
        val folders = preferences
            .getStringSet(KEY_FOLDERS, emptySet())
            .orEmpty()
            .toMutableSet()
        change(folders)
        preferences.edit().putStringSet(KEY_FOLDERS, folders).apply()
    }
}
