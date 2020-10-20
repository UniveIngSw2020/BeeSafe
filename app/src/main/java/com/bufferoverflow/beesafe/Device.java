package com.bufferoverflow.beesafe;

import android.bluetooth.BluetoothDevice;

import com.clj.fastble.data.BleDevice;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Device extends BleDevice {

    private LocalDateTime lastSeen;
    public LatLng coordinate;
    private boolean isNearby; // Controls if the device was nearby or a moving beacon

    // tries to blacklist devices that are not phones
    public static  boolean isMobileDevice (Device device) {
        ArrayList<String> types = new ArrayList<>();
        types.add("TV");
        types.add("Mi Band");
        types.add("Airpods");
        types.add("Buds");
        /// More to Add

        for(String type : types) {
            if (device.getName().contains(type))
                return false;
        }
        return true;
    }

    public Device(BluetoothDevice device) {
        super(device);
    }
}
