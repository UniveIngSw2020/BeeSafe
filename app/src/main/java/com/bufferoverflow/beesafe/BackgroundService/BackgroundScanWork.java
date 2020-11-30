package com.bufferoverflow.beesafe.BackgroundService;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bufferoverflow.beesafe.MapsActivity;
import com.bufferoverflow.beesafe.R;
import com.bufferoverflow.beesafe.Scan;

import java.util.concurrent.CountDownLatch;


public class BackgroundScanWork extends IntentService {

    private boolean activeService = true;
    private static int SCAN_TIME_MINUTES = 15;

    public BackgroundScanWork(){
        super(BackgroundScanWork.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Scan.scanLatch = new CountDownLatch(1);

        startForeground(1, getNotification());
        //Profile.getInstance().activateFavoritePlaceListeners(); //Activate the listeners for saved locations (To get notifications)
        tracingServiceLoop();
    }

    private void tracingServiceLoop () {
        while (activeService){
            try{
                if(activeService){
                    Log.d("TRACING", "About to call the tracing algorithm");
                    Scan.tracingAlgorithm(this);
                    Log.d("TRACING", "Tracing algorithm finished.");
                }
                Thread.sleep(30000);
            }catch (InterruptedException e){
                Log.i("TRACING","Thread Interrupted");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activeService=false;
        Log.i("Destroy", ", thread Id: "+Thread.currentThread().getId());
    }

    private Notification getNotification(){
        return App.getMyAppsNotificationManager().getNotification(MapsActivity.class,
                "BeeSafe Tracing Algorithm is Active",
                1,
                false,
                1);
    }


}
