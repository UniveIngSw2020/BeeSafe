package com.bufferoverflow.beesafe;
import com.clj.fastble.data.BleDevice;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Devices {

    private Set<BleDevice> devices;
    private List<String> blacklist = Arrays.asList(
            "TV", "Mi Band", "Airpods", "Buds"
    );

    public Devices (List<BleDevice> devices) {
        this.devices = new HashSet<>(devices);
    }

    // tries to minimize errors by blacklisting devices that are not phones
    private void filterDevices () {
        for(String type : blacklist)
            for (BleDevice device : devices)
                if (device.getName().contains(type))
                    devices.remove(device);
    }

    /* Returns the number of the devices scanned */
    public int getDevicesNumber () {
        filterDevices();
        return devices.size();
    }

}
