package com.dog.floatingobj

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.sceneview.ar.ARSceneView

class MainActivity : ComponentActivity() {

    private var arSceneView: ARSceneView? = null
    private var objectPlacer: ArObjectPlacer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        }

        setContent {
            var coordinates by remember { mutableStateOf("Initializing AR...") }
            var trackingStatus by remember { mutableStateOf("Not tracking") }

            ArCameraScreen(
                coordinates = coordinates,
                trackingStatus = trackingStatus,
                onArSceneViewCreated = { sceneView ->
                    arSceneView = sceneView
                    ArConfig.setupArSession(sceneView)

                    objectPlacer = ArObjectPlacer(
                        sceneView,
                        onCoordinatesUpdate = { coords, status ->
                            coordinates = coords
                            trackingStatus = status
                        }
                    )
                    objectPlacer?.enableTapToPlace()
                }
            )
        }
    }
}