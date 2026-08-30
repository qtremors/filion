@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package dev.qtremors.filion.about

import android.content.ClipData
import android.os.Build
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.qtremors.filion.BuildConfig
import dev.qtremors.filion.R
import dev.qtremors.filion.theme.bounceClickable
import dev.qtremors.filion.theme.expressiveSegmentedShapes
import dev.qtremors.filion.theme.spacing
import dev.qtremors.filion.ui.FilionScreenScaffold
import dev.qtremors.filion.ui.FilionSectionHeader
import dev.qtremors.filion.ui.showFilionToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    onOpenLicenses: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val copyToClipboard = { text: String ->
        coroutineScope.launch {
            clipboard.setClipEntry(
                ClipEntry(
                    ClipData.newPlainText(context.getString(R.string.app_name), text)
                )
            )
            context.showFilionToast(context.getString(R.string.copied_to_clipboard))
        }
    }

    FilionScreenScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.about_title),
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
            // App Logo
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_filion_logo_color),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(96.dp)
                    )
                }
            }

            // App Info Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilionSectionHeader(text = stringResource(R.string.section_app_info))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        SegmentedListItem(
                            onClick = { copyToClipboard("Filion v${BuildConfig.VERSION_NAME}") },
                            shapes = expressiveSegmentedShapes(index = 0, count = 5),
                            content = { Text(stringResource(R.string.version)) },
                            supportingContent = { Text(BuildConfig.VERSION_NAME) },
                            leadingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                        SegmentedListItem(
                            onClick = { uriHandler.openUri(context.getString(R.string.developer_url)) },
                            shapes = expressiveSegmentedShapes(index = 1, count = 5),
                            content = { Text(stringResource(R.string.developer)) },
                            supportingContent = { Text(stringResource(R.string.developer_name)) },
                            leadingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            trailingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                        SegmentedListItem(
                            onClick = { uriHandler.openUri(context.getString(R.string.website_url)) },
                            shapes = expressiveSegmentedShapes(index = 2, count = 5),
                            content = { Text(stringResource(R.string.website)) },
                            supportingContent = { Text(stringResource(R.string.website_display)) },
                            leadingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            trailingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                        SegmentedListItem(
                            onClick = { uriHandler.openUri(context.getString(R.string.repository_full_url)) },
                            shapes = expressiveSegmentedShapes(index = 3, count = 5),
                            content = { Text(stringResource(R.string.repository)) },
                            supportingContent = { Text(stringResource(R.string.repository_url)) },
                            leadingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Source, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            trailingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                        SegmentedListItem(
                            onClick = {
                                copyToClipboard("${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
                            },
                            shapes = expressiveSegmentedShapes(index = 4, count = 5),
                            content = { Text(stringResource(R.string.device)) },
                            supportingContent = { Text("${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}") },
                            leadingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                    }
                }
            }

            // Privacy & License Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilionSectionHeader(text = stringResource(R.string.section_privacy))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        SegmentedListItem(
                            onClick = { uriHandler.openUri(context.getString(R.string.privacy_policy_url)) },
                            shapes = expressiveSegmentedShapes(index = 0, count = 2),
                            content = { Text(stringResource(R.string.privacy_policy)) },
                            supportingContent = { Text(stringResource(R.string.privacy_summary)) },
                            leadingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            trailingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                        SegmentedListItem(
                            onClick = { uriHandler.openUri(context.getString(R.string.project_license_url)) },
                            shapes = expressiveSegmentedShapes(index = 1, count = 2),
                            content = { Text(stringResource(R.string.project_license)) },
                            supportingContent = { Text(stringResource(R.string.mit_license)) },
                            leadingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            trailingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                    }
                }
            }

            // Changelog & Support Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilionSectionHeader(text = stringResource(R.string.section_changelogs))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        SegmentedListItem(
                            onClick = { uriHandler.openUri(context.getString(R.string.view_releases_url)) },
                            shapes = expressiveSegmentedShapes(index = 0, count = 3),
                            content = { Text(stringResource(R.string.view_releases)) },
                            supportingContent = { Text(stringResource(R.string.view_releases_description)) },
                            leadingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            trailingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                        SegmentedListItem(
                            onClick = { uriHandler.openUri(context.getString(R.string.report_issue_url)) },
                            shapes = expressiveSegmentedShapes(index = 1, count = 3),
                            content = { Text(stringResource(R.string.report_issue)) },
                            supportingContent = { Text(stringResource(R.string.report_issue_description)) },
                            leadingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            trailingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                        SegmentedListItem(
                            onClick = onOpenLicenses,
                            shapes = expressiveSegmentedShapes(index = 2, count = 3),
                            content = { Text(stringResource(R.string.open_source_licenses)) },
                            supportingContent = { Text(stringResource(R.string.open_source_licenses_description)) },
                            leadingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            trailingContent = {
                                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                                }
                            },
                            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                    }
                }
            }
        }
    }
}
