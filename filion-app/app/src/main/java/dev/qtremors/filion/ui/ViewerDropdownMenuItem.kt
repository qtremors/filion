package dev.qtremors.filion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun ViewerDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    shape: Shape = MaterialTheme.shapes.medium
) {
    DropdownMenuItem(
        text = text,
        onClick = onClick,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        modifier = modifier.clip(shape)
    )
}

val androidx.compose.material3.Shapes.viewerMenuFirst: Shape
    get() = RoundedCornerShape(20.dp, 20.dp, 6.dp, 6.dp)
val androidx.compose.material3.Shapes.viewerMenuMiddle: Shape
    get() = RoundedCornerShape(6.dp)
val androidx.compose.material3.Shapes.viewerMenuLast: Shape
    get() = RoundedCornerShape(6.dp, 6.dp, 20.dp, 20.dp)
val androidx.compose.material3.Shapes.viewerMenuSingle: Shape
    get() = RoundedCornerShape(20.dp)
