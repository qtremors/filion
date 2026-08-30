package dev.qtremors.filion.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.edit

private const val PREFS_NAME = "filion_prefs"
private const val KEY_FOLDERS = "scanned_folders"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_DYNAMIC_COLOR = "dynamic_color"

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    OLED;

    companion object {
        fun fromStoredValue(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

fun ThemeMode.resolveDarkTheme(systemDark: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK,
    ThemeMode.OLED -> true
}

enum class FolderAddResult {
    ADDED,
    ALREADY_EXISTS,
    COVERED_BY_PARENT,
    REPLACED_CHILDREN
}

fun extractFolderDocId(uri: Uri): String {
    return runCatching {
        android.provider.DocumentsContract.getTreeDocumentId(uri)
    }.getOrNull() ?: uri.path.orEmpty()
}

fun isSameFolder(uri1: Uri, uri2: Uri): Boolean {
    if (uri1 == uri2) return true
    if (uri1.scheme != uri2.scheme || uri1.authority != uri2.authority) return false
    val doc1 = extractFolderDocId(uri1).trimEnd('/')
    val doc2 = extractFolderDocId(uri2).trimEnd('/')
    return doc1.isNotEmpty() && doc1 == doc2
}

fun isSubfolder(childUri: Uri, parentUri: Uri): Boolean {
    if (childUri == parentUri) return false
    if (childUri.scheme != parentUri.scheme) return false
    if (childUri.authority != parentUri.authority) return false

    if (childUri.scheme == "file") {
        val childPath = childUri.path?.trimEnd('/') ?: return false
        val parentPath = parentUri.path?.trimEnd('/') ?: return false
        return childPath.startsWith("$parentPath/")
    }

    val childDocId = extractFolderDocId(childUri).trimEnd('/')
    val parentDocId = extractFolderDocId(parentUri).trimEnd('/')

    if (childDocId.isEmpty() || parentDocId.isEmpty()) return false
    if (childDocId == parentDocId) return false

    if (parentDocId.endsWith(":")) {
        return childDocId.startsWith(parentDocId)
    }
    return childDocId.startsWith("$parentDocId/") || childDocId.startsWith("$parentDocId:")
}

fun pruneRedundantFolders(folders: List<Uri>): List<Uri> {
    val result = mutableListOf<Uri>()
    for (folder in folders) {
        val hasAncestor = folders.any { other ->
            other != folder && isSubfolder(childUri = folder, parentUri = other)
        }
        if (!hasAncestor && result.none { isSameFolder(it, folder) }) {
            result.add(folder)
        }
    }
    return result
}

class FilionPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() = ThemeMode.fromStoredValue(preferences.getString(KEY_THEME_MODE, null))
        set(value) {
            preferences.edit { putString(KEY_THEME_MODE, value.name) }
        }

    var dynamicColor: Boolean
        get() = preferences.getBoolean(KEY_DYNAMIC_COLOR, true)
        set(value) {
            preferences.edit { putBoolean(KEY_DYNAMIC_COLOR, value) }
        }

    fun folders(): List<Uri> = preferences
        .getStringSet(KEY_FOLDERS, emptySet())
        .orEmpty()
        .map(Uri::parse)
        .let(::pruneRedundantFolders)
        .sortedBy(Uri::toString)

    fun addFolder(uri: Uri): FolderAddResult {
        val current = folders()
        if (current.any { isSameFolder(it, uri) }) {
            return FolderAddResult.ALREADY_EXISTS
        }
        if (current.any { isSubfolder(childUri = uri, parentUri = it) }) {
            return FolderAddResult.COVERED_BY_PARENT
        }

        val subfolders = current.filter { isSubfolder(childUri = it, parentUri = uri) }
        updateFolders { set ->
            subfolders.forEach { sub -> set.remove(sub.toString()) }
            set.add(uri.toString())
        }

        return if (subfolders.isNotEmpty()) {
            FolderAddResult.REPLACED_CHILDREN
        } else {
            FolderAddResult.ADDED
        }
    }

    fun removeFolder(uri: Uri) {
        updateFolders { set ->
            set.removeAll { stored -> isSameFolder(Uri.parse(stored), uri) }
        }
    }

    private fun updateFolders(change: (MutableSet<String>) -> Unit) {
        val folders = preferences
            .getStringSet(KEY_FOLDERS, emptySet())
            .orEmpty()
            .toMutableSet()
        change(folders)
        preferences.edit { putStringSet(KEY_FOLDERS, folders) }
    }
}
