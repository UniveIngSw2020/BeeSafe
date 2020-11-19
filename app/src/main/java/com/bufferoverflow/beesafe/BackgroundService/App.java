package com.bufferoverflow.beesafe.BackgroundService;

import android.app.Application;

import com.bufferoverflow.beesafe.BackgroundService.AppPersistentNotificationManager;

public class App extends Application {

    private static AppPersistentNotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = AppPersistentNotificationManager.getInstance(this);
        notificationManager.registerNotificationChannelChannel("123",
                "BackgroundService",
                "BackgroundService");
    }

    public static AppPersistentNotificationManager getMyAppsNotificationManager(){
        return notificationManager;
    }

}
