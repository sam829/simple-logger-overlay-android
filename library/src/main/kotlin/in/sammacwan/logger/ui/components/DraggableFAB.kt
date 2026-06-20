package in.sammacwan.logger.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import in.sammacwan.logger.LoggerOverlay
import kotlin.math.roundToInt

@Composable
fun DraggableFAB(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hasNewLogs by LoggerOverlay.hasUnreadLogs.collectAsState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // Calculate screen bounds in pixels
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val fabSizePx = with(density) { 56.dp.toPx() } // Standard FAB size
    val paddingPx = with(density) { 16.dp.toPx() }
    
    // FAB position state (center-right by default)
    var offsetX by remember { 
        mutableFloatStateOf(screenWidthPx - fabSizePx - paddingPx) 
    }
    var offsetY by remember { 
        mutableFloatStateOf((screenHeightPx - fabSizePx) / 2) 
    }
    var isDragging by remember { mutableStateOf(false) }
    
    // Pulse animation on new log
    val pulseScale by animateFloatAsState(
        targetValue = if (hasNewLogs && !isDragging) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "FAB pulse"
    )
    
    // Shrink animation while dragging
    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "FAB drag scale"
    )
    
    FloatingActionButton(
        onClick = { LoggerOverlay.show(context) },
        modifier = modifier
            .offset {
                IntOffset(
                    offsetX.roundToInt(),
                    offsetY.roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        
                        // Update position
                        offsetX = (offsetX + dragAmount.x).coerceIn(
                            0f,
                            screenWidthPx - fabSizePx
                        )
                        offsetY = (offsetY + dragAmount.y).coerceIn(
                            0f,
                            screenHeightPx - fabSizePx
                        )
                    },
                    onDragEnd = {
                        isDragging = false
                        
                        // Snap to nearest edge (left or right)
                        val midX = screenWidthPx / 2
                        offsetX = if (offsetX < midX) {
                            paddingPx // Snap to left
                        } else {
                            screenWidthPx - fabSizePx - paddingPx // Snap to right
                        }
                        
                        // Constrain Y within bounds
                        offsetY = offsetY.coerceIn(
                            paddingPx,
                            screenHeightPx - fabSizePx - paddingPx
                        )
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            }
            .scale(pulseScale * dragScale)
    ) {
        Icon(
            imageVector = Icons.Default.BugReport,
            contentDescription = "Open Logger Overlay"
        )
    }
}
