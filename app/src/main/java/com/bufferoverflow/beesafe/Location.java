package com.bufferoverflow.beesafe;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Date;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

/*
    TODO:
        -Connect this class with FireBase DB
        -Method to update the Location data
        -Complete the class with prevision fields and methods

    This class represents a Location present on the database.
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

    Area[ Map<String, Location>[] ]
 */

public class Location {

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");

    private static final int PRECISION = 8; //Precision of GeoHash - Precision 8 => 38.2m x 19.1m
    private static final int BLUETOOTH_DEVICES_TRIGGER = 40; //Precision of GeoHash - Precision 8 => 38.2m x 19.1m
    private GeoHash coordinatesGeoHashed; //Coordinates of this location GeoHashed with a certain precision (suggested: 8)

    //Fields present on firebase database
    private String coordinates; //GeoHash in string format
    private int nrDevices; //Number of Bluetooth devices on this location
    private String lastSeen; //Last time on when data is updated

    //Firebase
    public Location() {}

    /* Create a Location from Coordinates after a Scan*/
    public Location (LatLng coordinates, int nrDevices) {
        this.coordinatesGeoHashed = GeoHash.withCharacterPrecision(coordinates.latitude, coordinates.longitude, PRECISION);
        this.coordinates = coordinatesGeoHashed.toBase32();
        this.lastSeen = ISO_8601_FORMAT.format(new Date());
        this.nrDevices = nrDevices;
        System.out.println(this.coordinates + "    " + nrDevices + "      " + lastSeen);
    }

    /* Create a Location from a given GeoHash */
    public Location (String g) {
        this.coordinatesGeoHashed = GeoHash.fromGeohashString(g);
        this.coordinates = coordinatesGeoHashed.toBase32();
        this.lastSeen = ISO_8601_FORMAT.format(new Date());
    }

    /* Returns the coordinate in LatLng format (the format which accepts Google Maps SDK) */
    @Exclude
    public LatLng getLatLng () {
        WGS84Point point = GeoHash.fromGeohashString(coordinates).getOriginatingPoint();
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    @Exclude
    public GeoHash getLocationGeoHashed () {
        return coordinatesGeoHashed;
    }

//    /* This static method creates a new Crowd on database if it doesn't exist, or updates data of an existing crowd */
//    public void updateCrowd (DatabaseReference locationReference) {
//        //current time + this.location + nrDevices
//        locationReference.setValue(this);
//    }

    //Firebase
    @Exclude
    public String getCoordinates() {
        return coordinates;
    }
    //Firebase
    public int getNrDevices() {
        return nrDevices;
    }
    //Firebase
    public String getLastSeen() {
        return lastSeen;
    }


}
