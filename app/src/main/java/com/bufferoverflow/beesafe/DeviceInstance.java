package com.bufferoverflow.beesafe;

import android.bluetooth.BluetoothDevice;

import com.clj.fastble.data.BleDevice;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class DeviceInstance extends BleDevice {

    private LocalDateTime lastSeen;
    private boolean isNearby; // Controls if the device was nearby or a moving beacon

    // tries to blacklist devices that are not phones
    public static  boolean isMobilePhone(DeviceInstance device) {
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

    public DeviceInstance(BluetoothDevice device) {
        super(device);
    }
}
