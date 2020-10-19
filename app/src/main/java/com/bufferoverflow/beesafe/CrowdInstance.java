package com.bufferoverflow.beesafe;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/*
    TODO:
        Approximation of the range of a certain coordinate

 */

public class CrowdInstance {
    private ArrayList<DeviceInstance> nearbyDevices;
    private LatLng coordinate;

    public CrowdInstance(ArrayList<DeviceInstance> nearbyDevices) {
        this.nearbyDevices = nearbyDevices;
    }

    // returns the number of devices in a certain location
    public double getDevicesNumber() {
        return nearbyDevices.size();
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
        return "[Devices:" + nearbyDevices.size() + ", Latitude: " + getLatitude() + ", Longitude: " + getLongitude() + "]";
    }
}
