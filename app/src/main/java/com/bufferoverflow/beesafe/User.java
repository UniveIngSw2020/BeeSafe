package com.bufferoverflow.beesafe;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import ch.hsr.geohash.GeoHash;

/*
    This class represents the user which is using the app. It should produce the same instance every time. (Singleton Pattern)
*/

public class User {

    /* For local storage saving */
    private final int PRIVATE_MODE = 0;
    private final String PREF_NAME = "USER_DATA";

    /* Properties */
    private HashMap<String, FavoritePlace> favoritePlaces; //Saved locations of the user
    private Area currentArea; //Current area GeoHashed
    private Area[] neighbourArea; //All 8 nearby GeoHash boxes N, NE, E, SE, S, SW, W, NW of precision 4

    /* Singleton Design Pattern */
    private static User user = null;

    /* Private constructor accessible only from getInstance method */
    private User(Context c) {
        loadFavoritePlaces(c);
        currentArea = null;
        neighbourArea = new Area[8];
    }

    /* Singleton Design Pattern */
    public static User getInstance(Context c) {
        if (user == null)
            user = new User(c);
        return user;
    }

    /* Updates the location of the user with the new Latitude and Longitude coordinates
    *  If currentArea is null (after loading the app) or if the area where the user is at the moment
    *  is different from the area covered by new coordinates, it changes the user's area and updates all the neighbour areas.
    */
    public void updateCurrentPosition(Double updatedLatitude, Double updatedLongitude) {
        //User has moved into a new Area OR loading the application -> update current Area + Neighbours
        if (currentArea == null || !GeoHash.geoHashStringWithCharacterPrecision(updatedLatitude, updatedLongitude, Area.PRECISION).equals(currentArea.getCoordinates())) {
            this.currentArea = new Area(new LatLng(updatedLatitude, updatedLongitude));
            for(int i=0; i<8;i++) { //update neighbour boxes of this area ----- N, NE, E, SE, S, SW, W, NW
                System.out.println("[" + i + "]" + currentArea.getGeoHash().getAdjacent()[i].toBase32());
                neighbourArea[i] = new Area(currentArea.getGeoHash().getAdjacent()[i]);
            }
        }
    }

    public Area getCurrentArea () { return currentArea;  }

    /*  Save a favorite place to local storage.
     *  Should be called only within this class after saving a new favorite place.
     *  Should be called every time a new place is added.
     *  TODO : Render the favorite place on map before calling this method.
     */
    public void addFavoritePlace(FavoritePlace fav, Context c) {
        favoritePlaces.put(fav.getGeoHash(), fav); //Adding to the field
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson favPlacesGSON = new Gson();
        String favorite = favPlacesGSON.toJson(fav);
        editor.putString(fav.getGeoHash(), favorite); //Saving locally | key:geohash -> value:FavoritePlace
        editor.apply();
        for (FavoritePlace l : favoritePlaces.values()) {
            System.out.println(l.getPlaceName() + " | " + l.getGeoHash());
        }
    }

    /* Removes a location from favorites (RAM + Local Storage)
     * TODO : Remove the favorite place from the map before calling this map.
     */
    public void removeFavoritePlace (String geohash, Context c) {
        if (favoritePlaces.containsKey(geohash)) {
            favoritePlaces.remove(geohash); //Remove from field
            SharedPreferences preferences = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            preferences.edit().remove(geohash).apply(); //Removing from local storage
        }
    }

    /*  Load favorite locations from local storage
     *  Should be called when opening the map to get the favorite places from local storage
     *  TODO : If this method is called from the map activity, render the map after calling this method.
     */
    public void loadFavoritePlaces (Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        favoritePlaces = new HashMap<>(); //reset field
        Map<String,?> keys = sharedPreferences.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){ //for every locally saved FavoritePlace
            Gson favPlacesGSON = new Gson(); //We create a new Gson object
            String json = entry.getValue().toString(); //Getting the key(GeoHash)
            Type type = new TypeToken<FavoritePlace>() {}.getType();
            FavoritePlace favorite = favPlacesGSON.fromJson(json, type);
            favoritePlaces.put(entry.getKey(), favorite); //Adding FavoriteLocation to field
        }
    }

    /* Get a favorite location from a geoHash */
    public FavoritePlace getFavoriteLocation (String geoHash) {
        if (favoritePlaces.containsKey(geoHash))
            return favoritePlaces.get(geoHash);
        else
            return null;
    }

    /* Get favorite locations of the user */
    public HashMap<String, FavoritePlace> getFavoriteLocations () {
        return favoritePlaces != null ? favoritePlaces : new HashMap<String, FavoritePlace>();
    }

    /* Enable event listeners for the background service */
    public void enableEventListenersFavPlaces() {

    }
}
