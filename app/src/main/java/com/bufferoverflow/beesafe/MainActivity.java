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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bufferoverflow.beesafe.AuxTools.AuxDateTime;
import com.bufferoverflow.beesafe.BackgroundService.App;
import com.bufferoverflow.beesafe.BackgroundService.AppPersistentNotificationManager;
import com.bufferoverflow.beesafe.BackgroundService.BackgroundScanWork;
import com.clj.fastble.BleManager;
import com.google.android.gms.maps.model.Marker;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
TODO:
   Listeners for saved places
   Privacy Policy
   Help
   -Service Stop/Start Notification + Buttons :
   -Service Notification Change for current place (offline) [nr devices using only bluetooth]
   -Register broadcast listener for bluetooht/gps on off


 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BleManager.getInstance().init(getApplication());
    }

    //Open Map
    public void openMap (View view) {
        if (BackgroundScanWork.isBluetoothEnabled() && BackgroundScanWork.isGpsEnabled(getApplicationContext())) { //Check if Bluetooth and GPS are enabled
            if (!App.isServiceActive())
                App.startService(getApplicationContext());
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        }
        else {
            Toast toast = Toast. makeText(getApplicationContext(), "You need to enable GPS and Bluetooth.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void service(View view) {
        final Button button = findViewById(R.id.serviceButton);
        if (App.isServiceActive()){ //service is active, we need to stop it
            button.setText("Start Service");
            App.stopService(getApplicationContext());
        }
        else { //service is not active, we need to start it
            button.setText("Stop Service");
            App.startService(getApplicationContext());
        }
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