package com.dog.floatingobj

import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Color
import io.github.sceneview.node.SphereNode

object ShapeFactory {

    fun createSphere(
        arSceneView: ARSceneView,
        radius: Float = 0.15f, // 30cm radius
        color: Color = Color(1f, 0f, 0f, 0.75f) // Red
    ): SphereNode {
        return SphereNode(
            engine = arSceneView.engine,
            radius = radius,
            materialInstance = arSceneView.materialLoader.createColorInstance(
                color = color,
                metallic = 0.0f,
                roughness = 0.4f
            )
        )
    }
}