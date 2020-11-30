package com.bufferoverflow.beesafe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.bufferoverflow.beesafe.AuxTools.AuxDateTime;
import com.bufferoverflow.beesafe.BackgroundService.AppPersistentNotificationManager;
import com.bufferoverflow.beesafe.BackgroundService.BackgroundScanWork;
import com.clj.fastble.BleManager;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
TODO:
   Listeners for saved places
   Privacy Policy
   Help
   Service Stop/Start Notification + Buttons
   Service Notification Change for current place (offline) [nr devices using only bluetooth]
   Register broadcast listener for bluetooht/gps on off


 */

public class MainActivity extends AppCompatActivity {

    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BleManager.getInstance().init(getApplication());

    }

    //Open Map
    public void openMap (View view) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    //Starting Foreground Service
    public void startService(View view) {
        serviceIntent = new Intent(getApplicationContext(), BackgroundScanWork.class);
        ContextCompat.startForegroundService(this,serviceIntent);
    }

    public void stopService(View view) {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.d("TRACING", String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));

//        AppPersistentNotificationManager ap = AppPersistentNotificationManager.getInstance(this);
//        ap.updateNotification(AuxDateTime.dateToString(AuxDateTime.currentTime()), "XXX");
        //getApplicationContext().stopService(serviceIntent);
    }

    public void stopSer() {
        stopService(new Intent(getApplicationContext(), BackgroundScanWork.class));
    }
    public void startSer() {
        serviceIntent = new Intent(getApplicationContext(), BackgroundScanWork.class);
        ContextCompat.startForegroundService(this,serviceIntent);
    }

    public void changeLanguage(View view) {
        List<String> l = new ArrayList<>();
        l.add("en");
        l.add("sq");

        new LovelyChoiceDialog(this)
                .setTopColorRes(R.color.colorPrimary)
                .setTitle("Language")
                .setIcon(R.drawable.amu_bubble_mask)
                .setMessage("Change language")
                .setItems(l, new LovelyChoiceDialog.OnItemSelectedListener<String>() {
                    @Override
                    public void onItemSelected(int i, String s) {

                    }
                })
                .show();
    }

}