package com.bufferoverflow.beesafe.BackgroundService;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.bufferoverflow.beesafe.Scan;
import com.clj.fastble.BleManager;

import java.util.Timer;
import java.util.TimerTask;

public class BackgroundScanWork extends Service {

    private boolean active = false; //true if both gps and bluetooth are On. Checked periodically by the tracing algorithm
    private Timer tracingTimer; //tracing algorithm timer scheduler
    private static final int TRACING_INTERVAL = 10; //time interval between each tracing procedure in minutes

    //Broadcast receiver for Bluetooth status change to on and off
    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_ON:
                        checkGpsBluetooth();
                        break;
                }
            }
        }
    };

    /* Broadcast receiver for GPS status change to on and off */
    private final BroadcastReceiver gpsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            checkGpsBluetooth();
        }
    };

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //Registering Bluetooth broadcast receiver
        IntentFilter bluetoothFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothBroadcastReceiver, bluetoothFilter);

        //Registering Gps broadcast receiver
        IntentFilter gpsFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(gpsBroadcastReceiver, gpsFilter);

        startForeground(1, getNotification()); //Showing foreground notification

        tracingTimer = new Timer (); //Start the timer for the tracing algorithm scheduler
        checkGpsBluetooth(); //Initial check
        startTracing(); //Running tracing algorithm in loop until service gets disabled
        return START_STICKY;
    }

    private void startTracing () {
        BleManager.getInstance().init(getApplication()); //Gets an instance for the bluetooth library
        TimerTask taskTracing = new TimerTask () {
            @Override
            public void run () {
                if (active)
                    Scan.tracingAlgorithm(getApplicationContext());
            }
        };

        //Reschedule the job -> Looping
        tracingTimer.scheduleAtFixedRate(taskTracing , 0L, TRACING_INTERVAL * 60 * 1000);
    }

    /* Called when the service gets stopped manually or by the system */
    @Override
    public void onDestroy() {
        super.onDestroy();
        tracingTimer.cancel(); //Canceling the scheduler
        unregisterReceiver(bluetoothBroadcastReceiver); //Unregistering the broadcast of bluetooth status change
        unregisterReceiver(gpsBroadcastReceiver); //Unregistering the broadcast of gps status change
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* Initial notification when the service starts */
    private Notification getNotification(){
        return App.getMyAppsNotificationManager(getApplicationContext()).getNotification();
    }

    /* Checks if GPS is enabled */
    public static boolean isGpsEnabled (Context c) {
        LocationManager locationManager = (LocationManager) c.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /* Checks if Bluetooth is enabled */
    public static boolean isBluetoothEnabled() {
        return BleManager.getInstance().isBlueEnable();
    }

    /* Check status of gps and bluetooth, and then update the foreground notification
     * Should be called when the service gets started for the first time, and when by the
     * broadcast receivers when status of at least one of these changes
     */
    private void checkGpsBluetooth () {
        boolean gps = isGpsEnabled(getApplicationContext());
        boolean bluetooth = isBluetoothEnabled();
        active = gps && bluetooth;

        if (!gps && !bluetooth)
            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active", "GPS and Bluetooth not enabled!");
        else if (!gps)
            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active", "GPS not enabled!");
        else if (!bluetooth)
            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active", "Bluetooth not enabled!");
        else
            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Is Active", "Scanning!");

    }





}
