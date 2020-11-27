package com.bufferoverflow.beesafe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bufferoverflow.beesafe.BackgroundService.BackgroundScanWork;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    public void StopService(View view) {
        getApplicationContext().stopService(serviceIntent);
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