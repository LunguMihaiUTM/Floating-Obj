package com.dog.floatingobj

import android.util.Log
import com.google.ar.core.Config
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.scene.PlaneRenderer

object ArConfig {

    fun setupArSession(arSceneView: ARSceneView) {

        arSceneView.planeRenderer.isEnabled = true
        arSceneView.planeRenderer.isVisible = true

        arSceneView.planeRenderer.planeRendererMode = PlaneRenderer.PlaneRendererMode.RENDER_ALL


        arSceneView.configureSession { session, config ->

            // Tells ARCore to detect flat horizontal and vertical surfaces
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL

            /***
             * ARCore either uses a depth sensor (if available) or multi-view stereo (infers depth by moving the camera).
             * This creates a depth map: a per-pixel distance value from camera to real objects.
             * Used for occlusion, physics, and more realistic 3D placement.
             */
            config.depthMode = Config.DepthMode.AUTOMATIC

            // Enables light estimation based on the real environment.
            config.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }
    }
}