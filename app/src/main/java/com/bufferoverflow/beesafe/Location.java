package com.bufferoverflow.beesafe;

import com.bufferoverflow.beesafe.AuxTools.AuxDateTime;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.firebase.database.Exclude;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

/*
 * This class represents a Location present on the database (location rich of data)
 * HeatMaps are rendered based on objects of this class.
 * The HeatMap color is based on the number of devices which are at these location.
 * Objects of this class should be created only from the data on the real-time database or from a scan instance.
 */

public class Location {

    public static final int PRECISION = 8; //Precision of GeoHash - Precision 8 => 38.2m x 19.1m

    /* Maps rendering properties */
    public HeatmapTileProvider provider;
    public TileOverlay overlay;
    public int nrDevices; //Number of Bluetooth devices on this location
    public String lastSeen; //Last time on when data is updated
    /* Fields present on firebase database should be public */
    private String coordinates; //GeoHash in string format

    /* Empty constructor used by firebase library to create objects of this class */
    public Location() {
    }

    /* Create a Location from a given GeoHash  and devices number */
    public Location(String g, int nrDevices) {
        this.coordinates = g;
        this.lastSeen = AuxDateTime.dateToString(AuxDateTime.currentTime());
        this.nrDevices = nrDevices;
    }

    /* Returns the coordinate in LatLng format (the format which accepts Google Maps SDK) */
    @Exclude
    public LatLng getLatLng() {
        WGS84Point point = GeoHash.fromGeohashString(coordinates).getOriginatingPoint();
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    /* Firebase */
    @Exclude
    public String getCoordinates() {
        return coordinates;
    }

    /* Firebase */
    public int getNrDevices() {
        return nrDevices;
    }

    /* Firebase */
    public String getLastSeen() {
        return lastSeen;
    }


}
