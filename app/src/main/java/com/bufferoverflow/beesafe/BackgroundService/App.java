package com.bufferoverflow.beesafe.BackgroundService;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bufferoverflow.beesafe.MainActivity;
import com.bufferoverflow.beesafe.User;

public class App extends Application {

    private static boolean serviceActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        User.getInstance(this);
        //startService(this); //Start the service
    }

    public static void startService(Context c) {
        getMyAppsNotificationManager(c).registerNotificationChannelChannel("123",
                "Tracing Service",
                "Tracing Service Algorithm");

        Intent serviceIntent = new Intent(c, BackgroundScanWork.class);
        ContextCompat.startForegroundService(c,serviceIntent);
        serviceActive = true;
        User.getInstance(c).enableCrowdEventListeners(c); //Enables the notification for crowd event listeners
        Toast toast = Toast. makeText(c, "Service Started", Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void stopService(Context c) {
        c.stopService(new Intent(c, BackgroundScanWork.class));
        serviceActive = false;
        User.getInstance(c).disableCrowdEventListeners(); //Disables the notification for crowd event listeners
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
