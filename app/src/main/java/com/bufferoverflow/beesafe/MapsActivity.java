package com.bufferoverflow.beesafe;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bufferoverflow.beesafe.AuxTools.AuxCrowd;
import com.bufferoverflow.beesafe.AuxTools.AuxDateTime;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final float[] HEATMAP_START_POINTS_RED = {0.5f};
    public static final float[] HEATMAP_START_POINTS_ORANGE = {0.5f};
    public static final double OPACITY = 0.7;
    //HeatMap settings
    private static final int[] HEATMAP_RED_GRADIENT = {Color.rgb(213, 0, 0)};
    public static final Gradient HEATMAP_RED = new Gradient(HEATMAP_RED_GRADIENT, HEATMAP_START_POINTS_RED);
    private static final int[] HEATMAP_ORANGE_GRADIENT = {Color.rgb(255, 109, 0),};
    public static final Gradient HEATMAP_ORANGE = new Gradient(HEATMAP_ORANGE_GRADIENT, HEATMAP_START_POINTS_ORANGE);
    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                //Map
                if (state == BluetoothAdapter.STATE_OFF) {
                    Toast toast = Toast.makeText(getApplicationContext(), "You need to enable GPS and Bluetooth.", Toast.LENGTH_LONG);
                    toast.show();
                    finish(); //Close the activity
                }
            }
        }
    };
    private final BroadcastReceiver gpsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.getSystemService(Context.LOCATION_SERVICE);
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast toast = Toast.makeText(getApplicationContext(), "You need to enable GPS and Bluetooth.", Toast.LENGTH_LONG);
                toast.show();
                finish(); //Close the activity
            }
        }
    };
    //Locations and Markers(Saved Places)
    private final Map<String, Marker> savedPlaces = new HashMap<>(); //Saved places
    private final Map<String, Location> locations = new HashMap<>(); //Locations with data
    private GoogleMap map; //Map object
    private ChildEventListener areaEventListener;
    private DatabaseReference mDatabase;
    private Area currentArea; //Current area GeoHashed
    private Area[] neighbourArea; //All 8 nearby GeoHash boxes N, NE, E, SE, S, SW, W, NW of precision 4


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        setUpMap(); //Configures the map

        //Registering Bluetooth broadcast receiver
        IntentFilter bluetoothFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothBroadcastReceiver, bluetoothFilter);
        //Registering Gps broadcast receiver
        IntentFilter gpsFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(gpsBroadcastReceiver, gpsFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        disableAreaEventListner(); //Disables the event listeners for the current area
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

        showLocationOnMap(); //Show current location on map + Center zoom to users location
        map.setMaxZoomPreference((float) 17.9); //Set Max Zoom level
        updateCurrentPosition(); //Update currentArea and neighbours area + Event listeners for this area

        /* Updating radius on zoom for more accurate HeatMap */
        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            boolean flag = false;

            @Override
            public void onCameraMove() {
                if (map.getCameraPosition().zoom >= 17.) {
                    for (Map.Entry<String, Location> entry : locations.entrySet()) {
                        Location l = entry.getValue();
                        l.provider.setRadius(52);
                        l.overlay.clearTileCache();
                    }
                    flag = true;
                } else if (map.getCameraPosition().zoom > 16) {
                    for (Map.Entry<String, Location> entry : locations.entrySet()) {
                        Location l = entry.getValue();
                        l.provider.setRadius(30);
                        l.overlay.clearTileCache();
                    }
                    flag = true;
                } else {
                    if (flag) {
                        for (Map.Entry<String, Location> entry : locations.entrySet()) {
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
        map.setOnMapLongClickListener(this::addFavoriteDialog);

        /* On marker click listener to view a favorite place */
        map.setOnMarkerClickListener(marker -> {
            viewFavoriteDialog(marker.getPosition());
            return false;
        });

        User user = User.getInstance(this);
        for (FavoritePlace fav : Objects.requireNonNull(user.getFavoriteLocations()).values()) { //For each saved favorite place
            Marker m = addFavoritePlaceToMap(fav); //Add favorite to Map
            savedPlaces.put(fav.getGeoHash(), m); //Save the Marker of this map
        }

    }

    /* Map configuration, inherited by the Maps Activity superclass */
    private void setUpMap() {
        ((SupportMapFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.map))).getMapAsync(this);
    }

    /* Location passed here should be crowded. It renders it on the map and saves it locally */
    public void addLocationToMap(Location location) {
        locations.put(location.getCoordinates(), location); //save new location locally
        Gradient g = AuxCrowd.crowdTypeToGradient(location.getNrDevices()); //Gradient type based on devices number
        //Here we need to create an ArrayList of LatLng in order to render them on map
        ArrayList<LatLng> l = new ArrayList<>();
        l.add(location.getLatLng());
        location.provider = new HeatmapTileProvider.Builder()
                .data(l)
                .radius(30)
                .build();
        location.provider.setGradient(g);
        location.provider.setOpacity(OPACITY);
        location.overlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(location.provider)); //Adding the HeatMap on the Map
    }

    /* Removes Location from HeatMap */
    public void removeLocationFromMap(Location location) {
        Location removedLocation = locations.remove(location.getCoordinates()); //Deleting the entry corresponding to this location
        if (removedLocation != null) {
            //Removing it from the map
            removedLocation.overlay.remove();
            removedLocation.overlay.clearTileCache();
        }
    }

    /* Renders a FavoritePlace to Map
     * It returns the created marker of this Favorite Place
     */
    public Marker addFavoritePlaceToMap(FavoritePlace place) {
        WGS84Point point = GeoHash.fromGeohashString(place.getGeoHash()).getOriginatingPoint();
        LatLng coordinates = new LatLng(point.getLatitude(), point.getLongitude());
        //Resizing the favorite marker icon
        Bitmap favoritePinpoint = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.favorite_marker), 100, 120, false);
        return map.addMarker(new MarkerOptions() //Rendering Marker on the map at the corresponding Favorite Place
                .position(coordinates)
                .icon(BitmapDescriptorFactory.fromBitmap(favoritePinpoint)));
    }

    /* Removes a FavoritePlace from Map + from local storage */
    public void removeFavoritePlace(String geoHash) {
        User.getInstance(this).removeFavoritePlace(geoHash, this); //Remove from local storage
        Marker m = savedPlaces.get(geoHash); //Get Marker of this favorite location
        if (m != null) //Avoiding a NullPointer Exception
            m.remove(); //Remove the marker from Map
    }

    /* Enable the listeners for new locations added/removed/updated for the current Area of the user and the neighbour Areas */
    private void enableAreaEventListener() {
        //Event listener for an area
        areaEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NotNull DataSnapshot dataSnapshot, String previousChildName) {
                //New location added on this area
                Location location = new Location(dataSnapshot.getKey(), Integer.parseInt(String.valueOf(dataSnapshot.child("nrDevices").getValue())));
                if (AuxCrowd.isCrowd(location.getNrDevices())) { //If this location is a crowd
                    addLocationToMap(location); //Add location to Map
                    Log.d("added", "onChildAdded:" + dataSnapshot.getKey() + " " + dataSnapshot.getValue());
                }
            }

            @Override
            public void onChildChanged(@NotNull DataSnapshot dataSnapshot, String previousChildName) {
                //Location got updated. To update it on map, we get it as new location and render it, removing the old data
                Location newLocationData = new Location(dataSnapshot.getKey(), Integer.parseInt(String.valueOf(dataSnapshot.child("nrDevices").getValue())));
                Location oldLocationData = locations.get(dataSnapshot.getKey());
                if (oldLocationData != null)
                    removeLocationFromMap(oldLocationData); //Remove the old data
                if (AuxCrowd.isCrowd(newLocationData.getNrDevices())) { //If this location is a crowd
                    addLocationToMap(newLocationData); //Add the updated location on the map
                    Log.d("changed", "onChildChanged:" + dataSnapshot.getKey() + " " + dataSnapshot.getValue());
                }
            }

            @Override
            public void onChildRemoved(@NotNull DataSnapshot dataSnapshot) {
                //A location got removed from this area
                Location location = new Location(dataSnapshot.getKey(), Integer.parseInt(String.valueOf(dataSnapshot.child("nrDevices").getValue())));
                if (locations.containsKey(location.getCoordinates())) { //If location is present on our HashMap
                    removeLocationFromMap(location); //Remove the location from the map
                    Log.d("removed", "onChildRemoved:" + dataSnapshot.getKey() + " " + dataSnapshot.getValue());
                }
            }

            @Override
            public void onChildMoved(@NotNull DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("moved", "onChildMoved:" + dataSnapshot.getKey() + " " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w("cancelled", "error", databaseError.toException());
            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference().child(currentArea.getCoordinates()); //Gets a node reference for the current Area
        mDatabase.addChildEventListener(areaEventListener); //Enables the area event listener
        //Enable the listener for each neighbour area
        for (int i = 0; i < 8; i++) { // N, NE, E, SE, S, SW, W, NW
            mDatabase = FirebaseDatabase.getInstance().getReference().child(neighbourArea[i].getCoordinates()); //Gets a node reference for each neighbour area
            mDatabase.addChildEventListener(areaEventListener); //Enables the area event listener for this neighbour
        }
    }

    /* Disable events listener for the current Area (Users current Area) and neighbour Areas, if they exist. They method should be called on Activity finish */
    private void disableAreaEventListner() {
        if (mDatabase != null) {
            mDatabase.removeEventListener(areaEventListener); //Disable current Area listener
            if (neighbourArea != null)
                //For each neighbour area, disable its corresponding listener */
                for (int i = 0; i < 8; i++) { // N, NE, E, SE, S, SW, W, NW
                    if (neighbourArea[i] != null) {
                        mDatabase = FirebaseDatabase.getInstance().getReference().child(neighbourArea[i].getCoordinates()); //Gets a node reference for each neighbour area
                        mDatabase.removeEventListener(areaEventListener); //Disables the listener to this reference
                    }
                }
        }
    }

    /* Shows a dialog where the user can save the Place to its Favorite Places and render the Marker on the Map */
    private void addFavoriteDialog(LatLng coordinates) {
        String geoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Location.PRECISION); //GeoHash of passed Location (Precision 8)

        //Get pretty printed street name using Google Maps API
        Geocoder geocoder = new Geocoder(this);
        List<Address> matches = new ArrayList<>();
        try {
            matches = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1);
        } catch (Exception ignored) {
        }
        String streetName = (matches.isEmpty() ? "" : matches.get(0).getAddressLine(0));

        final LovelyCustomDialog addFavDialog = new LovelyCustomDialog(this);
        addFavDialog.setView(R.layout.add_favorite);
        addFavDialog.setIcon(R.drawable.favorite_icon);
        addFavDialog.setTopColorRes(R.color.colorPrimary);
        addFavDialog.configureView(rootView -> {
            ((TextView) rootView.findViewById(R.id.streetNameText)).setText(streetName); //Updates the Street Name

            Button btnSave = rootView.findViewById(R.id.btnSave);
            btnSave.setOnClickListener(view -> { //Saving the new favorite places
                String name = ((EditText) rootView.findViewById(R.id.nameEditText)).getText().toString();
                Boolean notified = ((CheckBox) rootView.findViewById(R.id.notificationsCheckBox)).isChecked();
                FavoritePlace favorite = new FavoritePlace(geoHash, name, notified); //Creating the new fav place

                User.getInstance(this).addFavoritePlace(favorite, this); //Saving it on local storage
                Marker m = addFavoritePlaceToMap(favorite); //Adding the favorite to Map
                savedPlaces.put(favorite.getGeoHash(), m); //Save the Marker of this map

                Toast.makeText(getApplicationContext(), R.string.success_added, Toast.LENGTH_LONG).show();
                addFavDialog.dismiss();

            });

            Button btnCancel = rootView.findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(view -> { //Canceling the dialog
                addFavDialog.dismiss();
            });
        });
        addFavDialog.show();
    }

    /* Shows the details of this Favorite Place in a dialog view */
    private void viewFavoriteDialog(LatLng coordinates) {
        String geoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Location.PRECISION); //geoHash of Location (8 Precision)
        String areaGeoHash = GeoHash.geoHashStringWithCharacterPrecision(coordinates.latitude, coordinates.longitude, Area.PRECISION); //geoHash of Area (4 Precision)
        FavoritePlace fav = User.getInstance(this).getFavoriteLocation(geoHash);
        String namePlace = "Favorite Place";
        if (fav != null && !fav.getPlaceName().equals(""))
            namePlace = fav.getPlaceName();

        //Get pretty printed street name using Google Maps API
        Geocoder geocoder = new Geocoder(this);
        List<Address> matches = new ArrayList<>();
        try {
            matches = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1);
        } catch (Exception ignored) {
        }
        String streetName = (matches.isEmpty() ? "" : matches.get(0).getAddressLine(0));

        /* Creating the dialog */
        LovelyCustomDialog viewDialog = new LovelyCustomDialog(this);
        viewDialog.setView(R.layout.view_favorite);
        viewDialog.setTopColorRes(R.color.colorPrimary);
        viewDialog.setIcon(R.drawable.favorite_icon);
        String finalNamePlace = namePlace;
        viewDialog.configureView(rootView -> {
            TextView crowdedText = rootView.findViewById(R.id.crowdedText);
            TextView lastUpdateText = rootView.findViewById(R.id.lastUpdateText);
            TextView approximationText = rootView.findViewById(R.id.approximationText);

            //Gets a reference on the real time database of this location
            DatabaseReference locationReference = FirebaseDatabase.getInstance().getReference().child(areaGeoHash).child(geoHash);
            //Enables the listener (callback)
            locationReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String crowd, approximation, lastUpdate;
                    if (snapshot.exists()) { // Data present on database for this favorite location
                        int crowdType = AuxCrowd.crowdTypeToString(snapshot);
                        int nrDevices = ((Long) Objects.requireNonNull(snapshot.child("nrDevices").getValue())).intValue();
                        crowd = getString(R.string.crowded) + " " + getString(crowdType);
                        approximation = getString(R.string.approximation) + " " + nrDevices + " " + getString(R.string.persons); //Approximated people
                        lastUpdate = getString(R.string.last_update) + " " + AuxDateTime.getLastSeen(snapshot) + " " + getString(R.string.minutes_ago); //Last seen in minutes
                    } else { //No data
                        crowd = getString(R.string.crowded) + " " + getString(R.string.no_data);
                        approximation = getString(R.string.approximation) + " " + getString(R.string.no_data);
                        lastUpdate = getString(R.string.last_update) + " " + getString(R.string.no_data);
                    }
                    crowdedText.setText(crowd); //update crowded text
                    approximationText.setText(approximation); //update approximation
                    lastUpdateText.setText(lastUpdate); //update last seen in minutes
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            ((TextView) rootView.findViewById(R.id.streetNameText)).setText(streetName); //Sets the street Name
            ((TextView) rootView.findViewById(R.id.favText)).setText(finalNamePlace); //Name of this favorite location

            Button btnRemove = rootView.findViewById(R.id.btnRemove);
            btnRemove.setOnClickListener(view -> { //Removing a favorite place
                removeFavoritePlace(geoHash); //Remove from Map + Remove from local storage
                Toast.makeText(getApplicationContext(), R.string.success_removed, Toast.LENGTH_LONG).show();
                viewDialog.dismiss();
            });

            Button btnCancel = rootView.findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(view -> { //Cancel the dialog
                viewDialog.dismiss();
            });

            CheckBox notificationCheckBox = rootView.findViewById(R.id.notifications);
            if (fav != null) {
                notificationCheckBox.setChecked(fav.getReceiveNotifications());
                notificationCheckBox.setOnClickListener(view -> {
                    fav.setReceiveNotifications(notificationCheckBox.isChecked());
                    User.getInstance(getApplicationContext()).removeFavoritePlace(fav.getGeoHash(), getApplicationContext());
                    User.getInstance(getApplicationContext()).addFavoritePlace(fav, getApplicationContext());
                });
            }
        });
        viewDialog.show();
    }

    /* Enables the blue dot, which represents the users current location, and centers the map on users coordinates */
    private void showLocationOnMap() {
        //Checking for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true); //Enable the blue dot on map

            //Centers the location
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            android.location.Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(15)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    /*  Sets up the current area and neighbour area GeoHash and enables the listeners */
    public void updateCurrentPosition() {
        FusedLocationProviderClient client;
        client = LocationServices.getFusedLocationProviderClient(getApplicationContext()); //Current location
        //Checking for permissions
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<android.location.Location> task = client.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null) {
                    currentArea = new Area(new LatLng(location.getLatitude(), location.getLongitude()));
                    neighbourArea = new Area[8];
                    for (int i = 0; i < 8; i++) //Update neighbour boxes of this area ----- N, NE, E, SE, S, SW, W, NW
                        neighbourArea[i] = new Area(currentArea.getGeoHash().getAdjacent()[i]);
                    enableAreaEventListener(); //enable listeners for current and neighbour area
                }
            });
        }
    }
}