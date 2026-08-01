package dev.qtremors.filion

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.qtremors.filion.about.AboutScreen
import dev.qtremors.filion.about.LicensesScreen
import dev.qtremors.filion.settings.FilionPreferences
import dev.qtremors.filion.settings.SettingsScreen
import dev.qtremors.filion.settings.resolveDarkTheme
import dev.qtremors.filion.theme.FilionTheme
import dev.qtremors.filion.ui.formatViewerFileSize
import dev.qtremors.filion.viewer.ModelViewerScreen
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialTarget = resolveModelTarget(intent)

        setContent {
            val context = LocalContext.current
            val preferences = remember { FilionPreferences(context.applicationContext) }
            var themeMode by remember { mutableStateOf(preferences.themeMode) }
            var dynamicColor by remember { mutableStateOf(preferences.dynamicColor) }
            val dynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val systemDark = isSystemInDarkTheme()

            FilionTheme(
                darkTheme = themeMode.resolveDarkTheme(systemDark),
                dynamicColor = dynamicColor && dynamicColorAvailable
            ) {
                var activeTarget by remember { mutableStateOf<ModelTarget?>(initialTarget) }
                var localModels by remember { mutableStateOf<List<ModelTarget>>(emptyList()) }
                var savedFolderItems by remember { mutableStateOf<List<FolderItem>>(emptyList()) }
                var destinationStack by remember {
                    mutableStateOf(listOf(AppDestination.HOME))
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
                                activeTarget = ModelTarget(uri, displayName, mimeType, sizeBytes)
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.cannot_open_file, getString(R.string.unsupported_request)),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )

                val refreshLocalModels = {
                    localModels = emptyList()
                    savedFolderItems = loadSavedFolders(context)
                    Thread {
                        val files = scanLocalGlbFiles(context)
                        runOnUiThread { localModels = files }
                    }.start()
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
                                preferences.addFolder(uri)
                                refreshLocalModels()
                            }.onFailure { e ->
                                Toast.makeText(
                                    context,
                                    getString(
                                        R.string.folder_permission_failed,
                                        e.localizedMessage ?: getString(R.string.unsupported_request)
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    refreshLocalModels()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val target = activeTarget
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
            Toast.makeText(this, getString(R.string.no_app_found), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, getString(R.string.no_app_found), Toast.LENGTH_SHORT).show()
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

    // 1. Scan app-specific external files dir (no permissions needed, always accessible)
    val appExtDir = context.getExternalFilesDir(null)
    if (appExtDir != null) {
        scanFileDirectory(appExtDir, results)
    }

    // 2. Scan saved tree URIs
    val savedFolders = FilionPreferences(context).folders()
    for (treeUri in savedFolders) {
        runCatching {
            scanTreeUri(context, treeUri, DocumentsContract.getTreeDocumentId(treeUri), results)
        }
    }

    return results.distinctBy { it.uri.toString() }
}

private fun scanFileDirectory(dir: File, list: MutableList<ModelTarget>, depth: Int = 0) {
    if (depth > 2) return
    val files = dir.listFiles() ?: return
    for (file in files) {
        if (file.isDirectory) {
            if (!file.name.startsWith(".") && file.name != "Android") {
                scanFileDirectory(file, list, depth + 1)
            }
        } else if (file.name.endsWith(".glb", ignoreCase = true)) {
            list.add(
                ModelTarget(
                    uri = Uri.fromFile(file),
                    displayName = file.name,
                    mimeType = "model/gltf-binary",
                    sizeBytes = file.length()
                )
            )
        }
    }
}

private fun scanTreeUri(
    context: Context,
    treeUri: Uri,
    documentId: String,
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
                        scanTreeUri(context, treeUri, childId, results, depth + 1)
                    }
                } else if (name.endsWith(".glb", ignoreCase = true) || mimeType == "model/gltf-binary") {
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId)
                    results.add(
                        ModelTarget(
                            uri = fileUri,
                            displayName = name,
                            mimeType = if (mimeType.isBlank()) "model/gltf-binary" else mimeType,
                            sizeBytes = size
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    localModels: List<ModelTarget>,
    onSelectFile: () -> Unit,
    onSelectLocalModel: (ModelTarget) -> Unit,
    onAddFolder: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // App header with refresh option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh_models)
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.open_settings)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main actions panel
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelectFile)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.open_model),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.open_model_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.models_discovered),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (localModels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.no_models_found),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = stringResource(R.string.no_models_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FilledTonalButton(onClick = onAddFolder) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_scan_folder))
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(localModels) { model ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLocalModel(model) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ViewInAr,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = model.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = formatViewerFileSize(model.sizeBytes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
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
    val sizeBytes: Long
)
