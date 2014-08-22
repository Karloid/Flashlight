package com.krld.flashlight;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.io.IOException;

public class FlashlightActivity extends Activity {
    private ImageButton onOffButton;
    private static Camera cam;
    private Drawable buttonOffImg;
    private Drawable buttonOnImg;

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

        buttonOffImg = getResources().getDrawable(R.drawable.hqicon_dark);
        buttonOnImg = getResources().getDrawable(R.drawable.hqicon);
    }

    private void turnOnOffLight() {
        if (!systemHasFlashLight()) {
            errorMessage("No flashlight");
        }
        if (isCameraOff()) {
            turnOnButtonImg();
            Thread tmpThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        turnCameraOn();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            tmpThread.start();

        } else {
            turnCameraOff();
            updateButtonBackground();
        }

    }

    private void turnOffButtonImg() {
        onOffButton.setBackground(buttonOffImg);
    }

    private void turnOnButtonImg() {
        onOffButton.setBackground(buttonOnImg);
    }

    private void turnCameraOff() {
        cam.stopPreview();
        cam.release();
        cam = null;
    }

    private void turnCameraOn() throws IOException {
        cam = Camera.open();

        if (Build.VERSION.SDK_INT >= 11) {     //honeycomb req for nexus 5
            cam.setPreviewTexture(new SurfaceTexture(0));
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonBackground();
    }

    private void updateButtonBackground() {
        Drawable drawableImage;
        if (isCameraOff()) {
            drawableImage = buttonOffImg;
        } else {
            drawableImage = buttonOnImg;
        }
        onOffButton.setBackground(drawableImage);
    }
}
