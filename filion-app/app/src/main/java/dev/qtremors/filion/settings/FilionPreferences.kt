package dev.qtremors.filion.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import dev.qtremors.filion.ModelTarget
import org.json.JSONArray
import org.json.JSONObject

private const val PREFS_NAME = "filion_prefs"
private const val KEY_FOLDERS = "scanned_folders"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_DYNAMIC_COLOR = "dynamic_color"
private const val KEY_RECENT_MODELS = "recent_models"
private const val MAX_RECENT_MODELS = 15

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

    fun recentModels(): List<ModelTarget> {
        val raw = preferences.getString(KEY_RECENT_MODELS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            val list = mutableListOf<ModelTarget>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val uriStr = obj.optString("uri")
                if (uriStr.isNotBlank()) {
                    list.add(
                        ModelTarget(
                            uri = Uri.parse(uriStr),
                            displayName = obj.optString("displayName", "Model.glb"),
                            mimeType = obj.optString("mimeType", "model/gltf-binary"),
                            sizeBytes = obj.optLong("sizeBytes", 0L),
                            folderName = obj.optString("folderName", ""),
                            canonicalKey = obj.optString("canonicalKey", uriStr),
                            lastOpenedTimestamp = obj.optLong("lastOpenedTimestamp", 0L)
                        )
                    )
                }
            }
            list.distinctBy { it.canonicalKey }
        }.getOrDefault(emptyList())
    }

    fun addRecentModel(target: ModelTarget) {
        val current = recentModels().toMutableList()
        current.removeAll { it.canonicalKey == target.canonicalKey || it.uri.toString() == target.uri.toString() }
        val updated = target.copy(lastOpenedTimestamp = System.currentTimeMillis())
        current.add(0, updated)
        val trimmed = current.take(MAX_RECENT_MODELS)
        val array = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject().apply {
                put("uri", item.uri.toString())
                put("displayName", item.displayName)
                put("mimeType", item.mimeType)
                put("sizeBytes", item.sizeBytes)
                put("folderName", item.folderName)
                put("canonicalKey", item.canonicalKey)
                put("lastOpenedTimestamp", item.lastOpenedTimestamp)
            }
            array.put(obj)
        }
        preferences.edit { putString(KEY_RECENT_MODELS, array.toString()) }
    }

    fun clearRecentModels() {
        preferences.edit { remove(KEY_RECENT_MODELS) }
    }
}
