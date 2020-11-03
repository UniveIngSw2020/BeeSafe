package com.bufferoverflow.beesafe;

import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.hsr.geohash.GeoHash;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private ListView mLvDevices;
    private ArrayList<String> mDeviceList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button but =  (Button) findViewById(R.id.button);
        final EditText edit =  (EditText) findViewById(R.id.editTextTextPersonName);

        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                User user = new User("nestsssssialb", "test@gmail.com");

                Location l1 = new Location(new LatLng(12.0, 11.0));
                Location l2 = new Location(new LatLng(26.0, 57.0));
                Location l3 = new Location(new LatLng(26.0, 56.0));

                Area a1 = new Area(new LatLng(26.0, 55.0));
                a1.addLocation(l1);
                a1.addLocation(l2);
                a1.addLocation(l3);

                mDatabase = FirebaseDatabase.getInstance().getReference();
                System.out.println(a1.getCoordinates() + " XXXXXXXXXXXXXXXXXXXXXXX");
                mDatabase.child(a1.getCoordinates()).setValue(a1.getLocations());
            }
        });


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