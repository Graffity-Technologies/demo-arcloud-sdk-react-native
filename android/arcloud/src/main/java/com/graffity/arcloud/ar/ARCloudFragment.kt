package com.graffity.arcloud.ar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.ar.sceneform.PickHitResult
import com.graffity.android.arcloud.R
import com.graffity.android.arcloud.databinding.FragmentArcloudPublicBinding
import io.github.sceneview.node.Node

class AccessibleARNode(var id: String) {
    var onTouched: (() -> Unit)? = null
}

class ARCloudFragment : Fragment() {
    private var _binding: FragmentArcloudPublicBinding? = null
    private val binding get() = _binding!!

    var nodes = ArrayList<AccessibleARNode>()
    var onSceneTouched: ((nodeId: String?) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArcloudPublicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arCloudConfigFragment = ARCloudConfigFragment()
        arCloudConfigFragment.nodes = nodes
        arCloudConfigFragment.onSceneTouched = onSceneTouched

        childFragmentManager
            .beginTransaction()
            .replace(R.id.arcloud_public, arCloudConfigFragment)
            .commit()
    }
}