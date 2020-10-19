package com.bufferoverflow.beesafe;

import java.util.ArrayList;

public class CrowdInstance {
    private ArrayList<DeviceInstance> nearbyDevices;
    private double latitude;
    private double longitude;

    public CrowdInstance(ArrayList<DeviceInstance> nearbyDevices) {
        this.nearbyDevices = nearbyDevices;
    }

    // returns the number of devices in a certain location
    public double getDevicesNumber() {
        return nearbyDevices.size();
    }

    // returns the latitude of this geo location
    public double getLatitude() {
        return latitude;
    }

    // returns the longitude of this geo location
    public double getLongitude() {
        return longitude;
    }

    // returns a string representation of a Crowd Point
    public String toString() {
        return "[Devices:" + nearbyDevices.size() + ", Latitude: " + latitude + ", Longitude: " + longitude + "]";
    }
}
