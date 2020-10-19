package com.bufferoverflow.beesafe;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;

/*
    TODO:
        Approximation of the range of a certain coordinate

 */

public class CrowdInstance {
    private HashSet<DeviceInstance> devices;
    private LatLng coordinate;

    public CrowdInstance(HashSet<DeviceInstance> nearbyDevices) {
        this.devices = nearbyDevices;
    }

    public CrowdInstance() {

    }

    // adds a new Bluetooth device on this location
    public void addDevice(DeviceInstance device) {
        devices.add(device);
    }

    // returns the number of devices in a certain location
    public double getDevicesNumber() {
        return devices.size();
    }

    // returns the latitude of this geo location
    public double getLatitude() {
        return coordinate.latitude;
    }

    // returns the longitude of this geo location
    public double getLongitude() {
        return coordinate.longitude;
    }

    // returns a string representation of a Crowd Point
    public String toString() {
        return "[Devices:" + devices.size() + ", Latitude: " + getLatitude() + ", Longitude: " + getLongitude() + "]";
    }
}
