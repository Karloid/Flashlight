package com.krld.flashlight;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by krld_2 on 28.10.2014.
 */
public class WidgetProvider extends AppWidgetProvider {
    private static final String ACTION_WIDGET_RECEIVER = "actionWidgetAction";
    private static Camera cam;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Создаем новый RemoteViews
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.initial_widget);

        //Подготавливаем Intent для Broadcast
        Intent active = new Intent(context, WidgetProvider.class);
        active.setAction(ACTION_WIDGET_RECEIVER);
        active.putExtra("msg", "Hello Habrahabr");

        //создаем наше событие
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);

        //регистрируем наше событие
        remoteViews.setOnClickPendingIntent(android.R.id.icon, actionPendingIntent);

        //обновляем виджет
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //Ловим наш Broadcast, проверяем и выводим сообщение
        final String action = intent.getAction();
        toggleFlash();
        super.onReceive(context, intent);
    }

    private void toggleFlash() {
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
            try {
                cam.setPreviewTexture(new SurfaceTexture(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Camera.Parameters p = cam.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cam.setParameters(p);
        cam.startPreview();
    }
}
