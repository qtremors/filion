package dev.qtremors.filion.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ViewerToolbarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val tint: Color? = null,
    val containerColor: Color? = null,
    val onClick: () -> Unit
)

@Composable
fun ViewerSplitButtonGroup(
    actions: List<ViewerToolbarAction>,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    height: Dp = 48.dp,
    minWidth: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    trailingContent: (@Composable () -> Unit)? = null,
    actionModifier: @Composable (Modifier, () -> Unit) -> Modifier = { base, onClick ->
        base.clickable(onClick = onClick)
    }
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        actions.forEachIndexed { index, action ->
            val shape = when {
                actions.size == 1 && trailingContent == null -> CircleShape
                index == 0 -> RoundedCornerShape(50, 15, 15, 50)
                index == actions.lastIndex && trailingContent == null -> RoundedCornerShape(15, 50, 50, 15)
                else -> RoundedCornerShape(15)
            }
            Surface(
                shape = shape,
                color = action.containerColor ?: containerColor,
                contentColor = action.tint ?: contentColor,
                modifier = actionModifier(
                    Modifier.height(height).widthIn(min = minWidth).clip(shape),
                    action.onClick
                )
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Icon(action.icon, action.contentDescription, modifier = Modifier.widthIn(max = iconSize))
                }
            }
        }
        if (trailingContent != null) {
            Box(modifier = Modifier.height(height).widthIn(min = minWidth)) {
                trailingContent()
            }
        }
    }
}
