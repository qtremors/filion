package dev.qtremors.filion.settings

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.qtremors.filion.FolderItem
import dev.qtremors.filion.R

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SettingsSection(title = stringResource(R.string.settings_appearance)) {
                    Column {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            val label = when (mode) {
                                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                ThemeMode.DARK -> stringResource(R.string.theme_dark)
                            }
                            ListItem(
                                headlineContent = { Text(label) },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    RadioButton(
                                        selected = themeMode == mode,
                                        onClick = { onThemeModeChange(mode) }
                                    )
                                },
                                modifier = Modifier.clickable { onThemeModeChange(mode) }
                            )
                            if (index < ThemeMode.entries.lastIndex) HorizontalDivider()
                        }
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.dynamic_color)) },
                            supportingContent = {
                                Text(
                                    if (dynamicColorAvailable) {
                                        stringResource(R.string.dynamic_color_description)
                                    } else {
                                        stringResource(R.string.dynamic_color_unavailable)
                                    }
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = dynamicColor && dynamicColorAvailable,
                                    enabled = dynamicColorAvailable,
                                    onCheckedChange = onDynamicColorChange
                                )
                            },
                            modifier = if (dynamicColorAvailable) {
                                Modifier.clickable { onDynamicColorChange(!dynamicColor) }
                            } else {
                                Modifier
                            }
                        )
                    }
                }
            }

            item {
                SettingsSection(
                    title = stringResource(R.string.scan_folders),
                    action = {
                        TextButton(onClick = onAddFolder) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(stringResource(R.string.add_folder))
                        }
                    }
                ) {
                    if (folders.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_scan_folders_settings),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Column {
                            folders.forEachIndexed { index, folder ->
                                ListItem(
                                    headlineContent = { Text(folder.displayName) },
                                    supportingContent = { Text(folder.uri.toString(), maxLines = 1) },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { onRemoveFolder(folder.uri) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(
                                                    R.string.remove_folder,
                                                    folder.displayName
                                                ),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                )
                                if (index < folders.lastIndex) HorizontalDivider()
                            }
                        }
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_info)) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.about_title)) },
                        supportingContent = { Text(stringResource(R.string.about_description)) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable(onClick = onOpenAbout)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    action: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            action?.invoke()
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}
