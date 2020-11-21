package com.bufferoverflow.beesafe.AuxTools;

import com.bufferoverflow.beesafe.Area;
import com.bufferoverflow.beesafe.FavoritePlace;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import ch.hsr.geohash.GeoHash;

public class AuxMap {

    /* Updates the location of the user with the new Latitude and Longitude coordinates
     *  If currentArea is null (after loading the app) or if the area where the user is at the moment
     *  is different from the area covered by new coordinates, it changes the user's area and updates all the neighbour areas.
     */
    public static void updateCurrentPosition(Area currentArea, Area[] neighbourArea, Double updatedLatitude, Double updatedLongitude) {
        //User has moved into a new Area OR loading the application -> update current Area + Neighbours
        if (currentArea == null || !GeoHash.geoHashStringWithCharacterPrecision(updatedLatitude, updatedLongitude, Area.PRECISION).equals(currentArea.getCoordinates())) {
            currentArea = new Area(new LatLng(updatedLatitude, updatedLongitude));
            for(int i=0; i<8;i++) { //update neighbour boxes of this area ----- N, NE, E, SE, S, SW, W, NW
                System.out.println("[" + i + "]" + currentArea.getGeoHash().getAdjacent()[i].toBase32());
                neighbourArea[i] = new Area(currentArea.getGeoHash().getAdjacent()[i]);
            }
        }
    }

    public static void getMarkersFromFavoritePlaces(HashMap<String, FavoritePlace> favoriteLocations) {
        Map<String, Marker> savedPlaces = new HashMap<>();

    }
}
