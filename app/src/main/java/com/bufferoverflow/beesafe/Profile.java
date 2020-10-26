package com.bufferoverflow.beesafe;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashSet;
import ch.hsr.geohash.GeoHash;

/*
    This class represents the user which is using the app. It should produce the same instance every time.
    Singleton Design Pattern.
 */
public class Profile {

    @Nullable
    private static Profile instance = null;
    private LatLng currentLocation; //Current location in longitude and latitude
    private HashSet<Location> savedLocations; //Saved locations of the user
    private GeoHash currentLocationGeoHash; //Current location GeoHashed
    private GeoHash[] neighbourLocationBoxGeoHash; //All 8 nearby GeoHash boxes (east, north, west, south, north-east, north-west, south-east, south-west) of precision 4

    /* Private constructor accessible only from getInstance method */
    private Profile() {
        updateCurrentPosition(69.6969, 69.6969); //Updates current location by generating the GeoHashes of current location box and nearby boxes
    }

    /* Singleton Design Pattern to get only one instance of this class */
    @NotNull
    public static Profile getInstance() {
        if (instance == null)
            instance = new Profile();
        return instance;
    }

    /* Updates the location of this user frequently */
    public void updateCurrentPosition(Double updatedLatitude, Double updatedLongitude) {
        currentLocation = new LatLng(updatedLatitude, updatedLongitude);
        currentLocationGeoHash = GeoHash.withCharacterPrecision(updatedLatitude, updatedLongitude, 4);
    }

    /* Adds a location to favorites */
    public void addFavoriteLocation (Location favorite) {
        savedLocations.add(favorite);
    }

    /* Removes a location from favorites */
    public void removeFavoriteLocation (Location favorite) {
        savedLocations.remove(favorite);
    }

    /* Get favorite locations of user */
    @NotNull
    public HashSet<Location> getFavoriteLocation (Location favorite) {
        return savedLocations;
    }
}
