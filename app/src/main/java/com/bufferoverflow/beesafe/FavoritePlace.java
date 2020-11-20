package com.bufferoverflow.beesafe;

import android.annotation.SuppressLint;
import android.os.health.TimerStat;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

    /* This method returns the number of devices */
    public int getNrDevices (DataSnapshot snapshot) {
        return ((Long) snapshot.child("nrDevices").getValue()).intValue();
    }

    /* This static method checks if the passed snapshot, is Crowd or not */
    public int crowdType (DataSnapshot snapshot) {
        Object snap = snapshot.child("nrDevices").getValue();
        System.out.println("WWWWWWWWWWWWWWWWWWWWWW" + snapshot.child("nrDevices").getValue());
        if (snap == null)
            return R.string.no_data;
        else {
            int nrDevices = ((Long) snap).intValue();
            if (nrDevices < 20)
                return R.string.safe;
            else if (nrDevices < 30)
                return R.string.low;
            else
                return R.string.high;
        }
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
