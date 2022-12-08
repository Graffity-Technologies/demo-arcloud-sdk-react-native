package com.graffity.arcloud.ar

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.graffity.android.arcloud.R
import com.graffity.android.arcloud.databinding.FragmentArcloudConfigBinding
import com.graffity.arcloud.ApiStatus
import com.graffity.arcloud.sdkservice.ARContent
import com.graffity.arcloud.sdkservice.AccessTokenViewModel
import com.graffity.arcloud.sdkservice.AccessTokenViewModelFactory
import com.graffity.arcloud.sdkservice.ARContentViewModel
import com.graffity.arcloud.sdkservice.AnchorObjectViewModelFactory
import com.graffity.arcloud.view.ErrorLandingFragment
import java.util.*
import kotlin.concurrent.schedule

internal class ARCloudConfigFragment : Fragment() {
    private val fragmentTag = "ARCloudConfigFragment"
    private var _binding: FragmentArcloudConfigBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var isAccessTokenDone = false

    var nodes = ArrayList<AccessibleARNode>()
    var onSceneTouched: ((nodeId: String?) -> Unit)? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inputToken = requireActivity().getString(R.string.GRAFFITY_SECRET_TOKEN)
//        Log.d("GRAFFITY_SECRET_TOKEN", inputToken)

        _binding = FragmentArcloudConfigBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.arCloudConfigTextViewDesc
        textView.text = "Checking your location..."

        if (!isSupportedDevice()) {
            Log.e(tag, "Device is not supported")
            childFragmentManager
                .beginTransaction()
                .replace(
                    R.id.arcloud_config,
                    ErrorLandingFragment()
                ) // "AR does not support on your device."
                .disallowAddToBackStack()
                .commit()
        } else {
            requestPermissions { success ->
                if (success) {
                    val sharedAccessTokenViewModel: AccessTokenViewModel by activityViewModels {
                        AccessTokenViewModelFactory(inputToken)
                    }

                    val arCloudPrivateFragment = ARCloudPrivateFragment()
                    arCloudPrivateFragment.nodes = nodes
                    arCloudPrivateFragment.onSceneTouched = onSceneTouched

                    sharedAccessTokenViewModel.status.observe(viewLifecycleOwner) { status ->
                        when (status) {
                            ApiStatus.LOADING -> Log.d(fragmentTag, "ApiStatus.LOADING")
                            ApiStatus.ERROR -> Log.d(fragmentTag, "ApiStatus.ERROR")
                            ApiStatus.DONE -> {
                                Log.d(fragmentTag, "ApiStatus.DONE")
                                sharedAccessTokenViewModel.data.observe(viewLifecycleOwner) { token ->
//                                    Log.d(fragmentTag, "AccessToken $token")

                                    val anchorObjectViewModelFactory = AnchorObjectViewModelFactory(token)

                                    val arContentViewModel = anchorObjectViewModelFactory.let {
                                        ViewModelProvider(
                                            this,
                                            it
                                        )[ARContentViewModel::class.java]
                                    }

                                    arContentViewModel.data.observe(viewLifecycleOwner) {
//                                        Log.d(fragmentTag, "Task arContents $it")

                                        var arContents = it as? MutableList<ARContent>
                                        if (arContents == null) {
                                            arContents = mutableListOf()
                                        }
                                        Log.d(
                                            fragmentTag,
                                            "Loaded arContents ${arContents.size}"
                                        )

                                        arCloudPrivateFragment.arContents = arContents
                                        Timer("ChangeFragment", false).schedule(250) {
//                                            Log.d(fragmentTag, "Change view")
                                            childFragmentManager
                                                .beginTransaction()
                                                .replace(
                                                    R.id.arcloud_config,
                                                    arCloudPrivateFragment
                                                )
                                                .disallowAddToBackStack()
                                                .commit()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val userToken: String = requireActivity().getString(R.string.GRAFFITY_SECRET_TOKEN)
//        Log.d(fragmentTag, userToken)
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isSupportedDevice(): Boolean {
        val activityManager =
            requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val openGlVersionString = activityManager.deviceConfigurationInfo.glEsVersion
        if (openGlVersionString.toDouble() < 3.0) {
            Log.e(tag, "Library requires OpenGL ES 3.0 or later")
            return false
        }
        return true
    }

    private fun requestPermissions(onSuccess: (Boolean) -> Unit) {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("requestPermissions", "Precise location access granted")

                    onSuccess(true)
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("requestPermissions", "Only approximate location access granted")
                }
                permissions.getOrDefault(Manifest.permission.CAMERA, false) -> {
                    Log.d("requestPermissions", "Camera access granted")
                }
                else -> {
                    Log.e(tag, "No permission access granted.")
                    onSuccess(false)
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
            )
        )
    }
}