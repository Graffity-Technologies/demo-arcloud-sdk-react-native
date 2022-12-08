package com.graffity.arcloud.ar

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.graffity.android.arcloud.R
import kotlinx.android.parcel.Parcelize
import kotlin.system.exitProcess


//class AccessibleARNode(var id: String) {
//    var onTouched: (() -> Unit)? = null
//}

@Parcelize
class OnSceneTouchedListener(val onSceneTouchedAction: (nodeId: String?) -> Unit) : Parcelable {
    fun onSceneTouched(nodeId: String?) = onSceneTouchedAction(nodeId)
}

class ARCloudActivity : AppCompatActivity(R.layout.arcloud_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onSceneTouchedListener: OnSceneTouchedListener? = intent.getParcelableExtra("OnSceneTouchedListener")
        if (savedInstanceState == null) {
            val arCloudConfigFragment = ARCloudConfigFragment()
            arCloudConfigFragment.onSceneTouched = { nodeId ->
//                Log.d("ARCloudPrivateFragment", "onSceneTouched nodeId $nodeId")
                onSceneTouchedListener?.onSceneTouched(nodeId)
            }

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.arcloud_container_view, arCloudConfigFragment)
                .commit()

//            val bundle = Bundle().apply {
//                putParcelable("listenerOnSceneTouched", Listener(onSceneTouchedAction))
//            }
//
//            supportFragmentManager.commit {
//                setReorderingAllowed(true)
//                add<ARCloudConfigFragment>(R.id.arcloud_container_view, args = bundle)
//            }

//            supportFragmentManager.commit {
//                setReorderingAllowed(true)
//                add<ARCloudConfigFragment>(R.id.arcloud_container_view)
//            }
        }
//
//        val manager: FragmentManager = supportFragmentManager
//        val transaction: FragmentTransaction = manager.beginTransaction()
//        transaction.add(com.graffity.android.arcloud.R.id.arcloud_config, YOUR_FRAGMENT_NAME, YOUR_FRAGMENT_STRING_TAG)
//        transaction.addToBackStack(null)
//        transaction.commit()



//        childFragmentManager
//            .beginTransaction()
//            .replace(R.id.arcloud_public, arCloudConfigFragment)
//            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
        exitProcess(0)
    }

}