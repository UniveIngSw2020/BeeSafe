package com.bufferoverflow.beesafe.BackgroundService;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class App extends Application {

    private static AppPersistentNotificationManager notificationManager;
    private static Intent serviceIntent;
    private static boolean serviceActive = false;

    public static boolean isServiceActive() {
        return serviceActive;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startService(this);
    }

    public static void startService(Context c) {
        notificationManager = AppPersistentNotificationManager.getInstance(c);
        notificationManager.registerNotificationChannelChannel("123",
                "BackgroundService",
                "BackgroundService");

        serviceIntent = new Intent(c, BackgroundScanWork.class);
        ContextCompat.startForegroundService(c,serviceIntent);
        serviceActive = true;
        Toast toast = Toast. makeText(c, "Service Started", Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void stopService(Context c) {
        c.stopService(new Intent(c, BackgroundScanWork.class));
        serviceActive = false;
        Toast toast = Toast. makeText(c, "Service Stoped", Toast.LENGTH_SHORT);
        toast.show();
    }


    public static AppPersistentNotificationManager getMyAppsNotificationManager(){
        return notificationManager;
    }

}
