package com.example.modelviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.modelviewer.ui.screens.MainScreen
import com.example.modelviewer.ui.theme.Space3DTheme
import com.example.modelviewer.viewmodel.ModelViewModel
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

/**
 * Single Activity Architecture entry point.
 * Configures the fullscreen layout canvas and initializes the shared Filament render context.
 */
class MainActivity : ComponentActivity() {
    
    private val viewModel: ModelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide standard window decorations to maximize the visual canvas area
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Space3DTheme {
                // Initialize the shared C++ Filament engine at the Compose root
                // This ensures one instance is shared and properly disposed of on teardown
                val engine = rememberEngine()
                val modelLoader = rememberModelLoader(engine)
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        engine = engine,
                        modelLoader = modelLoader,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
