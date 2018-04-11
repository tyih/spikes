package com.novoda.spikes.arcore.helper

import android.content.Context
import com.google.ar.core.*
import com.novoda.spikes.arcore.rendering.ObjectRenderer
import java.util.ArrayList


class AnchorsVisualiser(private val context: Context) {

    private val virtualObjectRenderer = ObjectRenderer()
    private val anchors = ArrayList<Anchor>() // Anchors created from taps used for object placing.
    private val anchorMatrix = FloatArray(16) // Temporary matrix allocated here to reduce number of allocations for each frame.

    fun init() {

        virtualObjectRenderer.createOnGlThread(context, "models/andy.obj", "models/andy.png")
        virtualObjectRenderer.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f)
    }

    fun createTouchAnchors(camera: Camera, frame: Frame, tapHelper: TapHelper) {
        val tap = tapHelper.poll()
        if (tap != null && camera.trackingState == TrackingState.TRACKING) {
            for (hit in frame.hitTest(tap)) {
                // Check if any plane was hit, and if it was hit inside the plane polygon
                val trackable = hit.trackable
                // Creates an anchor if a plane or an oriented point was hit.
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose) || trackable is Point && trackable.orientationMode == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL) {
                    // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
                    // Cap the number of objects created. This avoids overloading both the
                    // rendering system and ARCore.
                    if (anchors.size >= 20) {
                        anchors.get(0).detach()
                        anchors.removeAt(0)
                    }
                    // Adding an Anchor tells ARCore that it should track this position in
                    // space. This anchor is created on the Plane to place the 3D model
                    // in the correct position relative both to the world and to the plane.
                    val anchor = hit.createAnchor()
                    anchors.add(anchor)
                    //debugViewDisplayer.append(String.format("Anchor created: %.2f, %.2f, %.2f", anchor.pose.xAxis[0], anchor.pose.yAxis[0], anchor.pose.zAxis[0]))
                    break
                }
            }
        }
    }

    fun visualiseTouchAnchors(frame: Frame, cameraViewMatrix: FloatArray, cameraProjectionMatrix: FloatArray) {
        // Visualize anchors created by touch.
        // Compute lighting from average intensity of the image.
        // The first three components are color scaling factors.
        // The last one is the average pixel intensity in gamma space.
        val colorCorrectionRgba = FloatArray(4)
        frame.lightEstimate.getColorCorrection(colorCorrectionRgba, 0)

        val scaleFactor = 1.0f
        for (anchor in anchors) {
            if (anchor.trackingState != TrackingState.TRACKING) {
                continue
            }
            // Get the current pose of an Anchor in world space. The Anchor pose is updated
            // during calls to session.update() as ARCore refines its estimate of the world.
            anchor.pose.toMatrix(anchorMatrix, 0)

            // Update and draw the model and its shadow.
            virtualObjectRenderer.updateModelMatrix(anchorMatrix, scaleFactor)
            virtualObjectRenderer.draw(cameraViewMatrix, cameraProjectionMatrix, colorCorrectionRgba)
        }
    }

}