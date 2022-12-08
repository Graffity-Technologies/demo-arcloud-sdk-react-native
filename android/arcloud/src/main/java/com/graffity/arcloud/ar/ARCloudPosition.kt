package com.graffity.arcloud.ar

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.ar.sceneform.math.Vector3
import com.google.maps.android.ktx.utils.sphericalDistance
import com.google.maps.android.ktx.utils.sphericalHeading
import kotlin.math.cos
import kotlin.math.sin

data class ARCloudPosition(
    val id: String,
    val latLng: LatLng,
    val altitude: Double,
//    val orientation: Double,
) {
    val TAG = "ARCloudPosition"
    override fun equals(other: Any?): Boolean {
        if (other !is ARCloudPosition) {
            return false
        }
        return this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}

internal fun ARCloudPosition.getPositionVector(azimuth: Float, latLng: LatLng): Vector3 {
    val placeLatLng = this.latLng

    val headingDegree = latLng.sphericalHeading(placeLatLng)
    Log.d(TAG, headingDegree.toString())

    val headingRadian = Math.toRadians(headingDegree)
    Log.d(TAG, headingRadian.toString())

    val distance = latLng.sphericalDistance(placeLatLng)
    Log.d(TAG, distance.toString())

    // Scale to distance with multiply r
    var r = -distance.toFloat()
    if (distance > 20f) { // if distance longer than 28m. node will disappear
        r = -20f
    }
    val x = r * sin(azimuth - headingRadian).toFloat()
    val y = this.altitude.toFloat() - 4 // TODO: remove - 10 ?
    val z = r * cos(azimuth - headingRadian).toFloat()
    return Vector3(x, y, z)
}

data class Geometry(
    val location: GeometryLocation
)

data class GeometryLocation(
    val lat: Double,
    val lng: Double
) {
    val latLng: LatLng
        get() = LatLng(lat, lng)
}