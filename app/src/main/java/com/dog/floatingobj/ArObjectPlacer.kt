package com.dog.floatingobj

import android.util.Log
import android.view.MotionEvent
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.sqr
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ImageNode
import io.github.sceneview.node.Node
import kotlin.math.sqrt
import io.github.sceneview.collision.HitResult as SceneHitResult


data class SphereData(
    val anchorNode: AnchorNode,
    val worldPosition: Float3 = Float3(0.0f, 0.0f, 0.0f)
)

class ArObjectPlacer(
    private val arSceneView: ARSceneView,
    private val onCoordinatesUpdate: (String, String) -> Unit = { _, _ -> },
    private val onSphereUpdate: (List<SphereData>) -> Unit = {}
) {
    private val placedNodes = mutableListOf<AnchorNode>()
    private var selectedNode: AnchorNode? = null
    private val textNodes = mutableMapOf<Node, ImageNode>() // Track text nodes


    fun enableTapToPlace() {
        arSceneView.onTouchEvent = { motionEvent: MotionEvent, hitResult: SceneHitResult? ->
            handleTouch(motionEvent, hitResult)
        }

        startPlaneMonitoring()
    }

    private fun handleTouch(motionEvent: MotionEvent, hitResult: SceneHitResult?): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedNode = findAnchorNodeFromHit(hitResult)

                if (selectedNode == null) {
                    tryPlaceObject(motionEvent)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                selectedNode?.let { node ->
                    dragNode(node, motionEvent)
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                selectedNode?.let { node ->
                    updateTextForNode(node)
                }
                selectedNode = null
            }
        }
        return false
    }

    private fun findAnchorNodeFromHit(sceneHitResult: SceneHitResult?): AnchorNode? {
        var node: Node? = sceneHitResult?.node
        while (node != null) {
            if (node is AnchorNode && placedNodes.contains(node)) {
                return node
            }
            node = node.parent
        }
        return null
    }

    private fun tryPlaceObject(motionEvent: MotionEvent) {
        val frame = arSceneView.session?.frame ?: return
        val session = arSceneView.session ?: return

        if (frame.camera.trackingState != TrackingState.TRACKING) return

        val hits = frame.hitTest(motionEvent.x, motionEvent.y)

        val planeHit = hits.firstOrNull { hitResult ->
            val trackable = hitResult.trackable
            trackable is Plane &&
                    trackable.isPoseInPolygon(hitResult.hitPose) &&
                    (trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING ||
                            trackable.type == Plane.Type.VERTICAL)
        }

        planeHit?.let {
            placeSphereWithWorldAnchor(it, session)
        }
    }

    // can't really "move" an anchor, will destroy the old one and create new one
    private fun dragNode(node: AnchorNode, motionEvent: MotionEvent) {
        val frame = arSceneView.session?.frame ?: return
        val session = arSceneView.session ?: return

        if (frame.camera.trackingState != TrackingState.TRACKING) return
        val hits = frame.hitTest(motionEvent.x, motionEvent.y)

        val planeHit = hits.firstOrNull { hitResult ->
            val trackable = hitResult.trackable
            trackable is Plane &&
                    trackable.isPoseInPolygon(hitResult.hitPose) &&
                    (trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING ||
                            trackable.type == Plane.Type.VERTICAL)
        } ?: return

        // create first
        val newPose = planeHit.hitPose
        val newAnchor = session.createAnchor(newPose)

        //destroy the old one
        node.anchor.detach()
        node.anchor = newAnchor
    }

    private fun startPlaneMonitoring() {
        arSceneView.onFrame = { _ ->
            // Monitor planes
            arSceneView.session?.frame?.getUpdatedTrackables(Plane::class.java)

            // Update coordinates for UI
            val frame = arSceneView.session?.frame
            if (frame != null) {
                if (frame.camera.trackingState == TrackingState.TRACKING) {
                    val camPos = arSceneView.cameraNode.worldPosition
                    val coordinates = "x=%.2f, y=%.2f, z=%.2f".format(camPos.x, camPos.y, camPos.z)
                    val status = "Tracking ✓"

                    arSceneView.post {
                        onCoordinatesUpdate(coordinates, status)
                    }
                } else {
                    val status = when (frame.camera.trackingState) {
                        TrackingState.PAUSED -> "Tracking Paused"
                        TrackingState.STOPPED -> "Tracking Stopped"
                        else -> "Not tracking"
                    }

                    arSceneView.post {
                        onCoordinatesUpdate("Initializing AR...", status)
                    }
                }
            }
        }
    }

    private fun updateTextForNode(anchorNode: AnchorNode) {
        // Get the sphere node (first child of anchor)
        val sphereNode = anchorNode.childNodes.firstOrNull() ?: return

        // Get current text node
        val oldTextNode = textNodes[sphereNode] ?: return

        // Get new position
//        val pos = sphereNode.worldPosition
        val text = "X:%.2f".format(calculateDistance(arSceneView.cameraNode, sphereNode))

        // Create new text node
        val newTextNode = ShapeFactory.createText(
            arSceneView,
            text = text
        )
        newTextNode.position = oldTextNode.position

        // Replace old with new
        sphereNode.removeChildNode(oldTextNode)  // Remove from SPHERE, not anchor
        sphereNode.addChildNode(newTextNode)     // Add to SPHERE, not anchor

        textNodes[sphereNode] = newTextNode
    }

    private fun placeSphereWithWorldAnchor(hitResult: HitResult, session: Session) {
        // still using the plane for sphere initial coordinates
        val hitPose = hitResult.hitPose

        // creating the anchor from the session position (no more plane dependency )
        val worldAnchor = session.createAnchor(hitPose)

        val anchorNode = AnchorNode(
            engine = arSceneView.engine,
            anchor = worldAnchor
        )

        arSceneView.addChildNode(anchorNode)

        val sphereNode = ShapeFactory.createSphere(arSceneView)

        anchorNode.addChildNode(sphereNode)

        // set the rotation for all nodes to be orientated up
        val spherePos = sphereNode.worldPosition
        val camPos = arSceneView.cameraNode.worldPosition

        val dx = camPos.x - spherePos.x
        val dz = camPos.z - spherePos.z
        val yaw = kotlin.math.atan2(dx, dz) * 180f / Math.PI.toFloat()

        sphereNode.worldRotation = Rotation(0f, yaw, 0f) // pitch 0, roll 0, yaw calculat

        arSceneView.addChildNode(anchorNode)

        val initialText = "X:%.2f".format(calculateDistance(arSceneView.cameraNode, sphereNode))
        //create one child sphere tied to the parent one
        val childSphereNode = ShapeFactory.createText(
            arSceneView,
            text = initialText
        )

        val offset = Position(0f, 0.3f, 0f)
        childSphereNode.position = offset

        sphereNode.addChildNode(childSphereNode)

        textNodes[sphereNode] = childSphereNode

        // add to the general list
        placedNodes.add(anchorNode)

        //show all current nodes coordinates
        showCoordinates()
    }

    private fun showCoordinates() {
        var i = 0
        for (node in placedNodes) {
            val position: Float3 = node.position
            Log.d(
                "Coordinates for node ${++i}",
                "X: ${position.x}, Y: ${position.y}, Z: ${position.z}\n"
            )
        }
    }

    private fun calculateDistance(node1: Node, node2: Node): Float {
        val position1: Float3 = node1.worldPosition
        val position2: Float3 = node2.worldPosition
        return sqrt(sqr(position2.x - position1.x) + sqr(position2.y - position1.y) + sqr(position2.z - position1.z))
    }
}