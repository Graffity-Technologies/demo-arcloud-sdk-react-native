package com.graffity.arcloud.ar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.ar.core.*
import com.google.ar.sceneform.rendering.FixedHeightViewSizer
import com.google.ar.sceneform.rendering.ViewRenderable
//import com.google.ar.sceneform.ux.VideoNode
import com.google.maps.android.ktx.utils.sphericalDistance
import com.graffity.android.arcloud.R
import com.graffity.android.arcloud.databinding.FragmentArcloudPrivateBinding
import com.graffity.arcloud.helpers.getBitmapFromURL
import com.graffity.arcloud.sdkservice.ARContent
import com.graffity.arcloud.sdkservice.AccessTokenViewModel
import com.graffity.arcloud.sdkservice.GrafARAnchorType
import com.graffity.arcloud.sdkservice.GrafARContentType
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.scale
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArFrame
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.ar.arcore.LightEstimationMode
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.collision.pickHitTest
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.node.ViewNode
import kotlinx.coroutines.launch


internal class ARCloudPrivateFragment : Fragment(), SensorEventListener {
    private val fragmentTag = "ARCloudPrivateFragment"

    private var _binding: FragmentArcloudPrivateBinding? = null
    private val binding get() = _binding!!

    // Sensor
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // AR
    var nodes = ArrayList<AccessibleARNode>()
    var onSceneTouched: ((nodeId: String?) -> Unit)? = null
    private var isStartAutoAnchorNode = false

    //    private var isStartAnchorModel = false
    var arContents = mutableListOf<ARContent>()
    var initWorldAnchorContents = mutableListOf<ARContent>()
    var imageAnchorNodes = mutableListOf<Node>()

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }
    private var isTheBarrelAnchored = false

    //    private var sampleVideoNode: VideoNode? = null
    private var sampleModelNode: ModelNode? = null

    //    private var sampleImageNode: ViewNode? = null
    private var arMediaPlayer: MediaPlayer? = null
//    private var augmentedImageModelNode: ArModelNode? = null
//    private var augmentedImageVideoNode: ArModelNode? = null

    // Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // ViewModel
    private val sharedAccessTokenViewModel: AccessTokenViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArcloudPrivateBinding.inflate(inflater, container, false)

//        Get ARContents form API Call
//        val anchorObjectViewModelFactory = sharedAccessTokenViewModel.data.value?.let {
//            AnchorObjectViewModelFactory(it)
//        }
//
//        val anchorObjectViewModel = anchorObjectViewModelFactory?.let {
//            ViewModelProvider(
//                this,
//                it
//            )[AnchorObjectViewModel::class.java]
//        }

//        anchorObjectViewModel?.data?.observe(viewLifecycleOwner) {
//            arContents = it as MutableList<ARContent>
//            Log.d(fragmentTag, "Loaded arContents ${arContents.size}")
//        }

//        val sampleVideoStreamLink = "https://videodelivery.net/41134e882e709fb7db40797972ef1385/manifest/video.m3u8"
//
//        arMediaPlayer = try {
//            MediaPlayer().apply {
//                isLooping = true
//                setAudioAttributes(
//                    AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .build()
//                )
//                setDataSource(sampleVideoStreamLink)
//                prepare()
//            }
//        } catch (e: IOException) {
//            Log.e(TAG, e.toString())
//            null
//        }
//        Log.d(fragmentTag, arMediaPlayer.toString())
//        arMediaPlayer?. let {
//            sampleVideoNode = VideoNode(context, lifecycle, arMediaPlayer, null)
//        }

//        augmentedImageVideoNode = ArModelNode()

//        sampleImageNode = ViewNode()
//        val imageView = ImageView(context)
//        imageView.load("https://graffity-sdk-public.s3.ap-southeast-1.amazonaws.com/Tests/demo-food-3d.png") {
//            placeholder(R.drawable.empty)
//            allowHardware(false)
//        }

//        Log.d("$fragmentTag imageView", imageView.toString())

//        ViewRenderable.builder()
//            .setView(context, imageView)
//            .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
//            .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
//            .setSizer(FixedHeightViewSizer(0.1f))
//            .build(lifecycle)
//            .thenAccept { viewRenderable ->
//                sampleImageNode?.let {
////                    Log.d("$fragmentTag ViewRenderable", it.toString())
//                    sampleImageNode!!.setRenderable(viewRenderable)
//                }
//            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SETUP FULL SCREEN
        requireActivity().actionBar?.hide()
