package com.bufferoverflow.beesafe;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

/*
    This class represents a users saved location.
 */

public class FavoritePlace implements Serializable {

    private String placeName;
    private String geohash;
    private boolean receiveNotifications; //If true, user get notified if this favorite place gets crowded


    public FavoritePlace (String geohash, String placeName, Boolean notified) {
        this.geohash = geohash;
        this.placeName = placeName;
        this.receiveNotifications = notified;
        enableEventListener(); //Enables the notifications event listener
    }

    public LatLng getLatLng() {
        WGS84Point point = GeoHash.fromGeohashString(geohash).getOriginatingPoint();
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    public String getGeoHash() {
        return this.geohash;
    }

    public String getPlaceName () {
        return placeName;
    }

    /* Activated the listener for database change on this favorite place */
    public void enableEventListener () {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("/u20d/u20dwxrf");
        db.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("DATAAAA", snapshot.toString());
                //TODO: Create a pop-up notification if crowded && receiveNotifications==true.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });
    }

}
