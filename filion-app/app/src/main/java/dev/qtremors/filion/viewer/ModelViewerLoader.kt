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
        val modelInstance = withContext(Dispatchers.Main) {
            modelLoader.createModelInstance(ByteBuffer.wrap(bytes))
        }
        return modelInstance ?: error("Unable to parse model from URI: $reference")
    }

    val modelInstance = when (uri?.scheme) {
        "file", "http", "https" ->
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
