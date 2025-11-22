package com.dog.floatingobj

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.ar.ARSceneView


/**
 * Creates a Composable that embeds the AR camera view.
 * It uses AndroidView to bridge the traditional Android ARSceneView into Jetpack Compose.
 * When the view is created, it passes the instance back via callback.
 */

@Composable
fun ArCameraScreen(
    modifier: Modifier = Modifier,
    onArSceneViewCreated: (ARSceneView) -> Unit = {}
) {
    AndroidView(
        factory = { context ->
            ARSceneView(context).also { sceneView ->
                onArSceneViewCreated(sceneView)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}