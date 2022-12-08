package com.graffity.arcloud.sdkservice

import com.google.ar.sceneform.math.Vector3
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers

internal enum class GrafARContentType {
    model3d,
    video,
    image,
    detection_image
}

internal enum class GrafARAnchorType {
    world_anchor,
    image_anchor,
    face_anchor,
    vertical_plane_anchor,
    horizontal_plane_anchor
}

internal data class AccessToken(
    @field:Json(name="_id") val _id: String, // in case json key is not same case @Json(name = "img_src")
    @field:Json(name="version") val version: String,
)

internal data class ARContent(
    @field:Json(name="_id") val _id: String,
    @field:Json(name="arContentId") val arContentId: String,
    @field:Json(name="parentId") val parentId: String? = "",
    @field:Json(name="arContentType") val arContentType: String,
    @field:Json(name="arAnchorType") val arAnchorType: String,

    @field:Json(name="location2D") val location2D: Location2D,
    @field:Json(name="altitude") val altitude: Double,

    @field:Json(name="position") val position: Vector3,
    @field:Json(name="scale") val scale: Vector3,
    @field:Json(name="rotation") val rotation: Vector3,

    @field:Json(name="renderRadiusInMeter") val renderRadiusInMeter: Int,
    @field:Json(name="isFrontFacing") val isFrontFacing: Boolean,

    @field:Json(name="downloadUrl") val downloadUrl: String,
)

internal data class Location2D(
    @field:Json(name="type") val type: String,
    @field:Json(name="coordinates") val coordinates: List<Double>
)

internal data class ValidateAccessTokenBody(
    @field:Json(name="raw") val raw: String,
)

internal data class GetAnchorObjectsBody(
    @field:Json(name="accessTokenRefId") val accessTokenRefId: String,
)


private var host = "https://console-backend-xoeyqjvd6q-as.a.run.app"
// if (BuildConfig.DEBUG) "https://fe8b-2403-6200-8863-5b2d-c15d-dab0-9a40-6a47.ap.ngrok.io" else
private const val xGraffApiKey = "YTA4Yjc4NWUtMjgxYi00ZTRmLWFlNjAtNTQ5NmJjZmVlNjdmLTUxNWUxMDE3LWYzNDktNDdlMi05MjBhLTMzODBlMTNhNGQ5YQ=="

internal val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

internal val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
//    .addConverterFactory(MoshiConverterFactory.create())
    .baseUrl(host)
    .build()

internal object SdkApi {
    internal val retrofitService : SdkApiService by lazy {
//        Log.d("SdkApiService", host)
//        Log.d("SdkApiService", retrofit.toString())
        retrofit.create(SdkApiService::class.java)
    }
}

internal interface SdkApiService {

    @Headers(
        "Content-Type: application/json",
        "x-graff-console: $xGraffApiKey",
    )
    @POST("/api/v1/access-token/validate")
    suspend fun validateAccessToken(@Body body: Map<String, String>): Response<AccessToken>

    @Headers(
        "Content-Type: application/json",
        "x-graff-console: $xGraffApiKey",
    )
    @POST("/api/v1/ar-content/get")
    suspend fun getAnchorObjects(@Body body: Map<String, String>): Response<List<ARContent>>
}