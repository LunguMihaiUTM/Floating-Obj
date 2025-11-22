package com.dog.floatingobj

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode

class ArObjectPlacer(private val arSceneView: ARSceneView) {

    private val gestureDetector = GestureDetector(
        arSceneView.context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                onTap(e)
                return true
            }
        }
    )

    fun enableTapToPlace() {
        arSceneView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        // Start continuous plane monitoring
        startPlaneMonitoring()
    }

    private var frameCount = 0

    private fun startPlaneMonitoring() {
        arSceneView.onFrame = { frameTime ->
            frameCount++
            val frame = arSceneView.session?.frame
            if (frame != null) {
                val allPlanes = frame.getUpdatedTrackables(Plane::class.java)

                // Log every 60 frames (once per second at 60fps)
                if (frameCount % 60 == 0) {
                    Log.d("ArDebug", "===== PLANE STATUS =====")
                    Log.d("ArDebug", "Total planes: ${allPlanes.size}")

                    var horizontalCount = 0
                    var verticalCount = 0

                    allPlanes.forEach { plane ->
                        if (plane.trackingState == TrackingState.TRACKING && plane.subsumedBy == null) {
                            when (plane.type) {
                                Plane.Type.HORIZONTAL_UPWARD_FACING -> horizontalCount++
                                Plane.Type.VERTICAL -> {
                                    verticalCount++
                                    val polygonPointCount = plane.polygon.limit() / 2
                                    Log.d("ArDebug", "  VERTICAL plane: " +
                                            "polygonPoints=$polygonPointCount, " +
                                            "subsumed=${plane.subsumedBy != null}")
                                }
                                else -> {}
                            }
                        }
                    }

                    Log.d("ArDebug", "Horizontal: $horizontalCount, Vertical: $verticalCount")
                }
            }
        }
    }

    private fun onTap(motionEvent: MotionEvent) {
        val frame = arSceneView.session?.frame ?: return

        // Debug: Log all detected planes AT THE TIME OF TAP
        val allPlanes = frame.getUpdatedTrackables(Plane::class.java)
        Log.d("ArDebug", "=== TAP EVENT - All Planes ===")
        allPlanes.forEach { plane ->
            Log.d("ArDebug", "Plane: ${plane.type}, " +
                    "tracking=${plane.trackingState}, " +
                    "subsumed=${plane.subsumedBy != null}")
        }

        val hits = frame.hitTest(motionEvent.x, motionEvent.y)

        Log.d("ArDebug", "=== TAP EVENT - Hit Results ===")
        hits.forEach { hitResult ->
            val trackable = hitResult.trackable
            if (trackable is Plane) {
                Log.d("ArDebug", "Hit: ${trackable.type}, " +
                        "inPolygon=${trackable.isPoseInPolygon(hitResult.hitPose)}")
            }
        }

        // Simple plane hit detection without stability filtering
        val planeHit = hits.firstOrNull { hitResult ->
            val trackable = hitResult.trackable
            val isValid = trackable is Plane &&
                    trackable.isPoseInPolygon(hitResult.hitPose) &&
                    (trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING ||
                            trackable.type == Plane.Type.VERTICAL)

            if (trackable is Plane) {
                Log.d("ArDebug", "Evaluating: ${trackable.type}, valid: $isValid")
            }

            isValid
        }

        planeHit?.let {
            val plane = it.trackable as Plane
            Log.d("ArDebug", "✅ PLACING OBJECT ON: ${plane.type}")
            placeSphere(it)
        } ?: Log.d("ArDebug", "❌ NO VALID PLANE HIT")
    }

    private fun placeSphere(hitResult: HitResult) {
        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(
            engine = arSceneView.engine,
            anchor = anchor
        )
        val sphereNode = ShapeFactory.createSphere(arSceneView)
        anchorNode.addChildNode(sphereNode)
        arSceneView.addChildNode(anchorNode)
    }
}