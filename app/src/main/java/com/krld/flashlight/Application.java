package com.krld.flashlight;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.widget.ImageButton;

import java.io.IOException;
import java.util.List;

/**
 * Created by brown fox on 22.09.2015.
 */
public class Application extends android.app.Application {

    private static Camera cam;
    private boolean isCameraOn = false;
    private Camera.Parameters parametersOn;
    private Camera.Parameters parametersOff;
    private boolean systemHasFlashLight;
    private boolean activityForeground;

    public static Application getInstance() {
        return instance;
    }

    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
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
            turnFlashOn();
            return true;
        } else {
            turnFlashOff();
            return false;
        }
    }

    private void turnFlashOff() {
        cam.setParameters(parametersOff);
        isCameraOn = false;
        if (!activityForeground) {
            onPause();
        }
    }

    private synchronized void turnFlashOn() {
        if (Build.VERSION.SDK_INT >= 11) {     //honeycomb req for nexus 5
            try {
                cam.setPreviewTexture(new SurfaceTexture(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cam.setParameters(parametersOn);
        isCameraOn = true;
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
}
