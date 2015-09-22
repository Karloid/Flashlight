package com.krld.flashlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class FlashlightActivity extends Activity {
    public static final int CROSSFADE_DURATION_MILLIS = 400;
    public static final int CROSSFADE_DURATION_SHORT = 0;
    private ImageButton onOffButton;
    private boolean systemWithoutFlash;
    private boolean lastStateIsOn;

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
        ImageButton imageButton = (ImageButton) findViewById(R.id.settings_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsDialog();
            }
        });
    }

    private void showSettingsDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.settings_d, null);
        new AlertDialog.Builder(this)
                .setView(view)
                .show();
    }

    private synchronized void turnOnOffLight() {
        if (systemWithoutFlash) {
            errorMessage(getResources().getString(R.string.no_flash));
            return;
        }
        Application.getInstance().toggleLightFromActivity();
        syncButtons(true);
    }

    private void syncButtons(boolean withAnimation) {
        boolean lightOn = !Application.getInstance().isCameraOff();
        if (lightOn) {
            turnOnButtonImg(withAnimation);
        } else {
            turnOffButtonImg(withAnimation);
        }
    }

    private void turnOffButtonImg(boolean withAnimation) {
        if (!lastStateIsOn) return;
        TransitionDrawable drawable = (TransitionDrawable) onOffButton.getDrawable();
        if (!withAnimation) {
            drawable.resetTransition();
        } else {
            drawable.reverseTransition(CROSSFADE_DURATION_MILLIS);
        }
        lastStateIsOn = false;
    }

    private void turnOnButtonImg(boolean withAnimation) {
        if (lastStateIsOn) return;
        TransitionDrawable drawable = (TransitionDrawable) onOffButton.getDrawable();
        drawable.startTransition(withAnimation ? CROSSFADE_DURATION_MILLIS : 0);
        lastStateIsOn = true;
    }


    private void errorMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        systemWithoutFlash = !Application.getInstance().getSystemHasFlashLight();
        if (systemWithoutFlash) {
            errorMessage(getResources().getString(R.string.no_flash));
            return;
        }

        Exception exception = Application.getInstance().activityOnResume();
        if (exception != null) {
            errorMessage(getString(R.string.failed_connect_to_camera));
        }

        syncButtons(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Application.getInstance().onPause();
    }
}
