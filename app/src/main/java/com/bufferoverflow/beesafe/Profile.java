package com.bufferoverflow.beesafe;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import ch.hsr.geohash.GeoHash;

/*
    This class represents the user which is using the app. It should produce the same instance every time. (Singleton Pattern)
*/

public class Profile {

    /* Properties */
    private ArrayList<FavoritePlace> favoritePlaces; //Saved locations of the user
    private Area currentArea; //Current area GeoHashed
    private Area[] neighbourArea; //All 8 nearby GeoHash boxes N, NE, E, SE, S, SW, W, NW of precision 4

    private static Profile profile = null;
    private String UID = null;


    /* Private constructor accessible only from getInstance method */
    private Profile() {
        currentArea = null;
        neighbourArea = new Area[8];
        Task<String> userID = FirebaseInstallations.getInstance().getId();
        userID.addOnCompleteListener( //Generating an unique id of this app installation
                new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        UID = task.getResult();
                    }
                }
        );
    }

    public static Profile getInstance() {
        if (profile == null)
            profile = new Profile();
        return profile;

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

    /* Adds a location to favorites */
    public void addFavoriteLocation (FavoritePlace fav) { //TODO
        //add fav to db

    }

    /* Removes a location from favorites */
    public void removeFavoriteLocation (FavoritePlace fav) { //TODO
        //remove fav from db
    }

//    /* Get favorite locations of user */
//    @NotNull
//    public HashSet<FavoritePlace> getFavoriteLocation () {
//        return favoritePlaces;
//    }
}
