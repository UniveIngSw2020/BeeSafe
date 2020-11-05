package com.bufferoverflow.beesafe;

import android.icu.text.AlphabeticIndex;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashSet;
import ch.hsr.geohash.GeoHash;

/*
    This class represents the user which is using the app. It should produce the same instance every time.
    User has some local saved locations.
    Singleton Design Pattern.
 */
public class Profile {

    @Nullable
    private static Profile instance = null;
    private LatLng currentLocation; //Current location in longitude and latitude
    private HashSet<FavoritePlace> favoritePlaces; //Saved locations of the user
    private Area currentArea; //Current area GeoHashed
    private Area[] neighbourArea; //All 8 nearby GeoHash boxes N, NE, E, SE, S, SW, W, NW of precision 4

    /* Private constructor accessible only from getInstance method */
    private Profile() {
        updateCurrentPosition(69.6969, 69.6969); //Updates current location by generating the GeoHashes of current location box and nearby boxes
    }

    /* Singleton Design Pattern to get only one instance of this class */
    @NotNull
    public static Profile getInstance() {
        if (instance == null)
            return new Profile();
        //Here we are sure Profile instance exists, so we just update the location and return the instance
        instance.updateCurrentPosition(69.6969, 69.6969);
        return instance;
    }

    /* Updates the location of this user frequently */
    public void updateCurrentPosition(Double updatedLatitude, Double updatedLongitude) {
        currentLocation = new LatLng(updatedLatitude, updatedLongitude);
        Area newArea = new Area(currentLocation);
        if (currentArea == null || newArea.getGeoHash() == currentArea.getGeoHash()) { //User has moved into a new Area OR first time opening the application -> update current Area + Neighbours
            currentArea = newArea;
            //update neighbour boxes of this area
            //N, NE, E, SE, S, SW, W, NW
            for(int i=0; i<8;i++)
                neighbourArea[i].setGeoHash(currentArea.getGeoHash().getAdjacent()[i]);
        }
  }

    /* Adds a location to favorites */
    public void addFavoriteLocation (FavoritePlace favorite) {
        favoritePlaces.add(favorite);
    }

    /* Removes a location from favorites */
    public void removeFavoriteLocation (FavoritePlace favorite) {
        favoritePlaces.remove(favorite);
    }

    /* Get favorite locations of user */
    @NotNull
    public HashSet<FavoritePlace> getFavoriteLocation (Location favorite) {
        return favoritePlaces;
    }
}
