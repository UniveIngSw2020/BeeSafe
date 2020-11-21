package com.bufferoverflow.beesafe.AuxTools;

import com.bufferoverflow.beesafe.Area;
import com.google.android.gms.maps.model.LatLng;

import ch.hsr.geohash.GeoHash;

public class AuxMap {

    //TODO MOVE ALL METHODS TO MAPS ACTIVITY

    /* Check if the current location corrisponds to the current area */
    public static boolean movedOnNewArea(Area currentArea, Double updatedLatitude, Double updatedLongitude ) {
        if (currentArea == null || !GeoHash.geoHashStringWithCharacterPrecision(updatedLatitude, updatedLongitude, Area.PRECISION).equals(currentArea.getCoordinates())) {
            return true;
        }
        return false;
    }

    /*  Updates the location of the user with the new Latitude and Longitude coordinates
     *  If the area where the user is at the moment is different from the area covered by new coordinates,
     *  it changes the user's area and updates all the neighbour areas.
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

}
