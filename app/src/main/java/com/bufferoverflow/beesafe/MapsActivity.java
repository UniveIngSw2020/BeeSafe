package com.bufferoverflow.beesafe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bufferoverflow.beesafe.AuxTools.AuxCrowd;
import com.bufferoverflow.beesafe.AuxTools.AuxDateTime;
import com.bufferoverflow.beesafe.AuxTools.AuxMap;
import com.bufferoverflow.beesafe.BackgroundService.BackgroundScanWork;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationSource.OnLocationChangedListener {

    //Map
    private GoogleMap map;
    private boolean mIsRestore;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLocations() != null ) {
                Log.d("XXXXXXXXXXX", "Lat " + locationResult.getLastLocation().getLatitude());
            }
        }
    };

    private ChildEventListener areaEventListener;
    private DatabaseReference mDatabase;

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
        Log.d("XXXXXXXXXXX","On start called  " + Thread.currentThread().getId() );
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRestore = savedInstanceState != null;
        setContentView(R.layout.map);
        setUpMap();

        /* To upload some samples
            currentArea.addLocation(new Location(new LatLng(45.503810, 12.260870), 15));
            try { Thread.currentThread().sleep(2000); } catch (InterruptedException ignored) { }
            currentArea.addLocation(new Location(new LatLng(45.479740, 12.249590),17));
            try { Thread.currentThread().sleep(2000); } catch (InterruptedException ignored) { }
            currentArea.addLocation(new Location(new LatLng(45.497735, 12.2676424), 30));
        */

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(MapsActivity.this, new OnSuccessListener<android.location.Location>() {
                    @Override
                    public void onSuccess(android.location.Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            GeoHash g = GeoHash.withCharacterPrecision(location.getLatitude(), location.getLongitude(), 8);
                            String geohash = g.toBase32();
                            Log.d("XXXXXXXXXXX","Thread ID " + Thread.currentThread().getId()  + "Lat: " + location.getLatitude() + " | Long: " + location.getLongitude() + " | Geohash: " + geohash );
                        }
                    }
                });

    }


    @Override
    protected void onStop() {
        super.onStop();
        disableAreaEventListner();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onStop();
        disableAreaEventListner();
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        if (this.map != null)
            return;
        this.map = map;

        enableMyLocation();

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



        updateCurrentPosition(new LatLng(45.491886, 12.244492)); //Update currentArea and neighbours area + Event listeners for this area

        User user = User.getInstance(this);
        user.loadFavoritePlaces(this); //Load favorite places from local storage
        for (FavoritePlace fav : user.getFavoriteLocations().values()) { //Load all Favorite Places
            Marker m = addFavoritePlaceToMap(fav); //Add favorite to Map
            savedPlaces.put(fav.getGeoHash(), m); //Save the Marker of this map
        }

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

    //Render FavoritePlace to Map
    public Marker addFavoritePlaceToMap(FavoritePlace place) {
        WGS84Point point = GeoHash.fromGeohashString(place.getGeoHash()).getOriginatingPoint();
        LatLng coordinates = new LatLng(point.getLatitude(), point.getLongitude());
        Bitmap favoritePinpoint = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fav2), 100, 120, false);
        Marker m = map.addMarker(new MarkerOptions() //Rendering Marker
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

    //Listener for locations changing on this area
    private void enableAreaEventListener () {
        areaEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Location location = new Location(dataSnapshot.getKey(), Integer.parseInt(dataSnapshot.child("nrDevices").getValue().toString()));
                locations.put(dataSnapshot.getKey(), location);
                addLocationToMap(location); //Add point and render the map (if Location is a crowd)
                Log.d("added", "onChildAdded:" + dataSnapshot.getKey()  + " " + dataSnapshot.getValue());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Location newLocationData = new Location(dataSnapshot.getKey(), Integer.parseInt(dataSnapshot.child("nrDevices").getValue().toString()));
                Location oldLocationData = locations.get(dataSnapshot.getKey());
                locations.put(dataSnapshot.getKey(), newLocationData);

                removeLocationFromMap(oldLocationData);
                addLocationToMap(newLocationData);

                Log.d("changed", "onChildChanged:" + dataSnapshot.getKey()  + " " + dataSnapshot.getValue());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Location location = locations.remove(dataSnapshot.getKey());
                removeLocationFromMap(location); //remove the location from the map
                Log.d("removed", "onChildRemoved:" + dataSnapshot.getKey()  + " " + dataSnapshot.getValue());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("moved", "onChildMoved:" + dataSnapshot.getKey()  + " " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("cancelled", "error", databaseError.toException());
            }
        }; //Listener
        mDatabase = FirebaseDatabase.getInstance().getReference().child(currentArea.getCoordinates()); //Gets a node reference for the current 4Precision GeoHash
        mDatabase.addChildEventListener(areaEventListener); //Adds the listener to this reference
    }

    /* Disable events listener for the current Area (Locations of this Area)
    *  First time opening Activity: Nothing happens
    * */
    private void disableAreaEventListner () {
        if (mDatabase != null)
            mDatabase.removeEventListener(areaEventListener);
    }




    private void addFavoriteDialog(LatLng coordinates) {
        String geoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Location.PRECISION); //geoHash of Location (8 Precision)
        String areaGeoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Area.PRECISION); //geoHash of Area (4 Precision)

        //Get pretty printed street name using Google Maps API
        Geocoder geocoder = new Geocoder(this);
        List<Address> matches = new ArrayList<>();
        try { matches = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1); } catch (Exception ignored){};
        String streetName = (matches.isEmpty() ? "" : matches.get(0).getAddressLine(0));

        final LovelyCustomDialog addFavDialog = new LovelyCustomDialog(this);
        addFavDialog.setView(R.layout.add_favorite);
        addFavDialog.setTopColorRes(R.color.colorPrimary);
        addFavDialog.configureView(rootView -> {
            ((TextView)rootView.findViewById(R.id.streetNameText)).setText(streetName); //Update Street Name

            Button btnSave = rootView.findViewById(R.id.btnSave);
            btnSave.setOnClickListener(view -> { //Adding
                String name = ((EditText) rootView.findViewById(R.id.nameEditText)).getText().toString();
                Boolean notified = ((CheckBox)rootView.findViewById(R.id.notificationsCheckBox)).isChecked();
                FavoritePlace favorite = new FavoritePlace(geoHash, name, notified); //Creating the new fav place

                User.getInstance(this).addFavoritePlace(favorite, this); //Saving it on local storage
                Marker m = addFavoritePlaceToMap(favorite); //Add favorite to Map
                savedPlaces.put(favorite.getGeoHash(), m); //Save the Marker of this map

                Toast.makeText(this, R.string.success_added, Toast.LENGTH_LONG).show();
                addFavDialog.dismiss();

            });

            Button btnCancel = rootView.findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(view -> { //Cancel
                addFavDialog.dismiss();
            });
        });
        addFavDialog.show();
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

        LovelyCustomDialog viewDialog = new LovelyCustomDialog(this);
        viewDialog.setView(R.layout.view_favorite);
        viewDialog.setTopColorRes(R.color.colorPrimary);
        viewDialog.setIcon(R.drawable.favorite_icon);
        viewDialog.configureView(rootView -> {
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

            Button btnRemove = rootView.findViewById(R.id.btnRemove);
            btnRemove.setOnClickListener(view -> { //Removing
                removeFavoritePlace(geoHash); //Remove from Map + Remove from local storage
                Toast.makeText(this, R.string.success_removed, Toast.LENGTH_LONG).show();
                viewDialog.dismiss();
            });

            Button btnCancel = rootView.findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(view -> { //Cancel
                viewDialog.dismiss();
            });
        });
        viewDialog.show();
    }




    @Override
    public void onLocationChanged(android.location.Location location) {
        Log.d("XXXXX", String.valueOf(location.getLatitude() + location.getLongitude() + location.getAccuracy()));
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }
        }
    }



    /*  Updates the location of the user with the new Latitude and Longitude coordinates
     *  If the area where the user is at the moment is different from the area covered by new coordinates,
     *  it changes the user's area and updates all the neighbour areas.
     */
    public void updateCurrentPosition(LatLng coordinates) {
        //User has moved into a new Area OR loading the application -> update current Area + Neighbours
        if (currentArea == null || !GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Area.PRECISION).equals(currentArea.getCoordinates())) {
            disableAreaEventListner();
            currentArea = new Area(new LatLng(coordinates.latitude, coordinates.longitude));
            enableAreaEventListener();
            neighbourArea = new Area[8];
            for(int i=0; i<8;i++) { //update neighbour boxes of this area ----- N, NE, E, SE, S, SW, W, NW
                System.out.println("[" + i + "]" + currentArea.getGeoHash().getAdjacent()[i].toBase32());
                neighbourArea[i] = new Area(currentArea.getGeoHash().getAdjacent()[i]);
            }
        }
    }

}