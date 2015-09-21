package com.krld.flashlight;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class FlashlightActivity extends Activity {
    public static final int CROSSFADE_DURATION_MILLIS = 400;
    public static final int CROSSFADE_DURATION_SHORT = 0;
    private ImageButton onOffButton;
    private static Camera cam;
    private Drawable buttonOffImg;
    private Drawable buttonOnImg;
    private boolean systemWithoutFlash;
    private boolean isCameraOn;
    private Camera.Parameters parametersOn;
    private Camera.Parameters parametersOff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flashlight);
        init();
    }

    private void init() {

        onOffButton = (ImageButton) findViewById(R.id.onoff_button);
        onOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOnOffLight();
            }
        });

        buttonOffImg = getResources().getDrawable(R.drawable.hqicon_dark);
        buttonOnImg = getResources().getDrawable(R.drawable.hqicon);
    }

    private synchronized void turnOnOffLight() {

        if (isCameraOff()) {
            turnFlashOn();
            turnOnButtonImg();
        } else {
            turnFlashOff();
            turnOffButtonImg();
        }
    }

    private void turnOffButtonImg() {
        TransitionDrawable drawable = (TransitionDrawable) onOffButton.getDrawable();
        drawable.reverseTransition(CROSSFADE_DURATION_MILLIS);
    }

    private void turnOnButtonImg() {
        TransitionDrawable drawable = (TransitionDrawable) onOffButton.getDrawable();
        drawable.startTransition(CROSSFADE_DURATION_MILLIS);
    }

    private void turnFlashOff() {
        cam.setParameters(parametersOff);
        isCameraOn = false;
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

    private boolean isCameraOff() {
        return !isCameraOn;
    }

    private void errorMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private boolean systemHasFlashLight() {
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

    @Override
    protected void onResume() {
        super.onResume();
        systemWithoutFlash = !systemHasFlashLight();
        if (systemWithoutFlash) {
            errorMessage(getResources().getString(R.string.no_flash));
            return;
        }

        if (cam == null) {
            cam = Camera.open();

            parametersOn = cam.getParameters();
            parametersOn.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            parametersOff = cam.getParameters();
            parametersOff.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isCameraOn) {
            cam.release();
            cam = null;
        }
    }
}
