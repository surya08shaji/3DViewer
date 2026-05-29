package com.example.modelviewer.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modelviewer.model.ModelItem
import com.example.modelviewer.ui.theme.*
import io.github.sceneview.SceneView
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberModelInstance

/**
 * A beautiful, hardware-optimized Composable that represents an independent 3D Model Container.
 * Renders the Filament Scene using the shared engine/loader caches and handles multi-touch gestures.
 */
@Composable
fun ModelContainer(
    modelItem: ModelItem,
    engine: com.google.android.filament.Engine,
    modelLoader: io.github.sceneview.loaders.ModelLoader,
    isTopMost: Boolean,
    onPositionChange: (Offset) -> Unit,
    onScaleChange: (Float) -> Unit,
    onRotationChange: (Float) -> Unit,
    onModelScaleChange: (Float) -> Unit,
    onToggleMode: () -> Unit,
    onClose: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic dimensions with base dimensions scaled by container scale
    val baseWidth = 230.dp
    val baseHeight = 230.dp
    
    val currentWidth = baseWidth * modelItem.scale
    val currentHeight = baseHeight * modelItem.scale

    // Bring-to-front instantly on press detector
    val pressGestureModifier = Modifier.pointerInput(modelItem.id) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Press) {
                    onTap()
                }
            }
        }
    }

    // Touch event interceptor to separate Normal Mode (drag container) from Interaction Mode (rotate model)
    val gestureModifier = Modifier.pointerInput(modelItem.id, modelItem.isInteractionMode) {
        detectTransformGestures { _, pan, zoom, _ ->
            if (modelItem.isInteractionMode) {
                // INTERACTION MODE: Zoom model on pinch, Rotate model on horizontal drag
                if (zoom != 1f) {
                    onModelScaleChange(zoom)
                } else if (pan.x != 0f) {
                    // Pan X maps directly to Y-axis model rotation (sensitivity adjusted)
                    onRotationChange(pan.x * 0.4f)
                }
            } else {
                // NORMAL MODE: Scale container on pinch, Drag container on pan
                if (zoom != 1f) {
                    onScaleChange(zoom)
                } else if (pan != Offset.Zero) {
                    onPositionChange(pan)
                }
            }
        }
    }

    // Smooth glow animations for active states
    val borderGlowColor = if (modelItem.isInteractionMode) GlowGreen else NeonCyan
    val borderStrokeWidth by animateDpAsState(targetValue = if (isTopMost) 2.dp else 1.dp, label = "BorderWidth")
    val borderOpacity by animateFloatAsState(targetValue = if (isTopMost) 1.0f else 0.4f, label = "BorderOpacity")

    Box(
        modifier = modifier
            .offset { IntOffset(modelItem.position.x.toInt(), modelItem.position.y.toInt()) }
            .size(width = currentWidth, height = currentHeight)
            .then(pressGestureModifier)
            .then(gestureModifier)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassColor.copy(alpha = 0.5f),
                        GlassColor.copy(alpha = 0.25f)
                    )
                )
            )
            .border(
                width = borderStrokeWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderGlowColor.copy(alpha = borderOpacity),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Asynchronous model compilation
        val modelInstance = rememberModelInstance(modelLoader, modelItem.modelPath)

        if (modelInstance == null) {
            // Elegant glowing glass loading indicator
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = NeonCyan,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "COMPILING...",
                        color = NeonCyan.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        } else {
            // Shared high-performance Filament SceneView
            SceneView(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                isOpaque = true
            ) {
                ModelNode(
                    modelInstance = modelInstance,
                    rotation = Rotation(0f, modelItem.rotation, 0f),
                    scale = Scale(modelItem.modelScale),
                    scaleToUnits = 1.0f
                )
            }
        }

        // Top control overlay bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sleek mode toggle button (Interact vs Move)
            IconButton(
                onClick = onToggleMode,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (modelItem.isInteractionMode) GlowGreen.copy(alpha = 0.25f)
                        else Color.White.copy(alpha = 0.08f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (modelItem.isInteractionMode) GlowGreen.copy(alpha = 0.6f)
                        else Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (modelItem.isInteractionMode) Icons.Default.TouchApp else Icons.Default.OpenWith,
                    contentDescription = "Toggle Interaction Mode",
                    tint = if (modelItem.isInteractionMode) GlowGreen else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Sleek close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Model Container",
                    tint = DangerRed.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Bottom label displaying the model name and status
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (modelItem.isInteractionMode) GlowGreen else NeonCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = modelItem.modelName,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
