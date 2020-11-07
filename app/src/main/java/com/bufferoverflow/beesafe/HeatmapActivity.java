//package com.bufferoverflow.beesafe;
//
///*
// * Copyright 2014 Google Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//
//import android.app.Application;
//import android.graphics.Color;
//import android.text.Html;
//import android.text.method.LinkMovementMethod;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.RawRes;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.TileOverlay;
//import com.google.android.gms.maps.model.TileOverlayOptions;
//import com.google.maps.android.heatmaps.Gradient;
//import com.google.maps.android.heatmaps.HeatmapTileProvider;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Scanner;
//
///**
// * A demo of the Heatmaps library. Demonstrates how the HeatmapTileProvider can be used to create
// * a colored map overlay that visualises many points of weighted importance/intensity, with
// * different colors representing areas of high and low concentration/combined intensity of points.
// */
//public class HeatmapActivity extends MapsActivity {
//
//    private HeatmapTileProvider mProvider;
//    private TileOverlay mOverlay;
//
//    @Override
//    protected void startDemo(boolean isRestore) {
//
//    }
//
//    @Override
//    public void onPointerCaptureChanged(boolean hasCapture) {
//
//    }
//
//    private void addHeatMap() {
//        List<LatLng> latLngs = null;
//
//        // Get the data: latitude/longitude positions of police stations.
//        try {
//            latLngs = readItems(R.raw.police_stations);
//        } catch (JSONException e) {
//            Toast.makeText(getApplicationContext(), "Problem reading list of locations.", Toast.LENGTH_LONG).show();
//        }
//
//        // Create a heat map tile provider, passing it the latlngs of the police stations.
//        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
//                .data(latLngs)
//                .build();
//
//        // Add a tile overlay to the map, using the heat map tile provider.
//        TileOverlay overlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
//    }
//
//    private List<LatLng> readItems() {
//        List<LatLng> myCoordinates = new ArrayList<>();
//        myCoordinates.add(new LatLng(45.503810, 12.260870));
//        myCoordinates.add(new LatLng(45.479740, 12.249590));
//        myCoordinates.add(new LatLng(45.497735, 12.2676424));
//        return myCoordinates;
//    }
//
//}
