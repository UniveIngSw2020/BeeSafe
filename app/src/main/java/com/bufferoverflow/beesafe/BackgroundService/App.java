package com.bufferoverflow.beesafe.BackgroundService;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class App extends Application {

    private static boolean serviceActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        startService(this); //Start the service
    }

    public static void startService(Context c) {
        getMyAppsNotificationManager(c).registerNotificationChannelChannel("123",
                "Tracing Service",
                "Tracing Service Algorithm");

        Intent serviceIntent = new Intent(c, BackgroundScanWork.class);
        ContextCompat.startForegroundService(c,serviceIntent);
        serviceActive = true;
        Toast toast = Toast. makeText(c, "Service Started", Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void stopService(Context c) {
        c.stopService(new Intent(c, BackgroundScanWork.class));
        serviceActive = false;
        Toast toast = Toast. makeText(c, "Service Stopped", Toast.LENGTH_SHORT);
        toast.show();
    }

    public static boolean isServiceActive() {
        return serviceActive;
    }

    public static AppPersistentNotificationManager getMyAppsNotificationManager(Context c){
        return AppPersistentNotificationManager.getInstance(c);
    }

}
