package com.reactnativesdkbridge;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.graffity.arcloud.ar.ARCloudActivity;

public class GraffityAndroidModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;
    private Intent intent;

    GraffityAndroidModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "GraffityAndroidModule";
    }

    @ReactMethod
    public void openARActivity() {
        intent = new Intent(reactContext, ARCloudActivity.class);

        intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK));
        reactContext.startActivity(intent);
    }
}
