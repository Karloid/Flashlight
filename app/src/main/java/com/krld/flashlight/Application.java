package com.krld.flashlight;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Created by brown fox on 22.09.2015.
 */
public class Application extends android.app.Application {

    public static final String PREFS_INSTANT_MODE = "PREFS_INSTANT_MODE";
    public static final String PREFS_NAME = "WHATEVER";
    private static Camera cam;
    private boolean isCameraOn = false;
    private Camera.Parameters parametersOn;
    private Camera.Parameters parametersOff;
    private boolean systemHasFlashLight;
    private boolean activityForeground;
    private boolean instantMode;

    public static Application getInstance() {
        return instance;
    }

    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        instantMode = getBooleanFromSharedPrefs(PREFS_INSTANT_MODE);
        systemHasFlashLight = systemHasFlashLight();
    }

    public boolean systemHasFlashLight() {
        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e("err", "Device has no camera!");
            return false;
        }
        try {
            Camera camera = Camera.open();
            if (camera == null) {
                return false;
            }
            Camera.Parameters parameters = camera.getParameters();

            if (parameters.getFlashMode() == null) {
                camera.release();
                return false;
            }

            List<String> supportedFlashModes = parameters.getSupportedFlashModes();
            if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1
                    && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
                camera.release();
                return false;
            }
            camera.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean toggleLight() {
        if (!systemHasFlashLight) return false;
        Exception exception = onResume();
        if (exception != null) {
            //TODO
            return false;
        }
        if (isCameraOff()) {
            return turnFlashOn();
        } else {
            return turnFlashOff();
        }
    }

    private Boolean turnFlashOff() {
        try {
            cam.setParameters(parametersOff);
            if (!instantMode)
                cam.stopPreview();
            isCameraOn = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!activityForeground) {
            onPause();
        }
        return isCameraOn;
    }

    private synchronized Boolean turnFlashOn() {
        if (Build.VERSION.SDK_INT >= 11) {     //honeycomb req for nexus 5
            try {
                cam.setPreviewTexture(new SurfaceTexture(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            cam.setParameters(parametersOn);
            if (!instantMode)
                cam.startPreview();
            isCameraOn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isCameraOn;
    }

    public boolean isCameraOff() {
        return !isCameraOn;
    }

    public Exception onResume() {
        if (!systemHasFlashLight) return null;
        if (cam == null) {
            try {
                cam = Camera.open();

                parametersOn = cam.getParameters();
                parametersOn.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

                parametersOff = cam.getParameters();
                parametersOff.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
        }
        return null;
    }

    public void onPause() {
        activityForeground = false;
        if (!isCameraOn && cam != null) {
            cam.release();
            cam = null;
        }
    }

    public void toggleLightFromActivity() {
        toggleLight();
        Intent updateIntent = new Intent(this, WidgetProvider.class);
        updateIntent.setAction(WidgetProvider.ACTION_UPDATE);
        sendBroadcast(updateIntent);
    }

    public boolean getSystemHasFlashLight() {
        return systemHasFlashLight;
    }

    public Exception activityOnResume() {
        activityForeground = true;
        return onResume();
    }

    public boolean getInstantMode() {
        return instantMode;
    }

    public boolean isInstantMode() {
        return instantMode;
    }

    public void handleNewInstantMode(boolean active) {
        if (isCameraOn) {
            toggleLightFromActivity();
        }
        saveToSharedPrefs(PREFS_INSTANT_MODE, active);
        instantMode = active;
    }

    private void saveToSharedPrefs(String key, boolean value) {
        SharedPreferences settings;
        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private boolean getBooleanFromSharedPrefs(String key) {
        SharedPreferences settings;
        settings = getSharedPreferences("WHATEVER", Context.MODE_PRIVATE);
        return settings.getBoolean(key, false);
    }
}
