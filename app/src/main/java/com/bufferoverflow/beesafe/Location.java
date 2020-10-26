package com.bufferoverflow.beesafe;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

/*
    TODO:
        -Connect this class with FireBase DB
        -Method to update the Location data
        -Complete the class with prevision fields and methods

    This class represents a Location and its data.
    HeatMaps are rendered based on objects of this class.
    The HeatMap color is based on the number of devices which are at these location.
    Objects of this class should be created only from the data on the real-time database. (Exception for favorite location objects)
    Render Location objects that are inside users location (declared on the Profile class)
    JSON struct:
        "u20" : { //Precision 3 | 156km x 156km   //to be changed to 4
            "u20dygz2" : { //Precision 8 | 38.2m x 19.1m
                "nrDevices" : 20.
                "lastSeen" : 124532512312
            }
        }
 */

public class Location {

    private static final int LOCATION_PRECISION = 8; //Precision of GeoHash - Precision 8 => 38.2m x 19.1m
    private static final int BLUETOOTH_DEVICES_TRIGGER = 40; //Precision of GeoHash - Precision 8 => 38.2m x 19.1m
    private GeoHash coordinates; //Coordinates of this location GeoHashed with a certain precision (suggested: 8)
    private int nrDevices; //Number of Bluetooth devices which
    private Date lastSeen; //Last time on when data is updated

    /* Create a Location from coordinates */
    public Location (LatLng coordinates) {
        this.coordinates = GeoHash.withCharacterPrecision(coordinates.latitude, coordinates.longitude, LOCATION_PRECISION);
    }

    /* Create a Location from a give GeoHash */
    public Location (GeoHash g) {
        coordinates = g;
    }

    /* Returns the coordinate in LatLng format (the format which accepts Google Maps SDK */
    public LatLng getLatLng () {
        WGS84Point point = coordinates.getOriginatingPoint();
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    /* This static method creates a new Crowd on database if it doesn't exist, or updates data of an existing crowd */
    public void updateCrowd () {

    }
}
