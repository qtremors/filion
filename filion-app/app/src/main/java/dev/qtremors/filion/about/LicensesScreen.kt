package dev.qtremors.filion.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.qtremors.filion.R

private data class LibraryNotice(
    val name: String,
    val detail: String,
    val url: String
)

private val libraryNotices = listOf(
    LibraryNotice(
        "AndroidX and Jetpack Compose",
        "Apache License 2.0",
        "https://github.com/androidx/androidx"
    ),
    LibraryNotice(
        "Kotlin Coroutines",
        "Apache License 2.0",
        "https://github.com/Kotlin/kotlinx.coroutines"
    ),
    LibraryNotice(
        "Google Filament",
        "Apache License 2.0",
        "https://github.com/google/filament"
    ),
    LibraryNotice(
        "SceneView",
        "Apache License 2.0",
        "https://github.com/SceneView/sceneview"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showApacheText by rememberSaveable { mutableStateOf(false) }
    val apacheText = remember(context) {
        context.resources.openRawResource(R.raw.apache_2_0)
            .bufferedReader()
            .use { it.readText() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.open_source_licenses)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.licenses_notice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.licenses_libraries),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            libraryNotices.forEachIndexed { index, library ->
                                ListItem(
                                    headlineContent = { Text(library.name) },
                                    supportingContent = { Text(library.detail) },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingContent = {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                                    },
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri(library.url)
                                    }
                                )
                                if (index < libraryNotices.lastIndex) HorizontalDivider()
                            }
                        }
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = { showApacheText = !showApacheText },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(
                            if (showApacheText) R.string.hide_apache_license else R.string.show_apache_license
                        )
                    )
                }
            }
            if (showApacheText) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = apacheText,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
