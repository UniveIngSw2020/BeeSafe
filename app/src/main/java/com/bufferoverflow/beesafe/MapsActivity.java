package com.bufferoverflow.beesafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.bufferoverflow.beesafe.AuxTools.AuxDateTime;
import com.bufferoverflow.beesafe.BackgroundService.BackgroundScanWork;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import ch.hsr.geohash.GeoHash;

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

        /* On long click on a position on the map, the popup gets displayed */
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                addFavorite(latLng);
                Marker m = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Albania"));
            }
        });

        /* On marker click listener to view a favorite place */
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                viewFavorite(marker.getPosition());
                return false;
            }
        });



        //onlongclick
            //addFavorite
        //onclickPinpoint
            //viewFavorite

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



    private void addFavorite(LatLng coordinates) {
        String geoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Location.PRECISION); //geoHash of Location (8 Precision)
        String areaGeoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Area.PRECISION); //geoHash of Area (4 Precision)

        System.out.println("XXXXXXXXXXXXXXXXXXXX:" + geoHash + "|" + areaGeoHash);
        System.out.println("ADDDDDDDDDDDDDDDDDD" + coordinates);
        Geocoder geocoder = new Geocoder(this);
        List<Address> matches = new ArrayList<>();
        try { matches = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1); } catch (Exception ignored){};
        String streetName = (matches.isEmpty() ? "" : matches.get(0).getAddressLine(0));
        //String geoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Location.PRECISION);

        new LovelyCustomDialog(this)
                .setView(R.layout.add_favorite)
                .setTopColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.favorite_icon)
                .configureView(rootView -> {
                    ((TextView)rootView.findViewById(R.id.streetNameText)).setText(streetName); //Update Street Name

                    Button btnSave = rootView.findViewById(R.id.btnSave);
                    btnSave.setOnClickListener(view -> { //Adding
                        String name = ((EditText) rootView.findViewById(R.id.nameEditText)).getText().toString();
                        Boolean notified = ((CheckBox)rootView.findViewById(R.id.notificationsCheckBox)).isChecked();
                        FavoritePlace favorite = new FavoritePlace(geoHash, name, notified); //Creating the new fav place
                        //TODO : Render it on map
                        User.getInstance(this).addFavoritePlace(favorite, this); //Saving it
                    });
                })
                .show();
    }

    @SuppressLint("SetTextI18n")
    private void viewFavorite(LatLng coordinates) {
        String geoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Location.PRECISION); //geoHash of Location (8 Precision)
        String areaGeoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Area.PRECISION); //geoHash of Area (4 Precision)
        FavoritePlace fav = User.getInstance(this).getFavoriteLocation(geoHash);
        String namePlace = fav.getPlaceName();

        //Get pretty printed street name using Google Maps API
        Geocoder geocoder = new Geocoder(this);
        List<Address> matches = new ArrayList<>();
        try { matches = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1); } catch (Exception ignored){};
        String streetName = (matches.isEmpty() ? "" : matches.get(0).getAddressLine(0));

        new LovelyCustomDialog(this)
                .setView(R.layout.view_favorite)
                .setTopColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.favorite_icon)
                .configureView(rootView -> {
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child(areaGeoHash).child(geoHash);
                    rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String approximation;
                            int crowdType = fav.crowdType(snapshot);
                            String crowd = getString(R.string.crowded) + getString(crowdType);
                            if (crowdType == R.string.no_data)
                                approximation = getString(R.string.approximation) + getString(R.string.no_data);
                            else
                                approximation = getString(R.string.approximation) + fav.getNrDevices(snapshot) + getString(R.string.persons);
                            ((TextView) rootView.findViewById(R.id.crowdedText)).setText(crowd); //update crowded text
                            ((TextView) rootView.findViewById(R.id.lastUpdateText)).setText(getString(R.string.last_update) + AuxDateTime.getLastSeen(snapshot) + getString(R.string.minutes_ago)); //update last seen in minutes
                            ((TextView)  rootView.findViewById(R.id.approximationText)).setText(approximation); //update approximation
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });

                    ((TextView)rootView.findViewById(R.id.streetNameText)).setText(streetName); //Street Name
                    ((TextView)rootView.findViewById(R.id.customNameText)).setText(getString(R.string.namePlace) + namePlace); //Name of this favorite location
                    Button btnSave = rootView.findViewById(R.id.btnRemove);
                    btnSave.setOnClickListener(view -> { //Removing
                        User.getInstance(this).removeFavoritePlace(geoHash, this);
                    });
                })
                .show();
    }

    private void renderFavoritePlaces () { //TODO

    }


}