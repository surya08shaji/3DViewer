package com.example.modelviewer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.modelviewer.ui.components.ModelContainer
import com.example.modelviewer.ui.theme.*
import com.example.modelviewer.viewmodel.ModelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ModelViewModel,
    engine: com.google.android.filament.Engine,
    modelLoader: io.github.sceneview.loaders.ModelLoader,
    modifier: Modifier = Modifier
) {
    var isAddDialogVisible by remember { mutableStateOf(false) }
    
    // Highly-optimized Z-Index sorted models list using derivedStateOf
    // This guarantees recomposition ONLY when list items are added/removed or zIndex properties change!
    val sortedModels by remember {
        derivedStateOf {
            viewModel.models.sortedBy { it.zIndex }
        }
    }
    
    // Identifies which container is currently on top (has the maximum zIndex)
    val topMostModelId by remember {
        derivedStateOf {
            viewModel.models.maxByOrNull { it.zIndex }?.id
        }
    }

    // List of static models ready to be loaded from assets
    val availableModels = listOf(
        Pair("damaged_helmet.glb", "Cyber Sci-Fi Helmet"),
        Pair("boombox.glb", "Retro Classic Boombox"),
        Pair("avocado.glb", "Organic Texture Avocado"),
        Pair("duck.glb", "Vibrant Yellow Duck")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF140D36),
                        DeepBlackSpace
                    ),
                    center = Offset(x = 500f, y = 800f),
                    radius = 1200f
                )
            )
    ) {
        // Futuristic Dot Grid Overlay that fades in and out based on Grid Snapping status
        AnimatedVisibility(
            visible = viewModel.isSnapToGrid.value,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(400)),
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridPx = viewModel.gridSize
                val dotColor = NeonCyan.copy(alpha = 0.15f)
                
                // Draw horizontal dots
                var y = 0f
                while (y < size.height) {
                    var x = 0f
                    while (x < size.width) {
                        drawCircle(
                            color = dotColor,
                            radius = 2f,
                            center = Offset(x, y)
                        )
                        x += gridPx
                    }
                    y += gridPx
                }
            }
        }

        // The Render Canvas - Dynamic List of movable containers
        sortedModels.forEach { item ->
            // Use Compose key to maintain identity, preventing GL recreation during rearrangements
            key(item.id) {
                ModelContainer(
                    modelItem = item,
                    engine = engine,
                    modelLoader = modelLoader,
                    isTopMost = item.id == topMostModelId,
                    onPositionChange = { delta -> viewModel.updatePosition(item.id, delta) },
                    onScaleChange = { mult -> viewModel.updateScale(item.id, mult) },
                    onRotationChange = { rot -> viewModel.updateRotation(item.id, rot) },
                    onModelScaleChange = { ms -> viewModel.updateModelScale(item.id, ms) },
                    onToggleMode = { viewModel.toggleInteractionMode(item.id) },
                    onClose = { viewModel.removeModel(item.id) },
                    onTap = { viewModel.bringToFront(item.id) }
                )
            }
        }

        // Top Glass Panel - HUD Brand & Spawning Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3D VIEWER",
                color = Color.White,
                fontSize = 18.sp,
                style = MaterialTheme.typography.titleMedium
            )
         /*   Column {

                Text(
                    text = "Engine: Shared Filament C++",
                    color = NeonCyan.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }*/

            // Glass FAB to summon the Spawn Dialog
            Button(
                onClick = { isAddDialogVisible = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlassColor.copy(alpha = 0.6f),
                    contentColor = NeonCyan
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(NeonCyan, Color.White.copy(alpha = 0.1f))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Model Icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ADD MODEL",
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Bottom Sleek Glass Controller Dashboard
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(GlassColor.copy(alpha = 0.6f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats HUD Tracker
            Column {
                Text(
                    text = "ACTIVE VIEWPORTS",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 8.sp,
                    style = MaterialTheme.typography.labelSmall
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${viewModel.models.size}",
                        color = NeonCyan,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = " / 5 limit",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Dashboard Quick Action Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Snap to Grid toggler
                IconButton(
                    onClick = { viewModel.toggleSnapToGrid() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (viewModel.isSnapToGrid.value) NeonCyan.copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (viewModel.isSnapToGrid.value) NeonCyan.copy(alpha = 0.7f)
                            else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Toggle Grid Snapping",
                        tint = if (viewModel.isSnapToGrid.value) NeonCyan else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Clear Workspace button
                IconButton(
                    onClick = { viewModel.clearAll() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(
                            width = 1.dp,
                            color = DangerRed.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Canvas",
                        tint = DangerRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Futuristic Holographic Spawning Dialog
        if (isAddDialogVisible) {
            Dialog(
                onDismissRequest = { isAddDialogVisible = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(GlassColor.copy(alpha = 0.95f))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(NeonPurple, NeonCyan)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "SPAWN VIEWPORT",
                            color = Color.White,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Inject a new 3D instance into the rendering thread",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableModels) { (path, name) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .clickable {
                                            viewModel.addModel(path, name)
                                            isAddDialogVisible = false
                                        }
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = name,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 13.sp,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Asset: $path",
                                            color = NeonCyan.copy(alpha = 0.6f),
                                            fontSize = 9.sp,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NeonCyan.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "SPAWN",
                                            color = NeonCyan,
                                            fontSize = 10.sp,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = { isAddDialogVisible = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "CANCEL",
                                color = DangerRed.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
