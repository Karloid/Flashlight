package com.krld.flashlight;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class FlashlightActivity extends Activity {
    private ImageButton onOffButton;
    private static Camera cam;

    /**
     * Called when the activity is first created.
     */
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
    }

    private void turnOnOffLight() {
        if (!systemHasFlashLight()) {
            errorMessage("No flashlight");
        }
        if (isCameraOff()) {
            turnCameraOn();
        } else {
            turnCameraOff();
        }
    }

    private void turnCameraOff() {
        cam.stopPreview();
        cam.release();
        cam = null;
    }

    private void turnCameraOn() {
        cam = Camera.open();
        Camera.Parameters p = cam.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cam.setParameters(p);
        cam.startPreview();
    }

    private boolean isCameraOff() {
        return cam == null;
    }

    private void errorMessage(String message) {
         //TODO
    }

    private boolean systemHasFlashLight() {
        //TODO
        return true;
    }
}
