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
import android.widget.Toast;

import java.io.IOException;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class FlashlightActivity extends Activity {
    public static final int CROSSFADE_DURATION_MILLIS = 400;
    public static final int CROSSFADE_DURATION_SHORT = 0;
    private ImageButton onOffButton;
    private Drawable buttonOffImg;
    private Drawable buttonOnImg;
    private boolean systemWithoutFlash;

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
        if (systemWithoutFlash) {
            errorMessage(getResources().getString(R.string.no_flash));
            return;
        }

        boolean lightOn = Application.getInstance().toggleLight();
        if (lightOn) {
            turnOnButtonImg();
        } else {
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


    private void errorMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        systemWithoutFlash = !Application.getInstance().systemHasFlashLight();
        if (systemWithoutFlash) {
            errorMessage(getResources().getString(R.string.no_flash));
            return;
        }

        Exception exception = Application.getInstance().onResume();
        if (exception != null) {
            errorMessage(getString(R.string.failed_connect_to_camera));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Application.getInstance().onPause();
    }
}
