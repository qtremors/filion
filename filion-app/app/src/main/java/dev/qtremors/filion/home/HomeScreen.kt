@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package dev.qtremors.filion.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.qtremors.filion.FolderItem
import dev.qtremors.filion.ModelTarget
import dev.qtremors.filion.R
import dev.qtremors.filion.theme.bounceClickable
import dev.qtremors.filion.theme.expressiveSegmentedShapes
import dev.qtremors.filion.theme.spacing
import dev.qtremors.filion.ui.EmptyState
import dev.qtremors.filion.ui.FilionFastScrollbar
import dev.qtremors.filion.ui.FilionScreenScaffold
import dev.qtremors.filion.ui.FilionSectionHeader
import dev.qtremors.filion.ui.LazyListScrollbarState
import dev.qtremors.filion.ui.formatViewerFileSize

@Composable
fun HomeScreen(
    localModels: List<ModelTarget>,
    folders: List<FolderItem>,
    recentModels: List<ModelTarget>,
    onSelectFile: () -> Unit,
    onSelectLocalModel: (ModelTarget) -> Unit,
    onAddFolder: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    onClearRecentModels: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedFolderUri by rememberSaveable { mutableStateOf<String?>(null) }

    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val scrollbarState = remember(listState) { LazyListScrollbarState(listState) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pullRefreshState = rememberPullToRefreshState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val selectedFolderItem = remember(folders, selectedFolderUri) {
        folders.firstOrNull { it.uri.toString() == selectedFolderUri }
    }

    val folderFilteredModels = remember(localModels, selectedFolderItem) {
        if (selectedFolderItem == null) {
            localModels
        } else {
            localModels.filter { model ->
                model.folderName.equals(selectedFolderItem.displayName, ignoreCase = true)
            }
        }
    }

    val displayedModels = remember(folderFilteredModels, searchQuery) {
        if (searchQuery.isBlank()) {
            folderFilteredModels
        } else {
            folderFilteredModels.filter {
                it.displayName.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.folderName.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    FilionScreenScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    stringResource(R.string.search_placeholder),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .bounceClickable {
                                    isSearchActive = false
                                    searchQuery = ""
                                }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier
                                .clip(CircleShape)
                                .bounceClickable { searchQuery = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.clear_search)
                                )
                            }
                        }
                    }
                )
            } else {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    actions = {
                        if (localModels.isNotEmpty()) {
                            IconButton(
                                onClick = { isSearchActive = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .bounceClickable { isSearchActive = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search_models)
                                )
                            }
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .clip(CircleShape)
                                .bounceClickable(onClick = onRefresh)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.refresh_models)
                            )
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .clip(CircleShape)
                                .bounceClickable(onClick = onOpenSettings)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.open_settings)
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    onRefresh()
                    isRefreshing = false
                },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                            MaterialTheme.spacing.screenGutter
                    ),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    // Hero Action Card (only visible when not actively searching)
                    if (!isSearchActive) {
                        item(key = "hero_action_card") {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .bounceClickable(onClick = onSelectFile)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.FolderOpen,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.open_model),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(R.string.open_model_description),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Horizontal Folders Row
                        if (folders.isNotEmpty() || localModels.isNotEmpty()) {
                            item(key = "folders_section") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp, bottom = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilionSectionHeader(text = stringResource(R.string.section_folders))
                                        TextButton(
                                            onClick = onAddFolder,
                                            modifier = Modifier.bounceClickable(onClick = onAddFolder)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(stringResource(R.string.add_folder))
                                        }
                                    }

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(vertical = 2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        item(key = "folder_all") {
                                            val isAllSelected = selectedFolderUri == null
                                            FilterChip(
                                                selected = isAllSelected,
                                                onClick = { selectedFolderUri = null },
                                                label = {
                                                    Text(
                                                        stringResource(R.string.all_folders),
                                                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.FolderOpen,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                },
                                                trailingIcon = if (localModels.isNotEmpty()) {
                                                    {
                                                        Text(
                                                            text = localModels.size.toString(),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                } else null,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }

                                        items(folders, key = { it.uri.toString() }) { folder ->
                                            val isFolderSelected = selectedFolderUri == folder.uri.toString()
                                            val folderModelCount = localModels.count {
                                                it.folderName.equals(folder.displayName, ignoreCase = true)
                                            }
                                            FilterChip(
                                                selected = isFolderSelected,
                                                onClick = {
                                                    selectedFolderUri = if (isFolderSelected) null else folder.uri.toString()
                                                },
                                                label = {
                                                    Text(
                                                        text = folder.displayName,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        fontWeight = if (isFolderSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Folder,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                },
                                                trailingIcon = {
                                                    Text(
                                                        text = folderModelCount.toString(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Recently Opened Shelf
                        if (recentModels.isNotEmpty()) {
                            item(key = "recently_opened_section") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp, bottom = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilionSectionHeader(text = stringResource(R.string.recently_opened))
                                        TextButton(
                                            onClick = onClearRecentModels,
                                            modifier = Modifier.bounceClickable(onClick = onClearRecentModels)
                                        ) {
                                            Text(stringResource(R.string.clear_recents))
                                        }
                                    }

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(vertical = 2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(recentModels, key = { "recent_${it.canonicalKey}" }) { recent ->
                                            Card(
                                                shape = RoundedCornerShape(18.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                                ),
                                                modifier = Modifier
                                                    .width(180.dp)
                                                    .bounceClickable { onSelectLocalModel(recent) }
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(14.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        modifier = Modifier.size(40.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Default.ViewInAr,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                modifier = Modifier.size(22.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Text(
                                                        text = recent.displayName,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = if (recent.folderName.isNotBlank()) {
                                                            "${recent.folderName} • ${formatViewerFileSize(recent.sizeBytes)}"
                                                        } else {
                                                            formatViewerFileSize(recent.sizeBytes)
                                                        },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Header row for Models (only show when there are models or folder/search filter is active)
                    val showHeader = if (isSearchActive && searchQuery.isNotBlank()) {
                        true
                    } else if (selectedFolderItem != null) {
                        true
                    } else {
                        displayedModels.isNotEmpty()
                    }

                    if (showHeader) {
                        item(key = "models_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isSearchActive && searchQuery.isNotBlank()) {
                                            "${displayedModels.size} matching"
                                        } else if (selectedFolderItem != null) {
                                            stringResource(R.string.models_in_folder, selectedFolderItem.displayName)
                                        } else {
                                            stringResource(R.string.models_discovered)
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (!isSearchActive && displayedModels.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                                        ) {
                                            Text(
                                                text = displayedModels.size.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (!isSearchActive && selectedFolderItem != null) {
                                    TextButton(
                                        onClick = { selectedFolderUri = null },
                                        modifier = Modifier.bounceClickable { selectedFolderUri = null }
                                    ) {
                                        Text(stringResource(R.string.show_all))
                                    }
                                } else if (!isSearchActive && folders.isEmpty()) {
                                    TextButton(
                                        onClick = onAddFolder,
                                        modifier = Modifier.bounceClickable(onClick = onAddFolder)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.add_folder))
                                    }
                                }
                            }
                        }
                    }

                    // Models List / Empty State
                    if (displayedModels.isEmpty()) {
                        item(key = "empty_state") {
                            if (isSearchActive && searchQuery.isNotBlank()) {
                                EmptyState(
                                    icon = Icons.Default.SearchOff,
                                    title = stringResource(R.string.no_search_results),
                                    description = stringResource(R.string.no_search_results_description)
                                )
                            } else if (selectedFolderItem != null) {
                                EmptyState(
                                    icon = Icons.Default.FolderOpen,
                                    title = stringResource(R.string.no_models_in_folder),
                                    description = stringResource(R.string.no_models_in_folder_description),
                                    actionLabel = stringResource(R.string.show_all),
                                    onAction = { selectedFolderUri = null }
                                )
                            } else {
                                EmptyState(
                                    icon = Icons.Default.ViewInAr,
                                    title = stringResource(R.string.no_models_found),
                                    description = stringResource(R.string.no_models_description),
                                    actionLabel = stringResource(R.string.open_model),
                                    onAction = onSelectFile,
                                    secondaryActionLabel = stringResource(R.string.add_scan_folder),
                                    onSecondaryAction = onAddFolder
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = displayedModels,
                            key = { _, model -> model.canonicalKey }
                        ) { index, model ->
                            val folderBadge = if (model.folderName.isNotBlank()) "${model.folderName} • " else ""
                            SegmentedListItem(
                                onClick = { onSelectLocalModel(model) },
                                shapes = expressiveSegmentedShapes(index = index, count = displayedModels.size),
                                leadingContent = {
                                    Box(
                                        modifier = Modifier.fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.ViewInAr,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                content = {
                                    Text(
                                        text = model.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = "$folderBadge${formatViewerFileSize(model.sizeBytes)} • 3D Model",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingContent = {
                                    Box(
                                        modifier = Modifier.fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(20.dp)
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

            if (displayedModels.size > 8) {
                FilionFastScrollbar(
                    scrollbarState = scrollbarState,
                    labelForIndex = { itemIndex ->
                        val safeIndex = itemIndex.coerceIn(0, displayedModels.lastIndex)
                        if (displayedModels.isNotEmpty()) {
                            val firstChar = displayedModels[safeIndex].displayName.trim().firstOrNull()?.uppercaseChar()
                            if (firstChar != null && (firstChar.isLetter() || firstChar.isDigit())) {
                                firstChar.toString()
                            } else {
                                "#"
                            }
                        } else {
                            ""
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp
                        )
                )
            }
        }
    }
}

