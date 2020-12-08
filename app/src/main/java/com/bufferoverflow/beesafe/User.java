package com.bufferoverflow.beesafe;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/*
    This class represents the user which is using the app. It should produce the same instance every time. (Singleton Pattern)
*/

public class User {

    /* For local storage saving */
    private final int PRIVATE_MODE = 0;
    private final String PREF_NAME = "USER_DATA";

    /* Properties */
    private HashMap<String, FavoritePlace> favoritePlaces; //Saved locations of the user

    /* Singleton Design Pattern */
    private static User user = null;

    /* Private constructor accessible only from getInstance method */
    private User(Context c) {
        loadFavoritePlaces(c);
    }

    /* Singleton Design Pattern */
    public static User getInstance(Context c) {
        if (user == null)
            user = new User(c);
        return user;
    }

    /*  Save a favorite place to local storage.
     *  Should be called every time a new place is added.
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

    /* Removes a location from favorites (RAM + Local Storage) */
    public void removeFavoritePlace (String geohash, Context c) {
        if (favoritePlaces.containsKey(geohash)) {
            favoritePlaces.remove(geohash); //Remove from field
            SharedPreferences preferences = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            preferences.edit().remove(geohash).apply(); //Removing from local storage
        }
    }

    /*  Load favorite locations from local storage
     *  Should be called when opening the map to get the favorite places from local storage
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

    /* Enable event listeners for the background service */ //TODO
    public void enableEventListenersFavPlaces() {

    }
}
