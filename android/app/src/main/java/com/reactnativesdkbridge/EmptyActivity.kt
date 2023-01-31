package com.reactnativesdkbridge;

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Process
import android.util.Log
import androidx.activity.OnBackPressedCallback
import com.graffity.arcloud.ar.ARCloudActivity
import com.graffity.arcloud.ar.ARCloudFragment
import com.graffity.arcloud.ar.OnSceneTouchedListener
import kotlin.system.exitProcess

//public class EmptyActivity extends AppCompatActivity {
//
//    private val arcloud = ARCloudFragment()
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_empty);
//
//
//    }
//}

class EmptyActivity : AppCompatActivity() {

    private val arcloud = ARCloudFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                exitProcess(Process.myPid())
            }
        }
        this.onBackPressedDispatcher.addCallback(
            this,
            callback
        )

        arcloud.onSceneTouched = { nodeId ->
            Log.d("ARCloudPrivateFragment", "onSceneTouched nodeId $nodeId")
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.empty, arcloud)
            .commit()
    }

//    override fun onBackPressed() {
//        finish()
//
//    }
}