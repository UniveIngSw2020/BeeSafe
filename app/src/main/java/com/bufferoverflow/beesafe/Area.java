package com.bufferoverflow.beesafe;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import ch.hsr.geohash.GeoHash;

@IgnoreExtraProperties
public class Area {

    public static final int PRECISION = 4; //Precision of GeoHash

    /* Firebase fields */
    private Map<String, Location> locations; //Locations with data available on database
    private String coordinates; //GeoHash in string format 4 precision

    /* Constructs an Area from a geoHash */
    public Area(GeoHash areaGeoHash) {
        this.coordinates = areaGeoHash.toBase32(); //Area GeoHash
        this.locations = new HashMap<>();
    }

    /* Constructs an area from a LatLngs */
    public Area(LatLng location) {
        this(GeoHash.withCharacterPrecision(location.latitude, location.longitude, PRECISION));
    }

    /* Firebase */
    public Area() {
    }

    /* GeoHash of this Area */
    public GeoHash getGeoHash() {
        return GeoHash.fromGeohashString(coordinates);
    }

    /* Firebase */
    public Map<String, Location> getLocations() {
        return locations;
    }

    /* Firebase */
    public String getCoordinates() {
        return coordinates;
    }

}
