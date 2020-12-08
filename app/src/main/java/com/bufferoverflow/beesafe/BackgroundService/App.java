package com.bufferoverflow.beesafe.BackgroundService;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bufferoverflow.beesafe.User;

public class App extends Application {

    public static final String SERVICE_CHANNEL_ID = "123";
    private static boolean serviceActive = false;

    /* Starts the service */
    public static void startService(Context c) {
        getMyAppsNotificationManager(c).registerNotificationChannelChannel(SERVICE_CHANNEL_ID, "Tracing Service", "Tracing");
        Intent serviceIntent = new Intent(c, BackgroundScanWork.class);
        ContextCompat.startForegroundService(c, serviceIntent);
        serviceActive = true;
        User.getInstance(c).enableAllCrowdEventListeners(c); //Enables the notification for crowd event listeners
        Toast toast = Toast.makeText(c, "Service Started", Toast.LENGTH_SHORT);
        toast.show();
    }

    /* Stops the service */
    public static void stopService(Context c) {
        c.stopService(new Intent(c, BackgroundScanWork.class));
        serviceActive = false;
        User.getInstance(c).disableAllCrowdEventListeners(); //Disables the notification for crowd event listeners
        Toast toast = Toast.makeText(c, "Service Stopped", Toast.LENGTH_SHORT);
        toast.show();
    }

    public static boolean isServiceActive() {
        return serviceActive;
    }

    public static AppPersistentNotificationManager getMyAppsNotificationManager(Context c) {
        return AppPersistentNotificationManager.getInstance(c);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