//        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // AR
        sceneView = view.findViewById(R.id.sceneView)
        loadingView = view.findViewById(R.id.loadingView)

        sceneView.instantPlacementEnabled = true
        sceneView.cloudAnchorEnabled = false
        sceneView.instructions.enabled = false
        sceneView.depthEnabled = true
        sceneView.lightEstimationMode = LightEstimationMode.DISABLED

        sceneView.planeRenderer.isVisible = false
        sceneView.configureSession(this::initializeSceneViewSession)
        sceneView.onAugmentedImageUpdate += this::checkAugmentedImageUpdate
        sceneView.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        sceneView.onArFrame = this::onUpdateARFrame

        sceneView.onTap = { motionEvent, node, renderable ->
//            Log.d(fragmentTag, "sceneView onTap")
//            Log.d(fragmentTag, "sceneView onTap node ${node?.id}")
            if (node != null) {
                onSceneTouched?.invoke(node.id)
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    // Update UI with location data
                    currentLocation = location
//                    Log.d("$fragmentTag currentLocation", location.latLng.toString())
//                    Log.d("anchorObjects", anchorObjects.toString())
                    if (initWorldAnchorContents.isNotEmpty() && currentLocation != null) { // && !isStartAutoAnchorNode
//                        Log.d(fragmentTag, arContents.size.toString())
                        startWorldAnchorRenderer()
                    }
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        startLocationUpdates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.initWorldAnchorContents = getARContentByAnchorType(this.arContents, GrafARAnchorType.world_anchor)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        createLocationRequest()
    }

    private fun onUpdateARFrame(arFrame: ArFrame) {
        updateWorldAnchor()
    }

    // Augmented Image Start
    private fun checkAugmentedImageUpdate(augmentedImage: AugmentedImage) {

        if (augmentedImage.trackingState == TrackingState.TRACKING) {
            val anchorImage = augmentedImage.createAnchor(augmentedImage.centerPose)
            val customArNode = ArNode(anchorImage)

            when (augmentedImage.trackingMethod) {
                AugmentedImage.TrackingMethod.LAST_KNOWN_POSE -> {
                    // The planar target is currently being tracked based on its last known pose.
//                    Log.d(fragmentTag, "LAST_KNOWN_POSE")
                }
                AugmentedImage.TrackingMethod.FULL_TRACKING -> {
                    // The planar target is being tracked using the current camera image.
//                    Log.d(fragmentTag, "FULL_TRACKING")
                }
                AugmentedImage.TrackingMethod.NOT_TRACKING -> {
                    // The planar target isn't been tracked.
//                    Log.d(fragmentTag, "NOT_TRACKING")

                    sampleModelNode?.let { sceneView.removeChild(it) }
//                    sampleVideoNode?.let { sceneView.removeChild(it) }
                }
            }

            val detectionImages = getARContentByContentTypeAndAnchorType(
                arContents,
                GrafARContentType.detection_image,
                GrafARAnchorType.image_anchor
            )
            val imageAnchorContents =
                getARContentByAnchorType(arContents, GrafARAnchorType.image_anchor)

            for (detectionImage in detectionImages) {
                if (augmentedImage.name == detectionImage.arContentId) {
                    val scaleFactor = 0.035f // manual tune
                    val positionFactor = 1.0f // manual tune

                    for (node in imageAnchorNodes) {
                        for (content in imageAnchorContents) {
                            if (node.id == content.arContentId && detectionImage.arContentId == content.parentId) {
//                            Log.d(fragmentTag, node.toString())
                                if (content.arContentType == GrafARContentType.model3d.toString()) {
                                    val modelNode = node as ArModelNode// 3D Model
                                    modelNode.anchor = anchorImage
                                    modelNode.modelPosition = Float3(
                                        content.position.x * positionFactor,
                                        content.position.z * positionFactor, // z in Graffity Console equal to y in Android
                                        content.position.y * positionFactor, // y in Graffity Console equal to z in Android
                                    )
                                    modelNode.scale = Float3(
                                        content.scale.x * scaleFactor,
                                        content.scale.y * scaleFactor,
                                        content.scale.z * scaleFactor
                                    )
                                    modelNode.modelRotation = Float3(
                                        (content.rotation.x * 180 / Math.PI).toFloat(),
                                        (content.rotation.y * 180 / Math.PI).toFloat(),
                                        (content.rotation.z * 180 / Math.PI).toFloat()
                                    )
                                    sceneView.addChild(modelNode)
                                }
//                            else if (content.arContentType == GrafARContentType.image.toString()) { // Image
//                                val imageNode = node as ViewNode
//                                imageNode.parent = customArNode
//
////                                imageNode.scale = Float3(
////                                    10f, 10f, 10f
////                                )
//                                Log.d(fragmentTag, "imageNode position ${imageNode.position}")
//                                Log.d(fragmentTag, "imageNode rotation ${imageNode.rotation}")
//                                Log.d(fragmentTag, "imageNode scale ${imageNode.scale}")
//                                sceneView.addChild(imageNode)
//                            }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initializeSceneViewSession(session: ArSession, config: Config) {
//        Log.d(fragmentTag, "initialiseSceneViewSession")
//        Log.d(fragmentTag, arContents.size.toString())

        // Add Detection Image to AR Session Database
        val detectionImages = getARContentByContentTypeAndAnchorType(
            arContents,
            GrafARContentType.detection_image,
            GrafARAnchorType.image_anchor
        )
//        Log.d(fragmentTag, "detectionImages.size ${detectionImages.size}")
        if (detectionImages.isNotEmpty()) {
            prepareImageAnchorContents()
            val imageDatabase = AugmentedImageDatabase(session)
            val imageWidthInMeters = 0.15f // 10cm define to reduce image detection time

            lifecycleScope.launch {
                for (image in detectionImages) {
//                    Log.d(fragmentTag, "Add detection image id ${image.arContentId}")
                    val imageBitmap = getBitmapFromURL(image.downloadUrl, requireContext())
                    imageBitmap?.let {
                        imageDatabase.addImage(image.arContentId, imageBitmap, imageWidthInMeters)
                    }
                }
                config.augmentedImageDatabase = imageDatabase
                session.configure(config)
            }
        }
    }

    private fun prepareImageAnchorContents() {
        val imageAnchorContents =
            getARContentByAnchorType(arContents, GrafARAnchorType.image_anchor)
        val imageAnchorContentItr = imageAnchorContents.iterator()
        while (imageAnchorContentItr.hasNext()) {
            val content = imageAnchorContentItr.next()
            when (content.arContentType) {
                GrafARContentType.model3d.toString() -> {
                    val modelNode = ArModelNode().apply {
                        loadModelAsync(
                            context = requireContext(),
                            glbFileLocation = content.downloadUrl,
                            lifecycle = lifecycle,
                            autoAnimate = true,
                        )
                    }
                    modelNode.id = content.arContentId
                    imageAnchorNodes.add(modelNode)
//                    Log.d(fragmentTag, "imageAnchorNodes model3d ${imageAnchorNodes.size}")
                }

                GrafARContentType.image.toString() -> {
                    val imageNode = ViewNode()
                    imageNode.id = content.arContentId
                    val imageView = ImageView(context)
                    imageView.load(content.downloadUrl) {
                        placeholder(R.drawable.empty)
                        allowHardware(false)
                    }

                    ViewRenderable.builder()
                        .setView(context, imageView)
                        .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                        .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                        .setSizer(FixedHeightViewSizer(1f))
                        .build(lifecycle)
                        .thenAccept { viewRenderable ->
                            imageNode.let {
//                                Log.d("$fragmentTag ViewRenderable", it.toString())
                                imageNode.setRenderable(viewRenderable)
                            }
                        }
                    imageAnchorNodes.add(imageNode)
//                    Log.d(fragmentTag, "imageAnchorNodes image ${imageAnchorNodes.size}")
                }
            }
        }
    }
//  Augmented Image End

    private fun startWorldAnchorRenderer() {
//        Log.d(fragmentTag, "startWorldAnchorRenderer")
//        val worldAnchorContents = getARContentByAnchorType(this.arContents, GrafARAnchorType.world_anchor)
//        if (initWorldAnchorContents.isEmpty()) {
//            stopLocationUpdates()
//        }

        val arContentItr = initWorldAnchorContents.iterator()
        while (arContentItr.hasNext()) {
            val obj = arContentItr.next()

            if (obj.arAnchorType == GrafARAnchorType.world_anchor.toString()) {
//                Log.d("$fragmentTag ARContent", obj.arContentId)

                val latLng = LatLng(obj.location2D.coordinates[1], obj.location2D.coordinates[0])
//                Log.d("$fragmentTag anchorObject location", latLng.toString())

                // Check distance
                val distance = currentLocation!!.latLng.sphericalDistance(latLng)

                if (distance <= obj.renderRadiusInMeter || obj.renderRadiusInMeter == 0) {
                    arContentItr.remove()

                    if (obj.arContentType == GrafARContentType.model3d.toString()) {
                        val cloudPosition = ARCloudPosition(obj.arContentId, latLng, obj.altitude)
                        val modelNode = ModelNode().apply {
                            loadModelAsync(
                                context = requireContext(),
                                glbFileLocation = obj.downloadUrl,
                                lifecycle = lifecycle,
                                autoAnimate = true,
                                scaleToUnits = obj.scale.x * 1.0f
                            )
                        }
//                        val accNode = nodes.filter { it.id == obj.arContentId }
//                Log.d("$fragmentTag accNode", accNode.toString())

                        modelNode.id = obj.arContentId
//                        modelNode.onTap = { _, _ ->
//                            if (accNode.isNotEmpty()) {
//                                accNode[0].onTouched?.invoke()
//                            }
//                        }

                        val posVec3 = cloudPosition.getPositionVector(
                            orientationAngles[0],
                            currentLocation!!.latLng
                        )

                        modelNode.position = Float3(posVec3.x, posVec3.y, posVec3.z)
                        sceneView.addChild(modelNode)
                    } else if (obj.arContentType == GrafARContentType.image.toString()) {
                        val cloudPosition = ARCloudPosition(obj.arContentId, latLng, obj.altitude - 5)
                        val imageNode = ViewNode()
                        val imageView = ImageView(context)
                        imageView.load(obj.downloadUrl) {
                            placeholder(R.drawable.empty)
                            allowHardware(false)
                        }

                        ViewRenderable.builder()
                            .setView(context, imageView)
                            .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                            .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                            .setSizer(FixedHeightViewSizer(obj.scale.x * 1))
                            .build(lifecycle)
                            .thenAccept { viewRenderable ->
                                imageNode.setRenderable(viewRenderable)
                            }

                        val posVec3 = cloudPosition.getPositionVector(
                            orientationAngles[0],
                            currentLocation!!.latLng
                        )
                        imageNode.position = Float3(posVec3.x, posVec3.y, posVec3.z)

//                        val accNode = nodes.filter { it.id == obj.arContentId }

                        imageNode.id = obj.arContentId
//                        imageNode.onTap = { _, _ ->
//                            if (accNode.isNotEmpty()) {
//                                accNode[0].onTouched?.invoke()
//                            }
//                        }
//                        Log.d(fragmentTag, "sceneView.addChild image")
                        sceneView.addChild(imageNode)
                    }
                }
            }
        }
    }

    private fun updateWorldAnchor() {
//        Log.d(fragmentTag, "updateWorldAnchor")
        val worldAnchorContents = getARContentByAnchorType(this.arContents, GrafARAnchorType.world_anchor)
//        Log.d(fragmentTag, "updateWorldAnchor ${worldAnchorContents.size}")
        for (node in sceneView.children) {
//            Log.d(fragmentTag, "updateWorldAnchor ${node.id}")
            for (content in worldAnchorContents) {
                if (node.id == content.arContentId) {
//                    Log.d(fragmentTag, "updateWorldAnchor ${content.arContentId}")
//                    val latLng = LatLng(content.location2D.coordinates[1], content.location2D.coordinates[0])
//                    val cloudPosition = ARCloudPosition(content.arContentId, latLng, content.altitude)
//                    val posVec3 = cloudPosition.getPositionVector(
//                        orientationAngles[0],
//                        currentLocation!!.latLng
//                    )
//                    node.position = Float3(posVec3.x, posVec3.y, posVec3.z)
                    if (content.isFrontFacing) {
                        node.transform(
                            position = node.position,
                            rotation = Rotation(node.rotation.x, sceneView.cameraNode.rotation.y, node.rotation.z),
                            scale = node.scale
                        )
                    }
                }
            }
        }
    }
    // Independent Functions Start
    private fun getARContentByAnchorType(
        arContents: MutableList<ARContent>,
        anchorType: GrafARAnchorType
    ): MutableList<ARContent> {
        val results = arContents.filter { it.arAnchorType == anchorType.toString() }
        return results as MutableList<ARContent>
    }

    private fun getARContentByContentType(
        arContents: MutableList<ARContent>,
        contentType: GrafARContentType
    ): MutableList<ARContent> {
        val results = arContents.filter { it.arContentType == contentType.toString() }
        return results as MutableList<ARContent>
    }

    private fun getARContentByContentTypeAndAnchorType(
        arContents: MutableList<ARContent>,
        contentType: GrafARContentType,
        anchorType: GrafARAnchorType
    ): MutableList<ARContent> {
        var results = arContents.filter { it.arContentType == contentType.toString() }
        results = results.filter { it.arAnchorType == anchorType.toString() }
        return results as MutableList<ARContent>
    }
// Independent Functions End

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationRequest.let {
            fusedLocationClient.requestLocationUpdates(
                it,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    // SENSOR START---------------------------------------------------------------------------------
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

//        Log.d("$fragmentTag orientationAngles[0]", orientationAngles[0].toString())
    }
// SENSOR END---------------------------------------------------------------------------------

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        sensorManager.unregisterListener(this)
        arMediaPlayer?.pause()
        arMediaPlayer = null
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
        arMediaPlayer?.release()
        arMediaPlayer = null
    }

    override fun onDetach() {
        super.onDetach()
        stopLocationUpdates()
        arMediaPlayer?.release()
        arMediaPlayer = null
    }
}

val Location.latLng: LatLng
    get() = LatLng(this.latitude, this.longitude)