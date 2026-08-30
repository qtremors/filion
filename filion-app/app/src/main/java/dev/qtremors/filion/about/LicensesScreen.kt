@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package dev.qtremors.filion.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.qtremors.filion.R
import dev.qtremors.filion.theme.bounceClickable
import dev.qtremors.filion.theme.expressiveSegmentedShapes
import dev.qtremors.filion.theme.spacing
import dev.qtremors.filion.ui.FilionScreenScaffold
import dev.qtremors.filion.ui.FilionSectionHeader

private data class LibraryInfo(
    val name: String,
    val license: String,
    val url: String
)

private val libraries = listOf(
    LibraryInfo("AndroidX Core KTX", "Apache 2.0", "https://developer.android.com/jetpack/androidx/releases/core"),
    LibraryInfo("AndroidX Activity Compose", "Apache 2.0", "https://developer.android.com/jetpack/androidx/releases/activity"),
    LibraryInfo("AndroidX Lifecycle Runtime KTX", "Apache 2.0", "https://developer.android.com/jetpack/androidx/releases/lifecycle"),
    LibraryInfo("AndroidX Core Splashscreen", "Apache 2.0", "https://developer.android.com/jetpack/androidx/releases/core"),
    LibraryInfo("Jetpack Compose UI", "Apache 2.0", "https://developer.android.com/jetpack/compose"),
    LibraryInfo("Jetpack Compose Material 3", "Apache 2.0", "https://developer.android.com/jetpack/compose"),
    LibraryInfo("Jetpack Compose Material Icons Extended", "Apache 2.0", "https://developer.android.com/jetpack/compose"),
    LibraryInfo("Kotlin Coroutines", "Apache 2.0", "https://github.com/Kotlin/kotlinx.coroutines"),
    LibraryInfo("Google Filament", "Apache 2.0", "https://github.com/google/filament"),
    LibraryInfo("SceneView", "Apache 2.0", "https://github.com/SceneView/sceneview")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LicensesScreen(onNavigateBack: () -> Unit) {
    val resources = LocalResources.current
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showApacheText by rememberSaveable { mutableStateOf(false) }

    val apacheText = remember(resources) {
        runCatching {
            resources.openRawResource(R.raw.apache_2_0)
                .bufferedReader()
                .use { it.readText() }
        }.getOrDefault("Apache License 2.0")
    }

    FilionScreenScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.open_source_licenses),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .bounceClickable(onClick = onNavigateBack)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                    MaterialTheme.spacing.screenGutter
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SegmentedListItem(
                    onClick = {},
                    shapes = expressiveSegmentedShapes(index = 0, count = 1),
                    content = {
                        Text(
                            text = stringResource(R.string.licenses_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilionSectionHeader(text = stringResource(R.string.licenses_section_libraries))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        libraries.forEachIndexed { index, lib ->
                            SegmentedListItem(
                                onClick = { uriHandler.openUri(lib.url) },
                                shapes = expressiveSegmentedShapes(index = index, count = libraries.size),
                                content = { Text(lib.name) },
                                supportingContent = { Text(lib.license) },
                                leadingContent = {
                                    Box(
                                        modifier = Modifier.fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                trailingContent = {
                                    Box(
                                        modifier = Modifier.fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                colors = ListItemDefaults.segmentedColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                modifier = Modifier.height(IntrinsicSize.Min)
                            )
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
