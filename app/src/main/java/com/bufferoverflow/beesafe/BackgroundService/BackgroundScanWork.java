package com.bufferoverflow.beesafe.BackgroundService;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bufferoverflow.beesafe.MainActivity;
import com.bufferoverflow.beesafe.MapsActivity;
import com.bufferoverflow.beesafe.R;
import com.bufferoverflow.beesafe.Scan;
import com.clj.fastble.BleManager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;


public class BackgroundScanWork extends Service { //IntentService ???

    private boolean active = false;
    private static int SCAN_TIMER_MINUTES = 1;

    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state) { //Service
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_ON:
                        checkGpsBluetooth();
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver gpsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
            checkGpsBluetooth();
        }
    };

    Timer myTimer = new Timer ();

    public BackgroundScanWork(){
        super();
        //super(BackgroundScanWork.class.getSimpleName());
    }

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
        //TODO  Profile.getInstance().activateFavoritePlaceListeners(); //Activate the listeners for saved locations (To get notifications)

        checkGpsBluetooth(); //Initial check
        tracingServiceLoop(); //Running tracing algorithm in loop until service gets disabled
        return START_STICKY;
    }

//    protected void onHandleIntent(@Nullable Intent intent) {
//        startForeground(1, getNotification());
//        //Profile.getInstance().activateFavoritePlaceListeners(); //Activate the listeners for saved locations (To get notifications)
//        tracingServiceLoop();
//    }

    private void tracingServiceLoop () {
        TimerTask tracingTimer = new TimerTask () {
            @Override
            public void run () {
                if (active) {
                    Log.d("TRACING", "About to call the tracing algorithm");
                    Scan.tracingAlgorithm(getApplicationContext());
                    Log.d("TRACING", "Tracing algorithm finished.");
                }

            }
        };

        //Reschedule the job -> Looping
        myTimer.scheduleAtFixedRate(tracingTimer , 0L, SCAN_TIMER_MINUTES * 60 * 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myTimer.cancel(); //Canceling the scheduler
        unregisterReceiver(bluetoothBroadcastReceiver); //Unregistering the broadcast of bluetooth status change
        unregisterReceiver(gpsBroadcastReceiver); //Unregistering the broadcast of gps status change
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification getNotification(){
        return App.getMyAppsNotificationManager().getNotification(MapsActivity.class,
                "BeeSafe Is Active",
                1,
                false,
                1);
    }

    /* Checks if GPS is enabled */
    public static boolean isGpsEnabled (Context c) {
        LocationManager locationManager = (LocationManager) c.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /* Checks if Bluetooth is enabled *?
    public static boolean isBluetoothEnabled() {
        return BleManager.getInstance().isBlueEnable();
    }

    private void checkGpsBluetooth () {
        boolean gps = isGpsEnabled(getApplicationContext());
        boolean bluetooth = isBluetoothEnabled();
        active = gps && bluetooth;

        if (!gps && !bluetooth)
            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active \uD83E\uDD7A", "GPS and Bluetooth not enabled!");
        else if (!gps && bluetooth)
            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active \uD83E\uDD7A", "GPS not enabled!");
        else if (gps && !bluetooth)
            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active \uD83E\uDD7A", "Bluetooth not enabled!");
        else
            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Is Active \uD83D\uDC1D", "Scanning!");

    }





}
