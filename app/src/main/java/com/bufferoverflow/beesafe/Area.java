package com.bufferoverflow.beesafe;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;
import ch.hsr.geohash.GeoHash;

@IgnoreExtraProperties
public class Area {

    public static final int PRECISION = 4; //Precision of GeoHash
    //Firebase Fields
    private Map<String, Location> locations; //Locations with data available on database
    private String coordinates; //GeoHash in string format 4 precision

    public Area(GeoHash areaGeoHash) {
        this.coordinates = areaGeoHash.toBase32();
        this.locations = new HashMap<String, Location>(); //String PRECISION 8 -> Location
        //mDatabase = FirebaseDatabase.getInstance().getReference().child(getCoordinates()); //Gets a node reference for the current 4Precision GeoHash
        //mDatabase.addChildEventListener(areaEventListener); //Adds the listener
    }

    public Area (LatLng location) {
        this(GeoHash.withCharacterPrecision(location.latitude, location.longitude, PRECISION));
    }

    /* GeoHash of this Area */
    public GeoHash getGeoHash () {
        return GeoHash.fromGeohashString(coordinates);
    }


    //Firebase
    public Area() {}
    //Firebase
    public Map<String, Location> getLocations() { return locations; }
    //Firebase
    public String getCoordinates() { return coordinates; }

}
