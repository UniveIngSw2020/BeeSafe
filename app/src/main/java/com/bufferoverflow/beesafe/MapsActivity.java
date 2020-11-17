package com.bufferoverflow.beesafe;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private static GoogleMap map;
    private boolean mIsRestore;

    private static Map<String, Location> locations = new HashMap<>();

    private static final int[] HEATMAP_RED_GRADIENT = {
            Color.rgb(213, 0, 0)
    };
    private static final int[] HEATMAP_ORANGE_GRADIENT = {
            Color.rgb(255, 109, 0),
    };
    public static final float[] HEATMAP_START_POINTS_RED = {0.5f };
    public static final float[] HEATMAP_START_POINTS_ORANGE = { 0.5f };

    public static final Gradient HEATMAP_RED = new Gradient(HEATMAP_RED_GRADIENT, HEATMAP_START_POINTS_RED);
    public static final Gradient HEATMAP_ORANGE = new Gradient(HEATMAP_ORANGE_GRADIENT, HEATMAP_START_POINTS_ORANGE);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRestore = savedInstanceState != null;
        setContentView(R.layout.map);
        setUpMap();
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        if (this.map != null)
            return;
        this.map = map;



        map.setMaxZoomPreference((float) 17.9);
        /* Updating radius on zoom for more accurate heatmap */
        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            boolean flag = false;
            @Override
            public void onCameraMove() {
                if (map.getCameraPosition().zoom >= 17.) {
                    for(Map.Entry<String, Location> entry : locations.entrySet()) {
                        Location l = entry.getValue();
                        l.provider.setRadius(52);
                        l.overlay.clearTileCache();
                    }
                    flag = true;
                }
                else if (map.getCameraPosition().zoom > 16) {
                    for(Map.Entry<String, Location> entry : locations.entrySet()) {
                        Location l = entry.getValue();
                        l.provider.setRadius(30);
                        l.overlay.clearTileCache();
                    }
                    flag = true;
                }
                else {
                    if(flag) {
                        for(Map.Entry<String, Location> entry : locations.entrySet()) {
                            Location l = entry.getValue();
                            l.provider.setRadius(20);
                            l.overlay.clearTileCache();
                        }
                        flag = false;
                    }
                }
            }
        });
    }

    private void setUpMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    public static void addLocationToMap(Location location) {
        locations.put(location.getCoordinates(), location);
        Gradient g = (location.nrDevices <20) ? HEATMAP_ORANGE : HEATMAP_RED;
        ArrayList<LatLng> l = new ArrayList<>();
        l.add(location.getLatLng());
        location.provider = new HeatmapTileProvider.Builder()
                .data(l)
                .radius(30)
                .build();
        location.provider.setGradient(g);
        location.provider.setOpacity(0.7);
        location.overlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(location.provider));
    }

    public static void removeLocationFromMap(Location location) {
        locations.remove(location.getCoordinates());
        location.overlay.remove();
        location.overlay.clearTileCache();
    }

}