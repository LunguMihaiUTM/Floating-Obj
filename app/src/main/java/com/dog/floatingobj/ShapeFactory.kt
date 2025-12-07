package com.dog.floatingobj

import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Color
import io.github.sceneview.node.SphereNode
import io.github.sceneview.math.Size
import io.github.sceneview.math.Position
import io.github.sceneview.node.ImageNode

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import io.github.sceneview.node.Node

object ShapeFactory {

    fun createSphere(
        arSceneView: ARSceneView,
        radius: Float = 0.15f, // 30cm radius
        customColor: Color = Color(1f, 0f, 0f, 0.75f) // Red
    ): SphereNode {
        return SphereNode(
            engine = arSceneView.engine,
            radius = radius,
            materialInstance = arSceneView.materialLoader.createColorInstance(
                color = customColor,
                metallic = 0.0f,
                roughness = 0.4f
            )
        )
    }
    fun createText(
        arSceneView: ARSceneView,
        text: String = "Hello World",
        radius: Float = 0.15f,
        customColor: Color = Color(1f, 0f, 0f, 0.75f)
    ): ImageNode {
        val bitmapWidth = 512
        val bitmapHeight = 256
        val bitmap = createBitmap(bitmapWidth, bitmapHeight)
        val canvas = Canvas(bitmap)

        // Clear completely
        canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

        // Draw background
        canvas.drawColor(android.graphics.Color.argb(200, 0, 0, 0))

        // Draw text
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 50f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Handle multiline text
        val lines = text.split("\n")
        val lineHeight = 60f
        val startY = bitmapHeight / 2f - (lines.size - 1) * lineHeight / 2f

        lines.forEachIndexed { index, line ->
            canvas.drawText(line, bitmapWidth / 2f, startY + index * lineHeight, paint)
        }

        return ImageNode(
            materialLoader = arSceneView.materialLoader,
            bitmap = bitmap,
            size = Size(0.3f, 0.15f)
        )
    }
}