package com.bufferoverflow.beesafe;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bufferoverflow.beesafe.AuxTools.AuxCrowd;
import com.bufferoverflow.beesafe.BackgroundService.App;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/*
 * This class represents a users saved location.
 */

public class FavoritePlace {

    private final String placeName; //Name that user gave to this place
    private final String geohash; //GeoHash of this location
    private boolean receiveNotifications; //If true, user get notified if this favorite place gets crowded
    private ValueEventListener crowdEventListener; //Crowd event listener associated to this location

    /* Constructs a favorite place based on a goeHash, name and a boolean which represents if notifications are enabled or not for this location */
    public FavoritePlace(String geohash, String placeName, Boolean notified) {
        this.geohash = geohash;
        this.placeName = placeName;
        this.receiveNotifications = notified;
    }

    /* This method returns the geoHash of this location */
    public String getGeoHash() {
        return this.geohash;
    }

    /* This method returns the name of this location */
    public String getPlaceName() {
        return placeName;
    }

    /* This method returns the status of notifications for this place */
    public boolean getReceiveNotifications() {
        return receiveNotifications;
    }

    /* This method sets the boolean variable associated to the notifications */
    public void setReceiveNotifications(boolean status) {
        receiveNotifications = status;
    }

    /* Activated the listener for database change on this favorite place */
    public void enableCrowdEventListener(Context c) {
        String areaGeoHash = geohash.substring(0, Area.PRECISION); //Area geoHash
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/" + areaGeoHash + "/" + geohash); //DB reference to this favorite place
        crowdEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int nrDevices = ((Long) Objects.requireNonNull(snapshot.child("nrDevices").getValue())).intValue();
                    if (receiveNotifications && snapshot.exists() && AuxCrowd.isCrowd(nrDevices)) {
                        String title = "Your favorite place " + placeName + " is now crowded!";
                        String content = "Approximation: " + nrDevices + " people.";
                        App.getMyAppsNotificationManager(c).sendFavPlaceNotification(title, content, FavoritePlace.this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        databaseReference.addValueEventListener(crowdEventListener); //adds the event listener
    }

    /* Disables the listener for database change on this favorite place */
    public void disableCrowdEventListener() {
        String areaGeoHash = geohash.substring(0, Area.PRECISION); //Area geoHash
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/" + areaGeoHash + "/" + geohash);
        databaseReference.removeEventListener(crowdEventListener); //Removes the event listener
    }

}
