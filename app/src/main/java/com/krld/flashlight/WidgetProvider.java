package com.krld.flashlight;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.widget.RemoteViews;

/**
 * Created by krld_2 on 28.10.2014.
 */
public class WidgetProvider extends AppWidgetProvider {
    private static final String ACTION_CLICKED = "action clicked";
    private static Camera cam;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        boolean cameraOff = Application.getInstance().isCameraOff();
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), cameraOff ? R.layout.widget_off : R.layout.widget_on);
        Intent active = new Intent(context, WidgetProvider.class);
        active.setAction(ACTION_CLICKED);

        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);

        remoteViews.setOnClickPendingIntent(android.R.id.icon, actionPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if (action.equals(ACTION_CLICKED)) {
            //TODO
            Application.getInstance().toggleLight();
            onUpdate(context, AppWidgetManager.getInstance(context), new int[]{0});
        }
        //   toggleFlash();
        super.onReceive(context, intent);
    }
}
