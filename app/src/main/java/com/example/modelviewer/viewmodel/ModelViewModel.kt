package com.example.modelviewer.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.example.modelviewer.model.ModelItem
import java.util.UUID

/**
 * Lightweight MVVM ViewModel managing the collections of 3D models and their positioning.
 * Provides thread-safe list operations and precise spatial transformations.
 */
class ModelViewModel : ViewModel() {
    
    // Thread-safe observable list of active model items in the current canvas session
    val models = mutableStateListOf<ModelItem>()
    
    // Tracks whether snapping to a spatial grid is active (optional bonus feature)
    val isSnapToGrid = mutableStateOf(false)
    val gridSize = 50f // Snap resolution in density-independent pixels
    
    private var currentMaxZIndex = 0f

    /**
     * Spawns a new 3D model container on the canvas with progressive offset cascades.
     */
    fun addModel(modelPath: String, modelName: String) {
        currentMaxZIndex += 1f
        // Offset each new model layout slightly to avoid perfect overlap cascades
        val offsetFactor = models.size * 40f
        val newItem = ModelItem(
            id = UUID.randomUUID().toString(),
            modelPath = modelPath,
            modelName = modelName,
            position = Offset(150f + offsetFactor, 150f + offsetFactor),
            scale = 1.0f,
            rotation = 0.0f,
            isInteractionMode = false,
            zIndex = currentMaxZIndex,
            modelScale = 1.0f
        )
        models.add(newItem)
    }

    /**
     * Disposes the selected model state. Clean resources will be handled at the Composable level.
     */
    fun removeModel(id: String) {
        models.removeAll { it.id == id }
    }

    /**
     * Updates the container's layout coordinates, supporting optional grid snapping.
     */
    fun updatePosition(id: String, delta: Offset) {
        val index = models.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = models[index]
            var newPos = item.position + delta
            
            if (isSnapToGrid.value) {
                val snappedX = Math.round(newPos.x / gridSize) * gridSize
                val snappedY = Math.round(newPos.y / gridSize) * gridSize
                newPos = Offset(snappedX.toFloat(), snappedY.toFloat())
            }
            
            models[index] = item.copy(position = newPos)
        }
    }

    /**
     * Resizes the layout container (Normal Mode pinching). Bounded safely to prevent micro/macro overflow.
     */
    fun updateScale(id: String, multiplier: Float) {
        val index = models.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = models[index]
            val newScale = (item.scale * multiplier).coerceIn(0.4f, 3.0f)
            models[index] = item.copy(scale = newScale)
        }
    }

    /**
     * Rotates the 3D model (Interaction Mode dragging).
     */
    fun updateRotation(id: String, deltaRotation: Float) {
        val index = models.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = models[index]
            var newRotation = (item.rotation + deltaRotation) % 360f
            if (newRotation < 0f) newRotation += 360f
            models[index] = item.copy(rotation = newRotation)
        }
    }

    /**
     * Zooms the 3D model inside its viewport (Interaction Mode pinching).
     */
    fun updateModelScale(id: String, multiplier: Float) {
        val index = models.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = models[index]
            val newModelScale = (item.modelScale * multiplier).coerceIn(0.3f, 4.0f)
            models[index] = item.copy(modelScale = newModelScale)
        }
    }

    /**
     * Toggles whether user gestures affect the container (move/resize) or the model (rotate/zoom).
     */
    fun toggleInteractionMode(id: String) {
        val index = models.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = models[index]
            models[index] = item.copy(isInteractionMode = !item.isInteractionMode)
        }
    }

    /**
     * Promotes the tapped container's visual stack order to the absolute top of the viewport.
     */
    fun bringToFront(id: String) {
        val index = models.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = models[index]
            if (item.zIndex < currentMaxZIndex || models.size == 1) {
                currentMaxZIndex += 1f
                models[index] = item.copy(zIndex = currentMaxZIndex)
            }
        }
    }
    
    /**
     * Toggles grid alignment overlay.
     */
    fun toggleSnapToGrid() {
        isSnapToGrid.value = !isSnapToGrid.value
    }
    
    /**
     * Resets the entire interactive canvas.
     */
    fun clearAll() {
        models.clear()
        currentMaxZIndex = 0f
    }
}
