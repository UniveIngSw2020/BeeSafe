package com.bufferoverflow.beesafe;

import android.bluetooth.BluetoothDevice;

import com.clj.fastble.data.BleDevice;

import java.time.LocalDateTime;

public class DeviceInstance extends BleDevice {

    private LocalDateTime lastSeen;
    private boolean isNearby; // Controls if the device was nearby or a moving beacon

    public DeviceInstance(BluetoothDevice device) {
        super(device);
    }
}
