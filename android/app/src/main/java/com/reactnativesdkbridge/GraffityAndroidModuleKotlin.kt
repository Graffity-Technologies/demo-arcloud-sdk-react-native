package com.reactnativesdkbridge

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import android.support.v4.os.IResultReceiver.Default
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.graffity.arcloud.ar.ARCloudActivity
import com.graffity.arcloud.ar.OnSceneTouchedListener
import kotlinx.parcelize.Parcelize
import java.io.Serializable


class GraffityAndroidModuleKotlin(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), Serializable {

//    private var touchedNodeId: String = "no_id"
//    private var touchedPromise: Promise? = null

//    @Parcelize
//    class TouchedListener(val nodeId: String, val reactContext: ReactApplicationContext) : Parcelable {
//        init {
//
//        }
//    }
    
//    private val activityEventListener =
//        object : BaseActivityEventListener() {
//            override fun onActivityResult(
//                activity: Activity?,
//                requestCode: Int,
//                resultCode: Int,
//                intent: Intent?
//            ) {
//                Log.d("onActivityResult", "onActivityResult 1")
////                sendToReactNative()
//                if (requestCode == IMAGE_PICKER_REQUEST) {
//                    touchedPromise?.let { promise ->
//                        when (resultCode) {
////                            Activity.RESULT_CANCELED ->
////                                promise.reject("CANCEL", "Activity was cancelled")
////                            Activity.RESULT_OK -> {
////                                promise.resolve(touchedNodeId)
////                            }
//
//                            else -> {
//                                Log.d("onActivityResult", "onActivityResult 2")
//                                promise.resolve(touchedNodeId)
//                            }
//                        }
//                        Log.d("onActivityResult", "onActivityResult 3")
////                        promise.resolve(touchedNodeId)
//
//                        touchedPromise = null
//                    }
//                }
//            }
//        }
//
//    init {
//        reactContext.addActivityEventListener(activityEventListener)
//    }

    override fun getName() = "GraffityAndroidModule"

    private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }
//
//    @ReactMethod
//    fun addListener(eventName: String) {
//        Log.d("addListener", "addListener")
//    }
//
//    @ReactMethod
//    fun removeListeners(count: Int) {
//        Log.d("removeListeners", "removeListeners")
//    }

//    private val onSceneTouchedListener: OnSceneTouchedListener
//        get() {
//            sendToReactNative()
//            val temp = OnSceneTouchedListener(onSceneTouchedAction)
//            return temp
//        }


//    private val onSceneTouchedAction: ((nodeId: String?) -> Unit) = { nodeId ->
//        Log.d("SomeFragment", "onSceneTouched nodeId $nodeId")
//
//        if (nodeId != null) {
////            sendToReactNative()
////            touchedNodeId = nodeId
//        }
//    }

//    val sendToReactNative = { // msg: String
//        val params = Arguments.createMap().apply {
//            putString("msg", "msg")
//        }
//        sendEvent(reactContext, "message", params)
//    }
//
//    @ReactMethod
//    fun getTouchedNodeId(promise: Promise) {
//        promise.resolve(touchedNodeId)
//    }
//
//    @ReactMethod
//    fun openARFragment(promise: Promise) {
//        Log.d("openARFragment", "openARFragment")
//
//    }

    @ReactMethod
    fun openARActivity(promise: Promise) {
        Log.d("openARActivity", "openARActivity")
        val intent = Intent(reactContext, EmptyActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        intent.putExtra("OnSceneTouchedListener", OnSceneTouchedListener(onSceneTouchedAction))
//        intent.putExtra("OnReactNativeListener", OnReactNativeListener(sendToReactNative))
        reactContext.startActivity(intent)

//        val activity = currentActivity
//        if (activity == null) {
//            promise.reject("ERROR", "Activity doesn't exist")
//            return
//        }
//
//        touchedPromise = promise
//        val intent = Intent(reactContext, ARCloudActivity::class.java)
//        intent.putExtra("OnSceneTouchedListener", OnSceneTouchedListener(onSceneTouchedAction))
//        activity.startActivityForResult(intent, IMAGE_PICKER_REQUEST)
    }

//    companion object {
//        const val IMAGE_PICKER_REQUEST = 1
//        const val E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST"
//        const val E_PICKER_CANCELLED = "E_PICKER_CANCELLED"
//        const val E_FAILED_TO_SHOW_PICKER = "E_FAILED_TO_SHOW_PICKER"
//        const val E_NO_IMAGE_DATA_FOUND = "E_NO_IMAGE_DATA_FOUND"
//    }
}