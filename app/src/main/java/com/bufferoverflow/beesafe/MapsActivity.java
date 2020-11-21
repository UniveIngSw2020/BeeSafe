package com.bufferoverflow.beesafe;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.bufferoverflow.beesafe.AuxTools.AuxCrowd;
import com.bufferoverflow.beesafe.AuxTools.AuxDateTime;
import com.bufferoverflow.beesafe.AuxTools.AuxMap;
import com.bufferoverflow.beesafe.BackgroundService.BackgroundScanWork;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Map
    private static GoogleMap map;
    private Intent serviceIntent;
    private boolean mIsRestore;

    //HeatMap settings
    private static final int[] HEATMAP_RED_GRADIENT = { Color.rgb(213, 0, 0) };
    private static final int[] HEATMAP_ORANGE_GRADIENT = { Color.rgb(255, 109, 0), };
    public static final float[] HEATMAP_START_POINTS_RED = {0.5f };
    public static final float[] HEATMAP_START_POINTS_ORANGE = { 0.5f };
    public static final Gradient HEATMAP_RED = new Gradient(HEATMAP_RED_GRADIENT, HEATMAP_START_POINTS_RED);
    public static final Gradient HEATMAP_ORANGE = new Gradient(HEATMAP_ORANGE_GRADIENT, HEATMAP_START_POINTS_ORANGE);

    //Locations and Markers(Saved Places)
    private Map<String, Marker> savedPlaces = new HashMap<>(); //Saved places
    private Map<String, Location> locations = new HashMap<>(); //Locations with data
    private Area currentArea; //Current area GeoHashed
    private Area[] neighbourArea; //All 8 nearby GeoHash boxes N, NE, E, SE, S, SW, W, NW of precision 4

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

        currentArea = null;
        neighbourArea = new Area[8];
        User user = User.getInstance(this);
        AuxMap.updateCurrentPosition(currentArea, neighbourArea, 45.503810, 12.260870); //Update current area
        user.loadFavoritePlaces(this); //Load favorite places from local storage
        for (FavoritePlace fav : user.getFavoriteLocations().values()) { //Load all Favorite Places
            Marker m = addFavoritePlaceToMap(fav); //Add favorite to Map
            savedPlaces.put(fav.getGeoHash(), m); //Save the Marker of this map
        }





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
                addFavoriteDialog(latLng);
                Marker m = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Albania"));
            }
        });

        /* On marker click listener to view a favorite place */
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                viewFavoriteDialog(marker.getPosition());
                return false;
            }
        });

    }


    private void setUpMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    //Render Location to HeatMap
    public void addLocationToMap(Location location) {
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

    //Remove Location from HeatMap
    public void removeLocationFromMap(Location location) {
        locations.remove(location.getCoordinates());
        location.overlay.remove();
        location.overlay.clearTileCache();
    }

    //Render FavoritePlaceto Map
    public Marker addFavoritePlaceToMap(FavoritePlace place) {
        WGS84Point point = GeoHash.fromGeohashString(place.getGeoHash()).getOriginatingPoint();
        LatLng coordinates = new LatLng(point.getLatitude(), point.getLongitude());
        Bitmap favoritePinpoint = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fav2), 100, 120, false);
        Marker m = map.addMarker(new MarkerOptions()
                .position(coordinates)
                .icon(BitmapDescriptorFactory.fromBitmap(favoritePinpoint)));
        m.setTag(place); //Associated place for this Marker
        return m;
    }

    //Remove FavoritePlace from Map + from local storage + //TODO : Disable listener
    public void removeFavoritePlace(String geoHash) {
        User.getInstance(this).removeFavoritePlace(geoHash, this); //Remove from local storage
        Marker m = savedPlaces.get(geoHash); //Get Marker of this favorite location
        m.remove(); //Remove from Map
        //TODO : Disable notification listener
    }


    //TODO : Enable Listeners for Locations changes



    private void addFavoriteDialog(LatLng coordinates) {
        String geoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Location.PRECISION); //geoHash of Location (8 Precision)
        String areaGeoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Area.PRECISION); //geoHash of Area (4 Precision)

        //Get pretty printed street name using Google Maps API
        Geocoder geocoder = new Geocoder(this);
        List<Address> matches = new ArrayList<>();
        try { matches = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1); } catch (Exception ignored){};
        String streetName = (matches.isEmpty() ? "" : matches.get(0).getAddressLine(0));

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

                        User.getInstance(this).addFavoritePlace(favorite, this); //Saving it on local storage
                        Marker m = addFavoritePlaceToMap(favorite); //Add favorite to Map
                        savedPlaces.put(favorite.getGeoHash(), m); //Save the Marker of this map

                    });
                })
                .show();
    }

    //TODO Pass in marker
    @SuppressLint("SetTextI18n")
    private void viewFavoriteDialog(LatLng coordinates) {
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
                    TextView crowdedText = rootView.findViewById(R.id.crowdedText);
                    TextView lastUpdateText = rootView.findViewById(R.id.lastUpdateText);
                    TextView approximationText = rootView.findViewById(R.id.approximationText);

                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child(areaGeoHash).child(geoHash);
                    rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String crowd, approximation, lastUpdate;
                            if (snapshot.exists()) { // Data present on database for this favorite location
                                int crowdType = AuxCrowd.crowdType(snapshot);
                                crowd = getString(R.string.crowded) + getString(crowdType);
                                approximation = getString(R.string.approximation) + fav.getNrDevices(snapshot) + getString(R.string.persons); //approximated people
                                lastUpdate = getString(R.string.last_update) + AuxDateTime.getLastSeen(snapshot) + getString(R.string.minutes_ago); //last seen in minutes
                            }
                            else { //No data
                                crowd = getString(R.string.crowded) + getString(R.string.no_data); //crowd density
                                approximation = getString(R.string.approximation) + getString(R.string.no_data); //approximated people
                                lastUpdate = getString(R.string.last_update) + getString(R.string.no_data); //last seen in minutes
                            }
                            crowdedText.setText(crowd); //update crowded text
                            approximationText.setText(approximation); //update approximation
                            lastUpdateText.setText(lastUpdate); //update last seen in minutes
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });

                    ((TextView)rootView.findViewById(R.id.streetNameText)).setText(streetName); //Street Name
                    ((TextView)rootView.findViewById(R.id.customNameText)).setText(getString(R.string.namePlace) + namePlace); //Name of this favorite location
                    Button btnSave = rootView.findViewById(R.id.btnRemove);
                    btnSave.setOnClickListener(view -> { //Removing
                        removeFavoritePlace(geoHash); //Remove from Map + Remove from local storage
                    });
                })
                .show();
    }

}