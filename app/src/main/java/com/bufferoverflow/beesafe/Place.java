package com.bufferoverflow.beesafe;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashSet;

/*
    TODO:
        Approximation of the range of a certain coordinate

 */

public class Place {

    private int DEVICES_LIMIT = 10;

    private LatLng coordinates;
    private HashSet<Device> devices;
    private int devicesNumber;

    //Constructor for unknown local Place && not present in Database
    public Place(LatLng coordinates) {
        this.coordinates = coordinates;
        this.devicesNumber = nrDevices;
        this.devices = new HashSet<>();
    }

    //Constructor for unknown local Place but present in Database
    public Place(LatLng coordinates, int nrDevices) {
        this.coordinates = coordinates;
        this.devicesNumber = nrDevices;
        this.devices = new HashSet<>();
    }

    // Returns a Place instance
    public Place instance (LatLng location) {
        DEVICES_LIMIT = 0;
        //if found on DB
            //D <- Value from server (nr Devices)
            //C <- Coordinates from Server
            //return Place(C, D);
        //else
            return new Place(location);
    }

    private boolean withinRadius(LatLng point) {
        //Calculate the distance between
    }

    //Centers the coordinate of the Place
    private void centerizeCoordinates() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(Device device : devices)
            builder.include(device.coordinate);
        LatLngBounds bounds = builder.build();
        this.coordinates =  bounds.getCenter();
    }

    // Controls if this place is crowded or not
    public boolean isCrowded() {
        return getDevicesNumber() > DEVICES_LIMIT;
    }

    //Adds a device from devices on this Place if does not exist
    public void addDevice(Device device) {
        devices.add(device);
        devicesNumber = devices.size(); //updates devices number
        centerizeCoordinates();
    }

    //Removes a device from devices on this Place
    public void removeDevice(Device device) {
        devices.remove(device);
        devicesNumber = devices.size(); //updates devices number
    }

    //Returns the number of devices in a certain location
    public double getDevicesNumber() {
        return devicesNumber;
    }

    //String representation of a Place
    public String toString() {
        return "[Devices:" + devices.size() + ", Latitude: " + coordinates.latitude + ", Longitude: " + coordinates.longitude + "]";
    }
}
