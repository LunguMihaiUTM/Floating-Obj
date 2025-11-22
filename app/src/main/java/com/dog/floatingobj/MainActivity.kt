package com.dog.floatingobj

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.sceneview.ar.ARSceneView


/**
 * Requests camera permission, creates the AR screen,
 * and stores a reference to the ARSceneView for later use (placing objects).
 */

class MainActivity : ComponentActivity() {

    private var arSceneView: ARSceneView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA),100)
        }

        setContent {
            ArCameraScreen(
                onArSceneViewCreated = {sceneView ->
                    arSceneView = sceneView
                    ArConfig.setupArSession(sceneView)

                    val objectPlacer = ArObjectPlacer(sceneView)
                    objectPlacer.enableTapToPlace()
                }
            )
        }
    }
}
