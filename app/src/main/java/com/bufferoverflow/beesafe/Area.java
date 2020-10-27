package com.bufferoverflow.beesafe;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import ch.hsr.geohash.GeoHash;

public class Area {

    private static final int PRECISION = 4; //Precision of GeoHash
    private ArrayList<Location> locations; //Locations with data available on database
    private GeoHash areaGeoHash;

    public Area (LatLng location) {
        areaGeoHash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, PRECISION);
    }

    public GeoHash getGeoHash () {
        return areaGeoHash;
    }

    public void setGeoHash (GeoHash g) {
        areaGeoHash = g;
    }

}
