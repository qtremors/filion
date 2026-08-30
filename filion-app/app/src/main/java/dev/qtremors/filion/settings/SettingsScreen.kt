@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package dev.qtremors.filion.settings

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.qtremors.filion.FolderItem
import dev.qtremors.filion.R
import dev.qtremors.filion.theme.bounceClickable
import dev.qtremors.filion.theme.expressiveSegmentedShapes
import dev.qtremors.filion.theme.spacing
import dev.qtremors.filion.ui.ExpressiveSwitch
import dev.qtremors.filion.ui.FilionListSurface
import dev.qtremors.filion.ui.FilionScreenScaffold
import dev.qtremors.filion.ui.FilionSectionHeader

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    dynamicColorAvailable: Boolean,
    folders: List<FolderItem>,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onAddFolder: () -> Unit,
    onRemoveFolder: (Uri) -> Unit,
    onOpenAbout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    FilionScreenScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
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
            // Appearance Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilionSectionHeader(text = stringResource(R.string.section_appearance))
                    FilionListSurface {
                        ThemeModeSelector(
                            currentMode = themeMode,
                            onModeSelected = onThemeModeChange
                        )
                    }
                    SegmentedListItem(
                        checked = dynamicColor && dynamicColorAvailable,
                        onCheckedChange = onDynamicColorChange,
                        enabled = dynamicColorAvailable,
                        shapes = expressiveSegmentedShapes(index = 0, count = 1),
                        leadingContent = {
                            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        content = { Text(stringResource(R.string.dynamic_color)) },
                        supportingContent = {
                            Text(
                                if (dynamicColorAvailable) {
                                    stringResource(R.string.dynamic_color_description)
                                } else {
                                    stringResource(R.string.dynamic_color_unavailable)
                                }
                            )
                        },
                        trailingContent = {
                            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                ExpressiveSwitch(
                                    checked = dynamicColor && dynamicColorAvailable,
                                    onCheckedChange = if (dynamicColorAvailable) onDynamicColorChange else null,
                                    enabled = dynamicColorAvailable
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

            // Scan Folders Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilionSectionHeader(text = stringResource(R.string.section_scan_folders))
                        TextButton(onClick = onAddFolder) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(stringResource(R.string.add_folder))
                        }
                    }

                    if (folders.isEmpty()) {
                        SegmentedListItem(
                            onClick = onAddFolder,
                            shapes = expressiveSegmentedShapes(index = 0, count = 1),
                            content = {
                                Text(
                                    text = stringResource(R.string.no_scan_folders_settings),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                        ) {
                            folders.forEachIndexed { index, folder ->
                                SegmentedListItem(
                                    onClick = {},
                                    shapes = expressiveSegmentedShapes(index = index, count = folders.size),
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier.fillMaxHeight(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    content = { Text(folder.displayName) },
                                    supportingContent = {
                                        Text(
                                            text = folder.uri.toString(),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    trailingContent = {
                                        Box(
                                            modifier = Modifier.fillMaxHeight(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconButton(onClick = { onRemoveFolder(folder.uri) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(
                                                        R.string.remove_folder,
                                                        folder.displayName
                                                    ),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
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
            }

            // Info Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilionSectionHeader(text = stringResource(R.string.section_info))
                    SegmentedListItem(
                        onClick = onOpenAbout,
                        shapes = expressiveSegmentedShapes(index = 0, count = 1),
                        leadingContent = {
                            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        content = { Text(stringResource(R.string.about_title)) },
                        supportingContent = { Text(stringResource(R.string.about_description)) },
                        trailingContent = {
                            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null
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
}
