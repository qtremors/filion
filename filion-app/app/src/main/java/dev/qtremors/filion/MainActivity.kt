package dev.qtremors.filion

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.Trace
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import dev.qtremors.filion.about.AboutScreen
import dev.qtremors.filion.about.LicensesScreen
import dev.qtremors.filion.home.HomeScreen
import dev.qtremors.filion.settings.FilionPreferences
import dev.qtremors.filion.settings.FolderAddResult
import dev.qtremors.filion.settings.SettingsScreen
import dev.qtremors.filion.settings.ThemeMode
import dev.qtremors.filion.theme.FilionTheme
import dev.qtremors.filion.theme.bounceClickable
import dev.qtremors.filion.ui.formatViewerFileSize
import dev.qtremors.filion.ui.showFilionToast
import dev.qtremors.filion.viewer.ModelViewerScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installDebugStrictMode()
        val splashScreen = traceStartupSection("Filion.installSplashScreen") {
            installSplashScreen()
        }
        traceStartupSection("Filion.activityOnCreate") {
            super.onCreate(savedInstanceState)
        }

        enableEdgeToEdge()

        val initialTarget = resolveModelTarget(intent)
        val preferences = FilionPreferences(applicationContext)

        var keepSplashScreen = true
        var preloadedModels by mutableStateOf<List<ModelTarget>>(emptyList())
        var preloadedFolders by mutableStateOf<List<FolderItem>>(emptyList())

        lifecycleScope.launch {
            try {
                traceStartupSection("Filion.splashPreload") {
                    withTimeoutOrNull(2000L) {
                        val themeDeferred = async(Dispatchers.IO) {
                            preferences.themeMode
                            preferences.dynamicColor
                        }
                        val foldersDeferred = async(Dispatchers.IO) {
                            loadSavedFolders(applicationContext)
                        }
                        val scanDeferred = async(Dispatchers.IO) {
                            scanLocalGlbFiles(applicationContext)
                        }
                        themeDeferred.await()
                        preloadedFolders = foldersDeferred.await()
                        preloadedModels = scanDeferred.await()
                    }
                }
            } finally {
                keepSplashScreen = false
            }
        }
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        setContent {
            val context = LocalContext.current
            var themeMode by remember { mutableStateOf(preferences.themeMode) }
            var dynamicColor by remember { mutableStateOf(preferences.dynamicColor) }
            val dynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val systemDark = isSystemInDarkTheme()

            FilionTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor,
                darkTheme = systemDark
            ) {
                var activeTarget by remember { mutableStateOf<ModelTarget?>(initialTarget) }
                var localModels by remember { mutableStateOf(preloadedModels) }
                var savedFolderItems by remember { mutableStateOf(preloadedFolders) }
                var destinationStack by remember {
                    mutableStateOf(listOf(AppDestination.HOME))
                }

                LaunchedEffect(preloadedModels, preloadedFolders) {
                    if (preloadedModels.isNotEmpty()) {
                        localModels = preloadedModels
                    }
                    if (preloadedFolders.isNotEmpty()) {
                        savedFolderItems = preloadedFolders
                    }
                }

                BackHandler(enabled = activeTarget == null && destinationStack.size > 1) {
                    destinationStack = destinationStack.pop()
                }

                val pickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent(),
                    onResult = { uri ->
                        if (uri != null) {
                            val displayName = queryColumn(uri, OpenableColumns.DISPLAY_NAME) { cursor, index -> cursor.getString(index) }
                                ?: uri.lastPathSegment
                                ?: "Model.glb"
                            val mimeType = contentResolver.getType(uri) ?: "model/gltf-binary"
                            val isGlb = displayName.endsWith(".glb", ignoreCase = true) ||
                                    mimeType == "model/gltf-binary" ||
                                    mimeType == "application/octet-stream"
                            if (isGlb) {
                                val sizeBytes = queryColumn(uri, OpenableColumns.SIZE) { cursor, index -> cursor.getLong(index) } ?: 0L
                                activeTarget = ModelTarget(
                                    uri = uri,
                                    displayName = displayName,
                                    mimeType = mimeType,
                                    sizeBytes = sizeBytes,
                                    folderName = "External",
                                    canonicalKey = uri.toString()
                                )
                            } else {
                                showFilionToast(
                                    getString(R.string.cannot_open_file, getString(R.string.unsupported_request))
                                )
                            }
                        }
                    }
                )

                val refreshLocalModels: () -> Unit = {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val folders = loadSavedFolders(context)
                        val files = scanLocalGlbFiles(context)
                        withContext(Dispatchers.Main) {
                            savedFolderItems = folders
                            localModels = files
                        }
                    }
                }

                val folderPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree(),
                    onResult = { uri ->
                        if (uri != null) {
                            runCatching {
                                contentResolver.takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                                val result = preferences.addFolder(uri)
                                when (result) {
                                    FolderAddResult.ADDED -> Unit
                                    FolderAddResult.ALREADY_EXISTS -> {
                                        showFilionToast(getString(R.string.folder_already_added))
                                    }
                                    FolderAddResult.COVERED_BY_PARENT -> {
                                        showFilionToast(getString(R.string.folder_already_covered))
                                    }
                                    FolderAddResult.REPLACED_CHILDREN -> {
                                        showFilionToast(getString(R.string.folder_subsumed_existing))
                                    }
                                }
                                refreshLocalModels()
                            }.onFailure { e ->
                                showFilionToast(
                                    getString(
                                        R.string.folder_permission_failed,
                                        e.localizedMessage ?: getString(R.string.unsupported_request)
                                    )
                                )
                            }
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    if (localModels.isEmpty()) {
                        refreshLocalModels()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = activeTarget,
                        transitionSpec = {
                            fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        },
                        label = "modelViewerTransition"
                    ) { target ->
                        if (target != null) {
                            ModelViewerScreen(
                                reference = target.uri.toString(),
                                title = target.displayName,
                                sizeBytes = target.sizeBytes,
                                mimeType = target.mimeType,
                                onNavigateBack = { activeTarget = null },
                                onShare = { shareTarget(target) },
                                onOpenWith = { openTargetWithChooser(target) }
                            )
                        } else {
                            when (destinationStack.last()) {
                                AppDestination.HOME -> HomeScreen(
                                    localModels = localModels,
                                    onSelectFile = { pickerLauncher.launch("*/*") },
                                    onSelectLocalModel = { activeTarget = it },
                                    onAddFolder = { folderPickerLauncher.launch(null) },
                                    onOpenSettings = {
                                        destinationStack = destinationStack.push(AppDestination.SETTINGS)
                                    },
                                    onRefresh = refreshLocalModels
                                )
                                AppDestination.SETTINGS -> SettingsScreen(
                                    themeMode = themeMode,
                                    dynamicColor = dynamicColor,
                                    dynamicColorAvailable = dynamicColorAvailable,
                                    folders = savedFolderItems,
                                    onThemeModeChange = { mode ->
                                        preferences.themeMode = mode
                                        themeMode = mode
                                    },
                                    onDynamicColorChange = { enabled ->
                                        preferences.dynamicColor = enabled
                                        dynamicColor = enabled
                                    },
                                    onAddFolder = { folderPickerLauncher.launch(null) },
                                    onRemoveFolder = { uri ->
                                        preferences.removeFolder(uri)
                                        runCatching {
                                            contentResolver.releasePersistableUriPermission(
                                                uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            )
                                        }
                                        refreshLocalModels()
                                    },
                                    onOpenAbout = {
                                        destinationStack = destinationStack.push(AppDestination.ABOUT)
                                    },
                                    onNavigateBack = { destinationStack = destinationStack.pop() }
                                )
                                AppDestination.ABOUT -> AboutScreen(
                                    onOpenLicenses = {
                                        destinationStack = destinationStack.push(AppDestination.LICENSES)
                                    },
                                    onNavigateBack = { destinationStack = destinationStack.pop() }
                                )
                                AppDestination.LICENSES -> LicensesScreen(
                                    onNavigateBack = { destinationStack = destinationStack.pop() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun resolveModelTarget(intent: Intent): ModelTarget? {
        val uri = intent.data ?: intent.clipData?.getItemAt(0)?.uri ?: return null
        val displayName = queryColumn(uri, OpenableColumns.DISPLAY_NAME) { cursor, index -> cursor.getString(index) }
            ?: uri.lastPathSegment
            ?: "Model.glb"
        val mimeType = intent.type ?: contentResolver.getType(uri) ?: "model/gltf-binary"
        val sizeBytes = queryColumn(uri, OpenableColumns.SIZE) { cursor, index -> cursor.getLong(index) } ?: 0L

        return ModelTarget(
            uri = uri,
            displayName = displayName,
            mimeType = mimeType,
            sizeBytes = sizeBytes
        )
    }

    private fun shareTarget(target: ModelTarget) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = target.mimeType
            putExtra(Intent.EXTRA_STREAM, target.uri)
            clipData = ClipData.newUri(contentResolver, target.displayName, target.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            startActivity(Intent.createChooser(sendIntent, getString(R.string.share)))
        }.onFailure {
            showFilionToast(getString(R.string.no_app_found))
        }
    }

    private fun openTargetWithChooser(target: ModelTarget) {
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(target.uri, target.mimeType)
            clipData = ClipData.newUri(contentResolver, target.displayName, target.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            startActivity(Intent.createChooser(openIntent, getString(R.string.image_gallery_open_with)))
        }.onFailure {
            showFilionToast(getString(R.string.no_app_found))
        }
    }

    private fun <T> queryColumn(uri: Uri, column: String, read: (Cursor, Int) -> T): T? =
        runCatching {
            contentResolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val index = cursor.getColumnIndex(column)
                if (index < 0 || cursor.isNull(index)) null else read(cursor, index)
            }
        }.getOrNull()

    private fun installDebugStrictMode() {
        if (!BuildConfig.DEBUG) return

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build()
        )
    }

    private inline fun <T> traceStartupSection(name: String, block: () -> T): T {
        Trace.beginSection(name)
        return try {
            block()
        } finally {
            Trace.endSection()
        }
    }
}

private fun getFolderDisplayName(context: Context, uri: Uri): String {
    if (uri.scheme == "file") {
        return uri.lastPathSegment ?: "Local Folder"
    }
    return runCatching {
        val documentId = DocumentsContract.getTreeDocumentId(uri)
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(uri, documentId)
        context.contentResolver.query(documentUri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(0)
            } else null
        }
    }.getOrNull() ?: uri.lastPathSegment ?: "Storage Folder"
}

private fun loadSavedFolders(context: Context): List<FolderItem> {
    val uris = FilionPreferences(context).folders()
    return uris.map { uri ->
        FolderItem(uri, getFolderDisplayName(context, uri))
    }
}

private fun scanLocalGlbFiles(context: Context): List<ModelTarget> {
    val results = mutableListOf<ModelTarget>()

    // App-specific external files dir (always accessible)
    val appExtDir = context.getExternalFilesDir(null)
    if (appExtDir != null) {
        scanFileDirectory(appExtDir, "App Storage", results)
    }

    // Saved tree URIs
    val savedFolders = FilionPreferences(context).folders()
    for (treeUri in savedFolders) {
        val folderName = getFolderDisplayName(context, treeUri)
        runCatching {
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            scanTreeUri(context, treeUri, docId, folderName, results)
        }
    }

    return results.distinctBy { it.canonicalKey }
}

private fun scanFileDirectory(dir: File, folderName: String, list: MutableList<ModelTarget>, depth: Int = 0) {
    if (depth > 2) return
    val files = dir.listFiles() ?: return
    for (file in files) {
        if (file.isDirectory) {
            if (!file.name.startsWith(".") && file.name != "Android") {
                scanFileDirectory(file, file.name, list, depth + 1)
            }
        } else if (file.name.endsWith(".glb", ignoreCase = true)) {
            val canonical = runCatching { file.canonicalPath }.getOrDefault(file.absolutePath)
            list.add(
                ModelTarget(
                    uri = Uri.fromFile(file),
                    displayName = file.name,
                    mimeType = "model/gltf-binary",
                    sizeBytes = file.length(),
                    folderName = folderName,
                    canonicalKey = "file:$canonical"
                )
            )
        }
    }
}

private fun scanTreeUri(
    context: Context,
    treeUri: Uri,
    documentId: String,
    currentFolderName: String,
    results: MutableList<ModelTarget>,
    depth: Int = 0
) {
    if (depth > 2) return
    val contentResolver = context.contentResolver
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
    val projection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_SIZE
    )

    runCatching {
        contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)

            while (cursor.moveToNext()) {
                val childId = cursor.getString(idCol)
                val name = cursor.getString(nameCol) ?: "unnamed"
                val mimeType = cursor.getString(mimeCol) ?: ""
                val size = cursor.getLong(sizeCol)

                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    if (!name.startsWith(".")) {
                        scanTreeUri(context, treeUri, childId, name, results, depth + 1)
                    }
                } else if (name.endsWith(".glb", ignoreCase = true) || mimeType == "model/gltf-binary") {
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId)
                    val authority = treeUri.authority ?: "content"
                    results.add(
                        ModelTarget(
                            uri = fileUri,
                            displayName = name,
                            mimeType = if (mimeType.isBlank()) "model/gltf-binary" else mimeType,
                            sizeBytes = size,
                            folderName = currentFolderName,
                            canonicalKey = "$authority:$childId"
                        )
                    )
                }
            }
        }
    }
}

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
    val canonicalKey: String = uri.toString()
)
