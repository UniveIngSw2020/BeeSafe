package com.bufferoverflow.beesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ch.hsr.geohash.GeoHash;

public class MainActivity extends AppCompatActivity {

    private ListView mLvDevices;
    private ArrayList<String> mDeviceList = new ArrayList<String>();

    private Map<String, Location> activeLocations; //after scanning, add data to db and this Map will be autoupdated by the event listener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button but =  (Button) findViewById(R.id.button);
        final EditText edit =  (EditText) findViewById(R.id.editTextTextPersonName);


        Profile user = new Profile(this); //This users profile
        user.updateCurrentPosition(45.503810, 12.260870); //
        final Area currentArea = user.getCurrentArea();

        //To save favorite locations
        //user.addFavoriteLocation(new Location(new LatLng(45.503810, 12.260870), 15), this);

        //To get favorite locations
//        HashSet<FavoritePlace> favs = user.getFavoriteLocation();
//        System.out.println(favs);

        //p.editor.putString(p.TEST, "XXXXXXXXX");
        //System.out.println(savedProfile.sharedPreferences.getString(p.TEST, null));

        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentArea.addLocation(new Location(new LatLng(45.503810, 12.260870), 15));
                try { Thread.currentThread().sleep(2000); } catch (InterruptedException ignored) { }
                currentArea.addLocation(new Location(new LatLng(45.479740, 12.249590),17));
                try { Thread.currentThread().sleep(2000); } catch (InterruptedException ignored) { }
                currentArea.addLocation(new Location(new LatLng(45.497735, 12.2676424), 30));

            }
        });

        startActivity(new Intent(MainActivity.this, MapsActivity.class));

        mLvDevices = (ListView) findViewById(R.id.lvDevicesMAIN);

        BleManager.getInstance().init(getApplication());
        BleManager b = BleManager.getInstance();
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setScanTimeOut(10000)
                .build();
        b.initScanRule(scanRuleConfig);

        b.scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                setFinishOnTouchOutside(success);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceList.add("Nome: " + bleDevice.getName() + ", " + "MAC: " + bleDevice.getMac());
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, mDeviceList);
                mLvDevices.setAdapter(adapter);
                System.out.println(bleDevice);
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                for (BleDevice b : scanResultList) {
                    System.out.println("Indirizzo MAC: " + b.getMac() + " | Nome: " + b.getName());
                }
            }
        });

//        Button button1 = (Button) findViewById(R.id.button1);
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(view.getContext(), FindBluetoothActivity.class);
//                view.getContext().startActivity(intent);}
//        });

    }

    private static void tracingAlgorithm () {

        //Scan for 10 seconds
        //Put devices in HashSet
        //Sleep 15 seconds
        //Scan for new devices for 10 seconds
        //Put devices in a new HashSet
        //devices <- The common elements of the two hashsets
        //currentLocation <- Current location
        //Place.upload(currentLocation, devices.size());

        //if Person moves from location (New probable crowd instance detected)
            //return crowd;
        //else
            //repeat

    }

}