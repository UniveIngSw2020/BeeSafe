package com.bufferoverflow.beesafe.BackgroundService;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.bufferoverflow.beesafe.BackgroundService.AppPersistentNotificationManager;

public class App extends Application {

    private static AppPersistentNotificationManager notificationManager;
    private Intent serviceIntent;

    @Override
    public void onCreate() {

        Log.d("XXXXXXXXXXX","App ID " + Thread.currentThread().getId() );


        super.onCreate();
        notificationManager = AppPersistentNotificationManager.getInstance(this);
        notificationManager.registerNotificationChannelChannel("123",
                "BackgroundService",
                "BackgroundService");

        serviceIntent = new Intent(getApplicationContext(), BackgroundScanWork.class);
        ContextCompat.startForegroundService(this,serviceIntent);
    }

    public static AppPersistentNotificationManager getMyAppsNotificationManager(){
        return notificationManager;
    }

}
