package com.bufferoverflow.beesafe;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Type;
import java.util.HashSet;
import ch.hsr.geohash.GeoHash;

/*
    This class represents the user which is using the app. It should produce the same instance every time.
    User has some local saved locations.
*/

public class Profile {

    /* For local storage saving */
    private static int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "USER_DATA";
    private static final String FAV_PLACES = "FAV_PLACES";

    /* Properties */
    private HashSet<FavoritePlace> favoritePlaces; //Saved locations of the user
    private Area currentArea = null; //Current area GeoHashed
    private Area[] neighbourArea; //All 8 nearby GeoHash boxes N, NE, E, SE, S, SW, W, NW of precision 4

    /* Private constructor accessible only from getInstance method */
    public Profile(Context c) {
        neighbourArea = new Area[8];
        favoritePlaces = loadFavoritePlaces(c);
        neighbourArea = new Area[8];
    }

    /* Updates the location of the user with the new Latitude and Longitude coordinates
    *  If currentArea is null (after loading the app) or if the area where the user is at the moment
    *  is different from the area covered by new coordinates, it changes the user's area and updates all the neighbour areas.
    */
    public void updateCurrentPosition(Double updatedLatitude, Double updatedLongitude) {
        if (currentArea == null || !GeoHash.geoHashStringWithCharacterPrecision(updatedLatitude, updatedLongitude, Area.PRECISION).equals(currentArea.getCoordinates())) { //User has moved into a new Area OR first time opening the application -> update current Area + Neighbours
            this.currentArea = new Area(new LatLng(updatedLatitude, updatedLongitude));
            for(int i=0; i<8;i++) { //update neighbour boxes of this area ----- N, NE, E, SE, S, SW, W, NW
                System.out.println("[" + i + "]" + currentArea.getGeoHash().getAdjacent()[i].toBase32());
                neighbourArea[i] = new Area(currentArea.getGeoHash().getAdjacent()[i]);
            }
        }
    }

    public Area getCurrentArea () { return currentArea;  }

    public Area[] getNeighbourArea () { return neighbourArea;  }

    //Load Favorite places from local storage
    private HashSet<FavoritePlace> loadFavoritePlaces(Context c) {
        HashSet<FavoritePlace> favPlaces;
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        Gson favPlacesGSON = new Gson();
        String json = sharedPreferences.getString(FAV_PLACES, null);
        Type type = new TypeToken<HashSet<FavoritePlace>>() {}.getType();
        favPlaces = favPlacesGSON.fromJson(json, type);
        return (favPlaces == null) ? new HashSet<FavoritePlace>() : favPlaces;
    }

    //Save Favorite Location to local storage
    private void saveFavoritePlaces(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson favPlacesGSON = new Gson();
        String favPlaces = favPlacesGSON.toJson(favoritePlaces);
        editor.putString(FAV_PLACES, favPlaces);
        editor.apply();
    }

    /* Adds a location to favorites */
    public void addFavoriteLocation (Location favorite, Context c) {
        favoritePlaces.add(new FavoritePlace(favorite));
        saveFavoritePlaces(c);
    }

    /* Removes a location from favorites */
    public void removeFavoriteLocation (FavoritePlace favorite, Context c) {
        favoritePlaces.remove(favorite);
        saveFavoritePlaces(c);
    }

    /* Get favorite locations of user */
    @NotNull
    public HashSet<FavoritePlace> getFavoriteLocation () {
        return favoritePlaces;
    }
}
