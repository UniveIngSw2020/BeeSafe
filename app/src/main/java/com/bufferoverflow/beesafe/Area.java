package com.bufferoverflow.beesafe;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;
import ch.hsr.geohash.GeoHash;

@IgnoreExtraProperties
public class Area {

    public static final int PRECISION = 4; //Precision of GeoHash
    public DatabaseReference mDatabase;

    //Firebase Fields
    private Map<String, Location> locations; //Locations with data available on database
    private String coordinates; //GeoHash in string format 4 precision

    //Listener for locations changing on this area
    private ChildEventListener areaEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            locations.put(dataSnapshot.getKey(), dataSnapshot.getValue(Location.class));
            //Add point and render the map (if Location is a crowd)
            Log.d("added", "onChildAdded:" + dataSnapshot.getKey()  + " " + dataSnapshot.getValue());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            locations.put(dataSnapshot.getKey(), dataSnapshot.getValue(Location.class));
            //Add point and render the map (if Location is a crowd)
            Log.d("changed", "onChildChanged:" + dataSnapshot.getKey()  + " " + dataSnapshot.getValue());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            locations.remove(dataSnapshot.getKey());
            //if this Location was a crowd, remove it from the map
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

    public Area(GeoHash areaGeoHash) {
        this.coordinates = areaGeoHash.toBase32();
        this.locations = new HashMap<String, Location>(); //String PRECISION 8 -> Location
        mDatabase = FirebaseDatabase.getInstance().getReference().child(getCoordinates()); //Gets a node reference for the current 4Precision GeoHash
        mDatabase.addChildEventListener(areaEventListener); //Adds the listener

    }

    public Area (LatLng location) {
        this(GeoHash.withCharacterPrecision(location.latitude, location.longitude, PRECISION));
    }

    /* Uploads location details to the real-time database */
    public void addLocation (Location l) {
        if(l.getCoordinates().substring(0,PRECISION).equals(coordinates)) { //if Location is in this Area (double checking because this should always occur)
            DatabaseReference locationReference = mDatabase.child(l.getCoordinates());
            locationReference.setValue(l);
        }
    }

    @Exclude
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
