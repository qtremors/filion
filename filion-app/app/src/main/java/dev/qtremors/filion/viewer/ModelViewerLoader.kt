package dev.qtremors.filion.viewer

import android.content.Context
import androidx.core.net.toUri
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import java.io.File
import java.nio.ByteBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun loadSceneViewModelInstance(
    context: Context,
    modelLoader: ModelLoader,
    reference: String
): ModelInstance {
    val uri = runCatching { reference.toUri() }.getOrNull()
    if (uri != null && (uri.scheme == "content" || uri.scheme == "android.resource")) {
        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } ?: error("Unable to open input stream for URI: $reference")
        return withContext(Dispatchers.Main) {
            modelLoader.createModelInstance(ByteBuffer.wrap(bytes))
        }
    }

    if (uri?.scheme == "file") {
        val filePath = uri.path ?: reference
        val file = File(filePath)
        if (file.exists()) {
            val bytes = withContext(Dispatchers.IO) { file.readBytes() }
            return withContext(Dispatchers.Main) {
                modelLoader.createModelInstance(ByteBuffer.wrap(bytes))
            }
        }
    }

    val modelInstance = when (uri?.scheme) {
        "http", "https" ->
            modelLoader.loadModelInstance(reference)
        null, "" -> {
            val file = File(reference)
            if (file.isAbsolute || file.exists()) {
                val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                withContext(Dispatchers.Main) {
                    modelLoader.createModelInstance(ByteBuffer.wrap(bytes))
                }
            } else {
                modelLoader.loadModelInstance(reference)
            }
        }
        else -> modelLoader.loadModelInstance(reference)
    }
    return modelInstance ?: error("Unable to load GLB file")
}
