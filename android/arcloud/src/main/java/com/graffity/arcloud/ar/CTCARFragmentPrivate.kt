package com.graffity.arcloud.ar

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario.launch
import coil.load
import com.google.android.gms.maps.model.LatLng
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.FixedHeightViewSizer
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.gson.Gson
import com.graffity.android.arcloud.R
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.ViewNode
import kotlinx.coroutines.Dispatchers
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

internal class EventInfoDialog(private val event: CTCEventData) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var title = event.title
        if (title == null) {
            title = "CTC Event"
        }
        var stage = event.stage!!.name

        if (stage != null) {
            val end = stage.length - 1
            stage = stage.slice(0..0) + stage.slice(1..end).lowercase()
        }
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle("  $title")
//                .setMessage("Stage: $stage \nTime: ${event.time_begin}-${event.time_end} \nType: ${event.types?.get(0)} \nDate: ${
//                    event.date?.split("-")?.first()
//                }")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setItems(arrayOf(
                    "Stage: $stage",
                    "Time: ${event.time_begin}-${event.time_end}",
                    "Type: ${event.types?.get(0)}",
                    "Date: ${event.date?.split("-")?.last()} Jun"
                ),
                    DialogInterface.OnClickListener { _, _ ->
                        // START THE GAME!
                    })
                .setNegativeButton("Close",
                    DialogInterface.OnClickListener { _, _ ->
                        // START THE GAME!
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

internal class CTCARPrivate(
    private val sceneView: ArSceneView,
    private val androidLifecycle: Lifecycle,
    private val androidContext: Context,
    private val orientationAngles: FloatArray,
    private val currentLocation: Location,
    private val fragmentManager: FragmentManager
) {
    private val eventInfoUrl =
        "https://graffity-sdk-public.s3.ap-southeast-1.amazonaws.com/ctc2022_event_data.json"
    private val eventLocationConfigUrl =
        "https://graffity-sdk-public.s3.ap-southeast-1.amazonaws.com/ctc2022_location_config.json"

    private var eventData: List<CTCEventData> = emptyList()
    var upcomingEvents = mutableListOf<CTCEventData>()
    var locationConfig: LocationConfig? = null

    var mainHandler: Handler = Handler(androidContext.mainLooper)

    fun start() {
        setEventData() // Do following tasks with chain callback which kinda bad
    }

    private fun setEventData() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(eventInfoUrl)
            .build()

        println(Thread.currentThread())
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    System.err.println("Response not successful")
                    return
                }
                if (response.body != null) {
                    val eventResponse = response.body!!.string()
                    val eventInfo = Gson().fromJson(eventResponse, CTCEventInfo::class.java)
                    eventData = eventInfo.data
//                Log.d("CTCARFragmentPrivate all events", eventInfo.data.size.toString())

                    setLocationConfig()
                }
            }
        })
        client.dispatcher.executorService.shutdown()
    }

    private fun setLocationConfig() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(this.eventLocationConfigUrl)
            .build()

        val updateEventStages = mutableListOf<CTCEventData>()

        println(Thread.currentThread())
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    System.err.println("Response not successful")
                    return
                }
                if (response.body != null) {
                    val locationResponse = response.body!!.string()
                    locationConfig = Gson().fromJson(locationResponse, LocationConfig::class.java)
//                Log.d("CTCARFragmentPrivate all stages", locationConfig?.stages?.size.toString())

                    // Set stage location to event data
                    for (stage in locationConfig?.stages!!) {
                        for (event in eventData) {
                            if (event.stage?.name!!.lowercase() == stage.name!!.lowercase()) {
                                event.stage = stage
                                updateEventStages.add(event)
                            }
                        }
                    }
                    eventData = updateEventStages

                    setupcomingEvents()
                }
            }
        })
        client.dispatcher.executorService.shutdown()
    }

    private fun setupcomingEvents() {
        val simpleDate = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.US)
        val currentDate = simpleDate.format(Date())
