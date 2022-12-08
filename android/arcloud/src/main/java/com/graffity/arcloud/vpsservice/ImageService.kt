package com.graffity.arcloud.vpsservice

import android.annotation.SuppressLint
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.graffity.image.*
import io.grpc.stub.MetadataUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.Closeable

private object Constants {
    const val vps_host = "vps.graffity.services"
    const val vps_port = 443
    const val vps_api_key = "KpN4I5l4G8gFB8HVx6Xd"
}

internal class ImageService() : Closeable {
    private val host = Constants.vps_host
    private val port = Constants.vps_port
    private val apiKey = Constants.vps_api_key

    private val channel = let {
//        Log.d("Connecting to", "${host}:${port}")

        val builder = ManagedChannelBuilder.forAddress(host, port)
        builder.useTransportSecurity()
        builder.executor(Dispatchers.IO.asExecutor()).build()
    }

    private val headers = Metadata()
    private val key = Metadata.Key.of("x-graff-api-key", Metadata.ASCII_STRING_MARSHALLER)


    @SuppressLint("CheckResult")
    internal fun getPose(
        byteImage: ByteString,
        cameraInfo: CameraInfo,
        gpsPosition: Position
    ): ImageResponse {
        val call = ImageGrpc.newBlockingStub(channel) // Change this to make Non-blocking call
        headers.put(key, apiKey)

        return try {
            val request = ImageRequest.newBuilder()
                .setMessage("Send Image to VPS Service")
                .setBytesImage(byteImage)
                .setCameraInfo(cameraInfo)
                .setGpsPosition(gpsPosition)
                .build()

            call
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers))
                .sendImage(request)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    internal fun checkAvailableArea(gpsPosition: Position): AvailableAreaResponse {
        val call = AvailableAreaGrpc.newBlockingStub(channel) // Change this to make Non-blocking call
        headers.put(key, apiKey)

        return try {
            val request = AvailableAreaRequest.newBuilder()
                .setGpsPosition(gpsPosition)
                .setMaxDistance(500) // metres
                .setMinDistance(0) // metres
                .build()

            call
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers))
                .checkAvailableArea(request)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun close() {
        channel.shutdownNow()
    }
}