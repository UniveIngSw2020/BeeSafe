package com.bufferoverflow.beesafe;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private static GoogleMap map;
    private boolean mIsRestore;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRestore = savedInstanceState != null;
        setContentView(R.layout.map);
        setUpMap();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (this.map != null)
            return;
        this.map = map;
        //((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        //addHeatMap();
    }

    private void setUpMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }


    public static void addLocationToMap (Location location) {
        ArrayList<LatLng> l = new ArrayList<>();
        l.add(location.getLatLng());
        location.provider = new HeatmapTileProvider.Builder()
                .data(l)
                .build();
        location.overlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(location.provider));
    }

    public static void removeLocationFromMap (Location location) {
        location.overlay.setVisible(false);
    }





    private void addHeatMap() {
        List<LatLng> latLngs = readItems();
        // Create a heat map tile provider, passing it the latlngs
        mProvider = new HeatmapTileProvider.Builder()
                .data(latLngs)
                .build();

        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    private List<LatLng> readItems() {
        List<LatLng> myCoordinates = new ArrayList<>();
        myCoordinates.add(new LatLng(45.503810, 12.260870));
        myCoordinates.add(new LatLng(45.479740, 12.249590));
        myCoordinates.add(new LatLng(45.497735, 12.2676424));
        return myCoordinates;
    }


}