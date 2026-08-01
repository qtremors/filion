package dev.qtremors.filion.about

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.qtremors.filion.BuildConfig
import dev.qtremors.filion.R

private const val DEVELOPER_URL = "https://github.com/qtremors"
private const val WEBSITE_URL = "https://qtremors.github.io/filion/"
private const val REPOSITORY_URL = "https://github.com/qtremors/filion"
private const val PRIVACY_URL = "$REPOSITORY_URL/blob/main/PRIVACY.md"
private const val LICENSE_URL = "$REPOSITORY_URL/blob/main/LICENSE.md"
private const val RELEASES_URL = "$REPOSITORY_URL/releases"
private const val ISSUES_URL = "$REPOSITORY_URL/issues/new"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onOpenLicenses: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Image(
                    painter = painterResource(R.drawable.ic_filion_logo_color),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(112.dp)
                )
            }
            item {
                AboutSection(stringResource(R.string.about_app_info)) {
                    AboutItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.version),
                        supporting = BuildConfig.VERSION_NAME
                    )
                    HorizontalDivider()
                    AboutItem(
                        icon = Icons.Default.Code,
                        title = stringResource(R.string.developer),
                        supporting = stringResource(R.string.developer_name),
                        external = true,
                        onClick = { uriHandler.openUri(DEVELOPER_URL) }
                    )
                    HorizontalDivider()
                    AboutItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.website),
                        supporting = "qtremors.github.io/filion",
                        external = true,
                        onClick = { uriHandler.openUri(WEBSITE_URL) }
                    )
                    HorizontalDivider()
                    AboutItem(
                        icon = Icons.Default.Source,
                        title = stringResource(R.string.repository),
                        supporting = "github.com/qtremors/filion",
                        external = true,
                        onClick = { uriHandler.openUri(REPOSITORY_URL) }
                    )
                    HorizontalDivider()
                    AboutItem(
                        icon = Icons.Default.PhoneAndroid,
                        title = stringResource(R.string.device),
                        supporting = "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}"
                    )
                }
            }
            item {
                AboutSection(stringResource(R.string.about_privacy_license)) {
                    AboutItem(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.privacy_policy),
                        supporting = stringResource(R.string.privacy_summary),
                        external = true,
                        onClick = { uriHandler.openUri(PRIVACY_URL) }
                    )
                    HorizontalDivider()
                    AboutItem(
                        icon = Icons.Default.Description,
                        title = stringResource(R.string.project_license),
                        supporting = stringResource(R.string.mit_license),
                        external = true,
                        onClick = { uriHandler.openUri(LICENSE_URL) }
                    )
                    HorizontalDivider()
                    AboutItem(
                        icon = Icons.Default.Description,
                        title = stringResource(R.string.open_source_licenses),
                        supporting = stringResource(R.string.open_source_licenses_description),
                        onClick = onOpenLicenses
                    )
                }
            }
            item {
                AboutSection(stringResource(R.string.about_support)) {
                    AboutItem(
                        icon = Icons.Default.History,
                        title = stringResource(R.string.view_releases),
                        supporting = stringResource(R.string.view_releases_description),
                        external = true,
                        onClick = { uriHandler.openUri(RELEASES_URL) }
                    )
                    HorizontalDivider()
                    AboutItem(
                        icon = Icons.Default.BugReport,
                        title = stringResource(R.string.report_issue),
                        supporting = stringResource(R.string.report_issue_description),
                        external = true,
                        onClick = { uriHandler.openUri(ISSUES_URL) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column { content() }
        }
    }
}

@Composable
private fun AboutItem(
    icon: ImageVector,
    title: String,
    supporting: String,
    external: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(supporting) },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = if (onClick != null) {
            {
                Icon(
                    if (external) Icons.AutoMirrored.Filled.OpenInNew else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        } else {
            null
        },
        modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    )
}
