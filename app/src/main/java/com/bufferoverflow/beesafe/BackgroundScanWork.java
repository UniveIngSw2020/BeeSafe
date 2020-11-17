package com.bufferoverflow.beesafe;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.awareness.snapshot.DetectedActivityResponse;
import com.google.android.gms.location.DetectedActivity;
import androidx.annotation.Nullable;

import com.clj.fastble.BleManager;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.awareness.Awareness;

import java.util.ArrayList;


public class BackgroundScanWork extends IntentService {

    private boolean activeService = true;

    private static int SCAN_TIME_MINUTES = 15;

    public BackgroundScanWork(){
        super(BackgroundScanWork.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        startForeground(1, getNotification());
        tracingServiceLoop();
    }

    private void tracingServiceLoop () {
        while (activeService){
            try{
                Thread.sleep(30000);
                if(activeService){
                    Scan scan = new Scan(this);
                    Log.d("TRACING", "About to call the tracing algorithm");
                    scan.tracingAlgorithm();
                    Log.d("TRACING", "Tracing algorithm finished. FOUND " + scan.getDevicesNumber() + " DEVICES");
                    //Log.i("Tracing","Thread id: "+Thread.currentThread().getId());
                }
            }catch (InterruptedException e){
                Log.i("Tracing","Thread Interrupted");
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
        return App.getMyAppsNotificationManager().getNotification(MainActivity.class,
                "BackgroundService running",
                1,
                false,
                1);
    }

}
