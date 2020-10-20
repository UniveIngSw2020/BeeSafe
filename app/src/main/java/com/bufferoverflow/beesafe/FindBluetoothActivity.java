package com.bufferoverflow.beesafe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class FindBluetoothActivity extends Activity {

    private BluetoothAdapter mBtAdapter;
    private ListView mLvDevices;
    private ArrayList<String> mDeviceList = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_bluetooth);

        mLvDevices = (ListView) findViewById(R.id.lvDevices);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBtReceiver, filter);

        // Getting the Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter != null) {
            mBtAdapter.startDiscovery();
            Toast.makeText(this, "Starting discovery...", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Bluetooth disabled or not available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        unregisterReceiver(mBtReceiver);
    }

    private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device.getAddress() + ", " + device.getName()); // get mac address
                //Toast.makeText(context, device.getAddress() + ", " + device.getName(), Toast.LENGTH_SHORT).show();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, mDeviceList);
                mLvDevices.setAdapter(adapter);
            }
        }
    };
}