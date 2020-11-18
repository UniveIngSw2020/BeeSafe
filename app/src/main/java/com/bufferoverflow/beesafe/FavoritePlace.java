package com.bufferoverflow.beesafe;

import com.google.android.gms.maps.model.LatLng;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

/*
    This class represents a users saved location.
 */

public class FavoritePlace {

    private String placeName;
    private GeoHash  geohash;
    private boolean receiveNotifications; //If true, user get notified if this favorite place gets crowded

    public FavoritePlace (String geohash, String placeName) {
        this.geohash = GeoHash.fromGeohashString(geohash);
        this.placeName = placeName;
        this.receiveNotifications = true;
    }

    public LatLng getLatLng() {
        WGS84Point point = geohash.getOriginatingPoint();
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    public String getPlaceName () {
        return placeName;
    }


}
