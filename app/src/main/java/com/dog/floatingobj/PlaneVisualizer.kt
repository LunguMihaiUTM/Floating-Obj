package com.dog.floatingobj

import android.util.Log
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Color
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.PlaneNode
import io.github.sceneview.node.RenderableNode
import kotlin.math.asin
import kotlin.math.atan2

object PlaneVisualizer {

    private val visiblePlanes = mutableMapOf<Plane, RenderableNode>()

    fun update(arSceneView: ARSceneView) {
        val session = arSceneView.session ?: return
        val frame = session.update()
        val planes = frame.getUpdatedTrackables(Plane::class.java)

        planes.forEach { plane ->
            if (plane.trackingState != TrackingState.TRACKING) return@forEach

            if (plane.type == Plane.Type.VERTICAL) {
                val existing = visiblePlanes[plane]
                if (existing == null) {
                    val newNode = createPlaneNode(arSceneView, plane)
                    arSceneView.addChildNode(newNode)
                    visiblePlanes[plane] = newNode
                } else {
                    updatePlaneNode(existing, plane)
                }
            }
        }
    }

    private fun createPlaneNode(arSceneView: ARSceneView, plane: Plane): RenderableNode {
        val color = Color(0f, 1f, 0f, 0.2f) // semi-transparent green

        val material = arSceneView.materialLoader.createColorInstance(
            color = color,
            metallic = 0f,
            roughness = 1f
        )

        val pose = plane.centerPose
        val extentX = plane.extentX
        val extentZ = plane.extentZ

        val node = PlaneNode(
            engine = arSceneView.engine,
            materialInstance = material
        ).apply {
            position = Position(pose.tx(), pose.ty(), pose.tz())
            rotation = quaternionToEuler(pose.qx(), pose.qy(), pose.qz(), pose.qw())
            scale = Scale(extentX, 1f, extentZ)
        }

        Log.d("PlaneVisualizer", "Created vertical plane visualizer node")
        return node
    }

    private fun updatePlaneNode(node: RenderableNode, plane: Plane) {
        val pose = plane.centerPose
        node.position = Position(pose.tx(), pose.ty(), pose.tz())
        node.rotation = quaternionToEuler(pose.qx(), pose.qy(), pose.qz(), pose.qw())
        node.scale = Scale(plane.extentX, 1f, plane.extentZ)
    }

    /** Converts quaternion (qx, qy, qz, qw) to Euler rotation (in radians). */
    private fun quaternionToEuler(x: Float, y: Float, z: Float, w: Float): Rotation {
        val ysqr = y * y

        val t0 = +2.0f * (w * x + y * z)
        val t1 = +1.0f - 2.0f * (x * x + ysqr)
        val roll = atan2(t0, t1)

        var t2 = +2.0f * (w * y - z * x)
        t2 = if (t2 > 1.0f) 1.0f else if (t2 < -1.0f) -1.0f else TODO()
        val pitch = asin(t2)

        val t3 = +2.0f * (w * z + x * y)
        val t4 = +1.0f - 2.0f * (ysqr + z * z)
        val yaw = atan2(t3, t4)

        return Rotation(roll, pitch, yaw)
    }
}
