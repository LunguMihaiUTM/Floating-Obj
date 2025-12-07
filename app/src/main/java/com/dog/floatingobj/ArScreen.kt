package com.dog.floatingobj

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.ar.ARSceneView

@Composable
fun ArCameraScreen(
    modifier: Modifier = Modifier,
    coordinates: String,
    trackingStatus: String,
    onArSceneViewCreated: (ARSceneView) -> Unit = {}
) {

    Box(
        modifier = modifier.fillMaxSize()
    ){
        AndroidView(
            factory = { context ->
                ARSceneView(context).apply {
                    // Don't set onFrame here - it will be overwritten by ArObjectPlacer
                    // Pass the update callback instead
                    onArSceneViewCreated(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Coordinates display
        Text(
            text = coordinates,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // Tracking status display
        Text(
            text = trackingStatus,
            color = if (trackingStatus == "Tracking ✓") Color.Green else Color.Yellow,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}