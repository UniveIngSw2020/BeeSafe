package com.bufferoverflow.beesafe;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 * This class represents the user which is using the app.
 * It should produce the same instance every time. (Singleton Pattern)
 * This class provides methods related to saving a favorite place on local storage.
 */

public class User {

    /* For local storage saving */
    private final int PRIVATE_MODE = 0;
    private final String PREF_NAME = "USER_DATA";

    /* Properties */
    private HashMap<String, FavoritePlace> favoritePlaces; //Saved locations of the user

    /* User Instance */
    private static User user = null;

    /* Private constructor accessible only from getInstance method */
    private User(Context c) {
        loadFavoritePlaces(c);
    }

    /* Singleton Design Pattern. Always returns the same object representing the user with his favorite places */
    @NotNull
    public static User getInstance(Context c) {
        if (user == null)
            user = new User(c);
        return user;
    }

    /*  Save a favorite place to local storage.
     *  Should be called every time a new place is added from the map
     */
    public void addFavoritePlace(FavoritePlace fav, Context c) {
        favoritePlaces.put(fav.getGeoHash(), fav); //Saving on hashMap
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson favPlacesGSON = new Gson();
        String favorite = favPlacesGSON.toJson(fav);
        editor.putString(fav.getGeoHash(), favorite); //Saving locally | key:geoHash(String) -> value:FavoritePlace
        editor.apply();
        fav.enableCrowdEventListener(c); //Enabling the crowd event listener for this added location
    }

    /* Removes a location from favorites (RAM + Local Storage) */
    public void removeFavoritePlace (String geohash, Context c) {
        if (favoritePlaces.containsKey(geohash)) { //Recheck if favorite place is saved
            Objects.requireNonNull(favoritePlaces.get(geohash)).disableCrowdEventListener(); //Disable the crowd event listener
            Objects.requireNonNull(favoritePlaces.remove(geohash)).disableCrowdEventListener(); //Remove from field and disable event listener
            SharedPreferences preferences = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            preferences.edit().remove(geohash).apply(); //Removing from local storage
        }
    }

    /*  Load all favorite locations from local storage
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
    @Nullable
    public FavoritePlace getFavoriteLocation (String geoHash) {
        return (favoritePlaces.containsKey(geoHash)) ? favoritePlaces.get(geoHash) : null;
    }

    /* Get all favorite locations of the user */
    @Nullable
    public HashMap<String, FavoritePlace> getFavoriteLocations () {
        return favoritePlaces != null ? favoritePlaces : new HashMap<>();
    }

    /* Enable event listeners for all favorite places */
    public void enableAllCrowdEventListeners(Context c) {
        for (FavoritePlace fav : favoritePlaces.values())
            fav.enableCrowdEventListener(c);
    }

    /* Disable event listeners for all favorite places */
    public void disableAllCrowdEventListeners() {
        for (FavoritePlace fav : favoritePlaces.values())
            fav.disableCrowdEventListener();
    }
}
