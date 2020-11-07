package com.bufferoverflow.beesafe;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import ch.hsr.geohash.GeoHash;

/*
    This class represents a local saved locations on user's device and has nothing to do with the database

 */

@IgnoreExtraProperties
public class FavoritePlace {

    private GeoHash  location;
    private Location associatedLocation;
    private boolean receiveNotifications; //If true, user get notified if this favorite place gets crowded

    public FavoritePlace (Location location) {
        this.location = location.getLocationGeoHashed();
        this.associatedLocation = null;
        this.receiveNotifications = true;
    }

    public boolean existsData () {
        return this.associatedLocation == null;
    }

    @NonNull
    @Override
    public String toString() {
        return "Saved Location geohashes: " + location.toBase32();
    }
}
