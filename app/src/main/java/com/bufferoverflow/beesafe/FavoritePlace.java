package com.bufferoverflow.beesafe;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bufferoverflow.beesafe.AuxTools.AuxCrowd;
import com.bufferoverflow.beesafe.BackgroundService.App;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.Objects;

/*
    This class represents a users saved location.
 */

public class FavoritePlace implements Serializable {

    private final String placeName;
    private final String geohash;
    private boolean receiveNotifications; //If true, user get notified if this favorite place gets crowded
    private ValueEventListener crowdEventListener;


    public FavoritePlace (String geohash, String placeName, Boolean notified, Context c) {
        Log.d("CREATED", placeName + " " + notified + " " + geohash);
        this.geohash = geohash;
        this.placeName = placeName;
        this.receiveNotifications = notified;
    }

    public String getGeoHash() {
        return this.geohash;
    }

    public String getPlaceName () {
        return placeName;
    }

    public void setReceiveNotifications(boolean status) {
        receiveNotifications = status;
    }

    public boolean getReceiveNotifications() {return receiveNotifications;}

    /* This method returns the number of devices */
    public int getNrDevices (DataSnapshot snapshot) {
        return ((Long) snapshot.child("nrDevices").getValue()).intValue();
    }

    /* Activated the listener for database change on this favorite place */
    public void enableCrowdEventListener (Context c) {
        String areaGeoHash = geohash.substring(0, Area.PRECISION);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/" + areaGeoHash + "/" + geohash);
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
            public void onCancelled(@NonNull DatabaseError databaseError){ }
        };
        databaseReference.addValueEventListener(crowdEventListener);
    }

    public void disableCrowdEventListener () {
        String areaGeoHash = geohash.substring(0, 4);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/" + areaGeoHash + "/" + geohash);
        databaseReference.removeEventListener(crowdEventListener);
    }

}
