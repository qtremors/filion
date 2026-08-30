package dev.qtremors.filion

import android.net.Uri

data class FolderItem(
    val uri: Uri,
    val displayName: String
)

data class ModelTarget(
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val folderName: String = "",
    val canonicalKey: String = uri.toString(),
    val lastOpenedTimestamp: Long = 0L
)
