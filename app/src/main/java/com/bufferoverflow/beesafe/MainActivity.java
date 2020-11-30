package com.bufferoverflow.beesafe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
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

        HashMap<String, Integer> hash = new HashMap<>();
        hash.put("one", 1);
        hash.put("two", 2);
        Log.d("ANOTHER", "before");
        for(Integer val : hash.values())
            Log.d("ANOTHER", String.valueOf(val));
        modify(hash);
        Log.d("ANOTHER", "after");
        for(Integer val : hash.values())
            Log.d("ANOTHER", String.valueOf(val));

//        AppPersistentNotificationManager ap = AppPersistentNotificationManager.getInstance(this);
//        ap.updateNotification(AuxDateTime.dateToString(AuxDateTime.currentTime()), "XXX");
        //getApplicationContext().stopService(serviceIntent);
    }

    private void modify(HashMap<String, Integer> hash) {
        hash.put("three", 3);
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