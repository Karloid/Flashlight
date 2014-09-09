package com.krld.flashlight;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.io.IOException;

public class FlashlightActivity extends Activity {
    public static final int CROSSFADE_DURATION_MILLIS = 400;
    public static final int CROSSFADE_DURATION_SHORT = 0;
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

    private synchronized void turnOnOffLight() {
        if (!systemHasFlashLight()) {
            errorMessage("No flashlight");
        }
        if (isCameraOff()) {
            Thread tmpThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        turnFlashOn();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            // dirt hack wait until flash turn on
            tmpThread.start();
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        cam.stopPreview();
        cam.release();
        cam = null;
    }

    private synchronized void turnFlashOn() throws IOException {
        boolean notConnected = true;
        int count = 0;
        int maxCount = 10;
        while (notConnected) {
            try {
                count++;
                if (count > maxCount) {
                    return;
                }
                cam = Camera.open();
            } catch (Exception e) {
                try {
                    Thread.sleep(50);
                    continue;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            notConnected = false;
        }
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
        // updateButtonDrawable();
    }

    private void updateButtonDrawable() {
        TransitionDrawable drawable = (TransitionDrawable) onOffButton.getDrawable();
        if (isCameraOff()) {
            drawable.reverseTransition(CROSSFADE_DURATION_SHORT);
        } else {
            drawable.startTransition(CROSSFADE_DURATION_SHORT);
        }
    }
}
