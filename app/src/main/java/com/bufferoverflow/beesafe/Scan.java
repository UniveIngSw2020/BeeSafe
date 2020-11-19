package com.bufferoverflow.beesafe;

import android.content.Context;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResponse;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Scan {

    private Context context;
    private Map<String, BleDevice> scanDevices;
    private boolean safeActivity = true;
    private List<String> blacklist = Arrays.asList(
            "TV", "Mi Band", "Airpods", "Buds"
    );
    private BleManager bleManager;
    private CountDownLatch latchFirstScan = new CountDownLatch(1);
    private CountDownLatch latchSecondScan = new CountDownLatch(1);
    private CountDownLatch latchActivityRecognition = new CountDownLatch(1);

    private static int FIRST_SCAN_DURATION = 10; //seconds
    private static int SECOND_SCAN_DURATION = 10; //seconds
    private static int WAIT_BETWEEN_SCANS = 10; //seconds
    private static int RSSI_RANGE_FILTER = -70; //RSSI Signal

    public Scan (Context c) {
        context = c;
        bleManager = BleManager.getInstance();
        scanDevices = new HashMap<>();
    }

    public void tracingAlgorithm() {
        safeActivityRecognition(); //Controls if users Activity is safe to begin scan
        try { latchActivityRecognition.await(1, TimeUnit.MINUTES); } catch (InterruptedException e) { e.printStackTrace(); } //Wait until activity recognition
        if (!safeActivity)
            return;

        Log.d("TRACING", "First Scan started");
        firstScan();
        try { latchFirstScan.await(); } catch (InterruptedException e) { e.printStackTrace(); } //Wait until first scan has finished
        Log.d("TRACING", "Successfully finished first scan");

        //Pause main thread
        try {
            Thread.sleep(WAIT_BETWEEN_SCANS * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("TRACING", "Second Scan started");
        secondScan();
        try { latchSecondScan.await(); } catch (InterruptedException e) { e.printStackTrace(); } //Wait until second scan has finished
        Log.d("TRACING", "Successfully finished second scan");

        filterManufacturer();
        filterRange();
        //uploadResult(getDevicesNumber());
    }

    private void firstScan () {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setScanTimeOut(FIRST_SCAN_DURATION * 1000)
                .build();
        bleManager.initScanRule(scanRuleConfig);
        bleManager.scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) { Log.d("TRACING", "First Scan Started"); }
            @Override
            public void onScanning(BleDevice bleDevice) {
                Log.d("TRACING", "FOUND Indirizzo MAC: " + bleDevice.getMac() + " | Nome: " + bleDevice.getName() + " | Signal : " + bleDevice.getDevice().getAlias() + " " + bleDevice.getRssi());
            }
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                for (BleDevice b : scanResultList) //Saving first scan results
                    scanDevices.put(b.getMac(), b);
                    //firstScanResult.put(b.getMac(), b);
                Log.d("TRACING", "First Scan Finished : " + scanDevices.size() + " devices");
                Log.d("TRACING", "Devices(First Scan): ");
                for (Map.Entry<String, BleDevice> entry : scanDevices.entrySet())
                    Log.d("TRACING", "Indirizzo MAC: " + entry.getValue().getMac() + " | Nome: " + entry.getValue().getName());
                latchFirstScan.countDown(); //Release Main Thread
            }
        });
    }

    private void secondScan() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setScanTimeOut(SECOND_SCAN_DURATION * 1000)
                .build();
        bleManager.initScanRule(scanRuleConfig);
        bleManager.scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) { Log.d("TRACING", "Second Scan Started"); }
            @Override
            public void onScanning(BleDevice bleDevice) { Log.d("TRACING", "FOUND Indirizzo MAC: " + bleDevice.getMac() + " | Nome: " + bleDevice.getName()); }
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                Map<String, BleDevice> secondScanResult = new HashMap<>();
                for (BleDevice b : scanResultList)
                    secondScanResult.put(b.getMac(), b);

                Log.d("TRACING", "Devices(Second Scan): ");
                for (Map.Entry<String, BleDevice> entry : secondScanResult.entrySet())
                    Log.d("TRACING", "Indirizzo MAC: " + entry.getValue().getMac() + " | Nome: " + entry.getValue().getName());

                //Removing the bluetooth devices not present on the second scan
                for (Iterator<Map.Entry<String, BleDevice>> it = scanDevices.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, BleDevice> b = it.next();
                    if(!secondScanResult.containsKey(b.getKey())) {
                        Log.d("TRACING", "Removing ||| Indirizzo MAC: " + b.getValue().getMac() + " | Nome: " + b.getValue().getName());
                        it.remove();
                    }
                }

                Log.d("TRACING", "Devices(Final Result): ");
                for (Map.Entry<String, BleDevice> entry : scanDevices.entrySet())
                    Log.d("TRACING", "Indirizzo MAC: " + entry.getValue().getMac() + " | Nome: " + entry.getValue().getName());

                latchSecondScan.countDown();
            }
        });
    }

    /* Checks if use is on car, public transport, general vehicle, bicycle or running.
       If this is the case then we return false to skip the scan this time
     */
    private void safeActivityRecognition () {
        Awareness.getSnapshotClient(context).getDetectedActivity()
                .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                    @Override
                    public void onSuccess(DetectedActivityResponse dar) {
                        ActivityRecognitionResult arr = dar.getActivityRecognitionResult();
                        // getMostProbableActivity() is good enough for basic Activity detection.
                        DetectedActivity probableActivity = arr.getMostProbableActivity();

                        int currentActivity = probableActivity.getType();
                        if (currentActivity == DetectedActivity.IN_VEHICLE ||
                            currentActivity == DetectedActivity.ON_BICYCLE ||
                            currentActivity == DetectedActivity.RUNNING)
                            safeActivity = false;

                        int confidence = probableActivity.getConfidence();
                        String activityStr = probableActivity.toString();
                        Log.d("TRACING", "Activity: " + activityStr + ", Confidence: " + confidence + "/100");
                        latchActivityRecognition.countDown();
                    }
                });
    }

    /* Returns the number of the devices scanned */
    public int getDevicesNumber () {
        return scanDevices.size();
    }

//    public void uploadResult(int nrDevices) {
//        Location l = new Location(new LatLng(45.503810, 12.260870), getDevicesNumber());
//        Profile.getInstance(context).getCurrentArea().addLocation(l);
//    }

    // tries to minimize errors by blacklisting devices that are not phones
    public void filterManufacturer () {
        for(String type : blacklist)
            for (Iterator<Map.Entry<String, BleDevice>> it = scanDevices.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, BleDevice> b = it.next();
                if (b.getValue().getName().contains(type))
                    scanDevices.remove(b.getKey());
            }
    }

    //tries to minimize errors by filtering out the devices in a large range
    public void filterRange() {
        for (Iterator<Map.Entry<String, BleDevice>> it = scanDevices.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, BleDevice> b = it.next();
            if (b.getValue().getRssi() <= RSSI_RANGE_FILTER)
                scanDevices.remove(b.getKey());
        }
    }
}
