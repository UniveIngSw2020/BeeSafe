package com.bufferoverflow.beesafe;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bufferoverflow.beesafe.BackgroundService.BackgroundScanWork;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private MaterialAlertDialogBuilder materialAlertDialogBuilder;

    private static GoogleMap map;
    private boolean mIsRestore;
    private Intent serviceIntent;
    private FirebaseAuth mAuth;

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
    protected void onStart() {
        super.onStart();


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRestore = savedInstanceState != null;

        setContentView(R.layout.map);
        setUpMap();

        User user = User.getInstance(this);
        user.updateCurrentPosition(45.503810, 12.260870); //
        final Area currentArea = user.getCurrentArea();

        /* To upload some samples
            currentArea.addLocation(new Location(new LatLng(45.503810, 12.260870), 15));
            try { Thread.currentThread().sleep(2000); } catch (InterruptedException ignored) { }
            currentArea.addLocation(new Location(new LatLng(45.479740, 12.249590),17));
            try { Thread.currentThread().sleep(2000); } catch (InterruptedException ignored) { }
            currentArea.addLocation(new Location(new LatLng(45.497735, 12.2676424), 30));
        */

        serviceIntent = new Intent(getApplicationContext(), BackgroundScanWork.class);
        ContextCompat.startForegroundService(this,serviceIntent);
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


        addFavorite();

    }

    private void setUpMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    //HeatMap
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


    private void addFavorite() {
        new LovelyCustomDialog(this)
                .setView(R.layout.add_favorite)
                .setTopColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.favorite_icon)
                .configureView(rootView -> {
                    Button b = rootView.findViewById(R.id.btnLogin);
                    b.setOnClickListener(view -> {
                        EditText editText = rootView.findViewById(R.id.etEmail);
                        String temp = editText.getText().toString();
                        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx" +  temp);
                    });
                })
                .show();
    }

    private void viewFavorite() {
        new LovelyCustomDialog(this)
                .setView(R.layout.view_favorite)
                .setTopColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.favorite_icon)
                .configureView(rootView -> {
                    Button b = rootView.findViewById(R.id.btnLogin);
                    b.setOnClickListener(view -> {
                        EditText editText = rootView.findViewById(R.id.etEmail);
                        String temp = editText.getText().toString();
                        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx" +  temp);
                    });
                })
                .show();
    }

}