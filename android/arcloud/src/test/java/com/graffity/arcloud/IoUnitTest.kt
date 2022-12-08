package com.graffity.arcloud
//
//import android.content.Context
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.google.protobuf.ByteString
//import com.graffity.arcloud.io.ImageService
//import io.grpc.graffity.image.CameraInfo
//import io.grpc.graffity.image.Position
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.annotation.Config
//import java.io.InputStream
//
//
///**
// * Local unit test for IO functions
// */
//
//@RunWith(AndroidJUnit4::class)
//@Config(sdk = [29]) // Look like JUnit limit to API Level 29
//class IoUnitTest {
//
//    private val context: Context = ApplicationProvider.getApplicationContext()
//
//    @Test
//    fun check_availableArea() {
//        // TRUE Digital Park is in database
//        val gpsPosition = Position.newBuilder()
//            .setLatitude(13.685685)
//            .setLongitude(100.611000)
//            .setAltitude(0.0)
//            .build()
//
//        val response = ImageService().checkAvailableArea(gpsPosition)
//        assert(response.message is String)
//    }
//
//    @Test
//    fun send_vpsGrpc() {
//        // Sending sample image to VPS server
//        // with True Digital Park param
//
//        val cameraInfo = CameraInfo.newBuilder()
//            .setPixelFocalLength((3000/4).toFloat())
//            .setPrincipalPointX((2000/4).toFloat())
//            .setPrincipalPointY((1500/4).toFloat())
//            .setRadialDistortion(0.0F)
//            .build()
//
//        val gpsPosition = Position.newBuilder()
//            .setLatitude(13.685685)
//            .setLongitude(100.611000)
//            .setAltitude(0.0)
//            .build()
//
//        val image: InputStream = context.resources.assets.open("TEST_IMG_2838-2760.jpg")
//        val imageByteArray = ByteArray(image.available())
//        image.read(imageByteArray)
//        val imageByteString = ByteString.copyFrom(imageByteArray)
//        image.close()
//
//        // Do not change to Kotlin PrintIn due to log not show in Run tab
//        System.out.println("Size of imageByteString " + imageByteString.size().toString())
//        System.out.println("cameraInfo " + cameraInfo.toString())
//        System.out.println("gpsPosition " + gpsPosition.toString())
//
//        val response = ImageService().getPose(
//            byteImage = imageByteString,
//            cameraInfo = cameraInfo,
//            gpsPosition = gpsPosition
//        )
//
//        assert(response.message is String)
//    }
//}