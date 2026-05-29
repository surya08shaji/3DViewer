package com.example.modelviewer.model

import androidx.compose.ui.geometry.Offset

/**
 * Represents the state of a single active 3D model inside its container.
 * Leverages Kotlin default parameters to strictly satisfy the required properties
 * while supporting advanced features like Z-index ordering and internal model scaling.
 */
data class ModelItem(
    val id: String,
    val modelPath: String,
    val position: Offset,
    val scale: Float,               // Container scale factor (Normal Mode)
    val rotation: Float,            // Model rotation in degrees (Y-axis) (Interaction Mode)
    val isInteractionMode: Boolean, // Switch between container controls and direct model gestures
    val modelName: String = "",     // User-friendly display name of the model
    val zIndex: Float = 0f,         // Visual stack order for overlapping containers
    val modelScale: Float = 1.0f    // 3D model zoom factor inside the viewport (Interaction Mode)
)
