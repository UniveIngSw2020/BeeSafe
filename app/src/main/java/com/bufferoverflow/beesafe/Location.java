package com.bufferoverflow.beesafe;

import com.bufferoverflow.beesafe.AuxTools.AuxDateTime;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.firebase.database.Exclude;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

/*
    This class represents a Location present on the database.
    HeatMaps are rendered based on objects of this class.
    The HeatMap color is based on the number of devices which are at these location.
    Objects of this class should be created only from the data on the real-time database. (Exception for favorite location objects)
    Render Location objects that are inside users location (declared on the Profile class)
    JSON struct:
        "u209" : { //Precision 4
            "u20dygz2" : { //Precision 8 | 38.2m x 19.1m
                "nrDevices" : 20
                "lastSeen" : 124532512312
            }
        }
 */

public class Location  {

    /* Maps rendering properties and field */
    public HeatmapTileProvider provider;
    public TileOverlay overlay;

    public static final int PRECISION = 8; //Precision of GeoHash - Precision 8 => 38.2m x 19.1m
    private static final int BLUETOOTH_DEVICES_TRIGGER = 40; //Precision of GeoHash - Precision 8 => 38.2m x 19.1m
    private GeoHash coordinatesGeoHashed; //Coordinates of this location GeoHashed with a certain precision (suggested: 8)

    //Fields present on firebase database
    private String coordinates; //GeoHash in string format
    public int nrDevices; //Number of Bluetooth devices on this location
    private String lastSeen; //Last time on when data is updated

    //Firebase
    public Location() {}

    /* Create a Location from Coordinates after a Scan*/
    public Location (LatLng coordinates, int nrDevices) {
        this.coordinatesGeoHashed = GeoHash.withCharacterPrecision(coordinates.latitude, coordinates.longitude, PRECISION);
        this.coordinates = coordinatesGeoHashed.toBase32();
        this.lastSeen = AuxDateTime.dateToString(AuxDateTime.currentTime());
        this.nrDevices = nrDevices;
    }

    /* Create a Location from a given GeoHash */
    public Location (String g, int nrDevices) {
        this.coordinatesGeoHashed = GeoHash.fromGeohashString(g);
        this.coordinates = coordinatesGeoHashed.toBase32();
        this.lastSeen = AuxDateTime.dateToString(AuxDateTime.currentTime());
        this.nrDevices = nrDevices;
    }

    /* Returns the coordinate in LatLng format (the format which accepts Google Maps SDK) */
    @Exclude
    public LatLng getLatLng () {
        WGS84Point point = GeoHash.fromGeohashString(coordinates).getOriginatingPoint();
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    @Exclude
    public GeoHash getLocationGeoHashed () {
        return coordinatesGeoHashed;
    }

    //Firebase
    @Exclude
    public String getCoordinates() {
        return coordinates;
    }
    //Firebase
    public int getNrDevices() {
        return nrDevices;
    }
    //Firebase
    public String getTime() {
        return lastSeen;
    }


}
