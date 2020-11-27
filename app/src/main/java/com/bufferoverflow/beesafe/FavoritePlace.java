package com.bufferoverflow.beesafe;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.health.TimerStat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bufferoverflow.beesafe.AuxTools.AuxCrowd;
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


    public FavoritePlace (String geohash, String placeName, Boolean notified, Context c) {
        this.geohash = geohash;
        this.placeName = placeName;
        this.receiveNotifications = notified;
        enableEventListener(c); //Enables the notifications event listener
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

    /* Activated the listener for database change on this favorite place */
    public void enableEventListener (Context c) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("/u20d/u20dwxrf");
        db.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("DATAAAA", snapshot.toString());
                if (receiveNotifications && AuxCrowd.isCrowd(snapshot)) {
                    String title, content;

                    title = placeName + " is crowded!";
                    content = "Approximation: " + snapshot.child("nrDevices").getValue() + " devices.";

                    NotificationChannel notificationChannel = new NotificationChannel("Favorite Place Notification Channel","Favorite Place Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
                    NotificationManager manager = c.getSystemService(NotificationManager.class);
                    manager.createNotificationChannel(notificationChannel);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(c, notificationChannel.getId())
                            .setSmallIcon(R.drawable.favorite_icon)
                            .setContentTitle(title)
                            .setContentText(content)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);
                    notificationManager.notify(1000, builder.build());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });
    }

}