//        Log.d("CTCARFragmentPrivate Current date is", currentDate)

        val rightNow = Calendar.getInstance()
        val currentDay = 25
        val currentHour = 10
        val currentMin = 30
//        val currentDay = rightNow.get(Calendar.DAY_OF_MONTH)
//        val currentHour = rightNow.get(Calendar.HOUR_OF_DAY)
//        val currentMin = rightNow.get(Calendar.MINUTE)
        val currentTime = currentHour.toDouble() + currentMin.toDouble() / 60

        for (event in eventData) {
            val eventDate = event.date!!.split("-").last()
            val eventTimeBeginHour = event.time_begin!!.split(":").first()
            val eventTimeBeginMin = event.time_begin.split(":").last()
            val eventTimeEndHour = event.time_end!!.split(":").first()
            val eventTimeEndMin = event.time_end.split(":").last()

            val eventTimeBegin = eventTimeBeginHour.toDouble() + eventTimeBeginMin.toDouble() / 60
            val eventTimeEnd = eventTimeEndHour.toDouble() + eventTimeEndMin.toDouble() / 60

            if (currentDay == eventDate.toInt()) {
                if (currentTime in eventTimeBegin..eventTimeEnd) {
                    this.upcomingEvents.add(event)
                }
            }
        }
//        Log.d("CTCARFragmentPrivate UpcomingEvents size is", this.upcomingEvents.size.toString())

        mainHandler.post(setEventARCard)
    }

    private val setEventARCard = Runnable {
        for (event in upcomingEvents) {
//            Log.d("CTCARFragmentPrivate", "setEventARCard")
            var imageScale = 1.0f
            val rotation = Float3(0.0f, 0.0f, 0.0f)

            val latLng = LatLng(event.stage!!.worldCoor.lat, event.stage!!.worldCoor.lng)
//            val latLng = LatLng(13.57929358142664, 100.10961698724267)
            val cloudPosition = ARCloudPosition(event.uuid!!, latLng, event.stage!!.worldCoor.altAndroid - 1)

            val sampleImageNode = ViewNode()
            val imageView = ImageView(androidContext)
            imageView.load(event.arcard) {
                placeholder(R.drawable.empty)
                allowHardware(false)
            }

            if (event.stage!!.name!!.lowercase() == "creative stage" || event.stage!!.name!!.lowercase() == "business stage" || event.stage!!.name!!.lowercase() == "workshop 1" || event.stage!!.name!!.lowercase() == "workshop 2") {
                imageScale = 2.5f * locationConfig!!.cornerImageScale.toFloat()
                rotation.y = locationConfig!!.cornerAngle * 57.295779513.toFloat() // Radians to Degrees
//                Log.d("CTCARFragmentPrivate locationConfig!!.cornerAngle", locationConfig!!.cornerAngle.toString())
            } else {
                imageScale = 3.8f * locationConfig!!.defaultImageScale.toFloat()
            }

            ViewRenderable.builder()
                .setView(androidContext, imageView)
                .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                .setSizer(FixedHeightViewSizer(imageScale))
                .build(androidLifecycle)
                .thenAccept { viewRenderable ->
                    sampleImageNode.setRenderable(viewRenderable)
                }

            val posVec3 = cloudPosition.getPositionVector(
                orientationAngles[0],
                currentLocation.latLng
            )
            sampleImageNode.position = Float3(posVec3.x, posVec3.y, posVec3.z)
//            Log.d("CTCARFragmentPrivate sampleImageNode.rotation", sampleImageNode.rotation.toString())
            sampleImageNode.rotation = rotation
//            Log.d("CTCARFragmentPrivate sampleImageNode.rotation 2", sampleImageNode.rotation.toString())
//            sampleImageNode.scale = Scale(3.0f)

            sampleImageNode.onTap = { _, _ ->
//                Log.d("CTCARFragmentPrivate", event.uuid)
                val dialog = EventInfoDialog(event)
                dialog.show(fragmentManager, event.uuid)
            }

            sceneView.addChild(sampleImageNode)
        }

//        val latLng1 = LatLng(13.579377, 100.109433)
//        val cloudPosition1 = ARCloudPosition("event.uuid!!343434", latLng1, 12.0)
//        val latLng2 = LatLng(13.579303, 100.109279)
//        val cloudPosition2 = ARCloudPosition("event.uuid!!756839", latLng2, 12.0)
//        val latLng3 = LatLng(13.579220, 100.109110)
//        val cloudPosition3 = ARCloudPosition("event.uuid!!402392", latLng3, 12.0)

//        val divider = 2
//        val modelNode = ModelNode(
//            scale = Float3(1f*divider, 1f*divider, 1f*divider),
//        ).apply {
//            loadModelAsync(
//                context = androidContext,
//                glbFileLocation = "https://graffity-sdk-public.s3.ap-southeast-1.amazonaws.com/3d-samples/bee_gltf.glb",
//                lifecycle = androidLifecycle,
//                autoAnimate = true,
//                autoScale = false,
//            )
//        }
//        val modelNode2 = ModelNode(
//            scale = Float3(1f*divider, 1f*divider, 1f*divider),
//        ).apply {
//            loadModelAsync(
//                context = androidContext,
//                glbFileLocation = "https://graffity-sdk-public.s3.ap-southeast-1.amazonaws.com/3d-samples/bee_gltf.glb",
//                lifecycle = androidLifecycle,
//                autoAnimate = true,
//                autoScale = false,
//            )
//        }
//        val modelNode3 = ModelNode(
//            scale = Float3(1f*divider, 1f*divider, 1f*divider),
//        ).apply {
//            loadModelAsync(
//                context = androidContext,
//                glbFileLocation = "https://graffity-sdk-public.s3.ap-southeast-1.amazonaws.com/3d-samples/bee_gltf.glb",
//                lifecycle = androidLifecycle,
//                autoAnimate = true,
//                autoScale = false,
//            )
//        }

//        sceneView.addChild(modelNode)
//        sceneView.addChild(modelNode2)
//        sceneView.addChild(modelNode3)

//        val posVec3 = cloudPosition1.getPositionVector(
//            orientationAngles[0],
//            currentLocation.latLng
//        )
//        val posVec32 = cloudPosition2.getPositionVector(
//            orientationAngles[0],
//            currentLocation.latLng
//        )
//        val posVec33 = cloudPosition3.getPositionVector(
//            orientationAngles[0],
//            currentLocation.latLng
//        )
//
//        modelNode.position = Float3(posVec3.x, posVec3.y, posVec3.z)
//        modelNode2.position = Float3(posVec32.x, posVec32.y, posVec32.z)
//        modelNode3.position = Float3(posVec33.x, posVec33.y, posVec33.z)
    }
}

internal data class LocationConfig(
    val usePointCloud: Boolean,
    val cornerAngle: Float,
    val cornerImageScale: Double,
    val defaultImageScale: Double,
    val stages: List<CTCEventStage>
)

internal data class CTCEventInfo(
    val timestamp: String,
    val data: List<CTCEventData>
)

internal class CTCEventData(
    val uuid: String? = null,
    val title: String? = null,
    val date: String? = null,
    val time_begin: String? = null,
    val time_end: String? = null,
    var stage: CTCEventStage? = null,
    val types: List<String>? = null,
    val categories: List<String>? = null,
    val thumbnail: String? = null,
    val updated_at: String? = null,
    val arcard: String,
)

internal class CTCEventStage(
    val slug: String? = null,
    val name: String? = null,
    val toShow: Boolean,
    val pointCloudCoor: Vector3,
    val worldCoor: WorldCoor
)

internal class WorldCoor(
    val lat: Double,
    val lng: Double,
//    val alt: Double,
    val altAndroid: Double,
)