package com.bufferoverflow.beesafe;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;
import ch.hsr.geohash.GeoHash;

@IgnoreExtraProperties
public class Area {

    private static final int PRECISION = 4; //Precision of GeoHash
    private GeoHash areaGeoHash;

    //Firebase Fields
    private Map<String, Location> locations; //Locations with data available on database
    private String coordinates; //GeoHash in string format 4 precision

    //Listener for locations changing on this area
    private ChildEventListener areaEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d("added", "onChildAdded:" + dataSnapshot.getKey()  + " " + dataSnapshot.getValue());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d("changed", "onChildChanged:" + dataSnapshot.getKey()  + " " + dataSnapshot.getValue());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d("removed", "onChildRemoved:" + dataSnapshot.getKey()  + " " + dataSnapshot.getValue());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d("moved", "onChildMoved:" + dataSnapshot.getKey()  + " " + dataSnapshot.getValue());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("cancelled", "error", databaseError.toException());
        }
    };


    public Area (LatLng location) {
        this.areaGeoHash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, PRECISION);
        this.coordinates = areaGeoHash.toBase32();
        this.locations = new HashMap<String, Location>(); //String PRECISION 8 -> Location
    }

    public Area(GeoHash areaGeoHash) {
        this.locations = new HashMap<String, Location>();
        this.areaGeoHash = areaGeoHash;
    }

    public void addLocation (Location l) {
        GeoHash g = l.getLocationGeoHashed();
        //if(g.toString().equals(coordinates)) { //if Location is in this Area
            locations.put(l.getCoordinates(), l);
        //}
    }

    @Exclude
    public GeoHash getGeoHash () {
        return areaGeoHash;
    }

    public void setGeoHash (GeoHash g) {
        areaGeoHash = g;
    }

    //Firebase
    public Area() {}
    //Firebase
    public Map<String, Location> getLocations() { return locations; }
    //Firebase
    public String getCoordinates() { return coordinates; }

}
