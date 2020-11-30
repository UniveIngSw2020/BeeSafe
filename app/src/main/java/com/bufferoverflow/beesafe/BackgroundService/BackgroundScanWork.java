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

import com.bufferoverflow.beesafe.MapsActivity;
import com.bufferoverflow.beesafe.R;
import com.bufferoverflow.beesafe.Scan;
import com.clj.fastble.BleManager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;


public class BackgroundScanWork extends Service { //IntentService ???

    private boolean activeService = true;
    private static int SCAN_TIME_WITH_UPLOAD = 1;
    private static int SCAN_TIME_WITHOUT_UPLOAD = 1;

    private boolean BLUETOOTH_STATUS = BleManager.getInstance().isBlueEnable();
    private boolean GPS_STATUS = BleManager.getInstance().isBlueEnable();


    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        if (!isGpsEnabled()) {
                            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active", "GPS and Bluetooth not enabled!");
                        }
                        else {
                            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active", "Bluetooth not enabled!");
                        }
                        break;
                    case BluetoothAdapter.STATE_ON:
                        if (!isGpsEnabled()){
                            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active", "GPS not enabled!");
                        }
                        else {
                            AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Is Active", " :D !");
                        }
                        Log.d("TRACING", "Bluetooth ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver gpsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            Log.d("TRACING", "CALLED");
            final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
            if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                Log.d("TRACING", "GPS ON");
                if (!isBluetoothEnabled()) {
                    AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active", "Bluetooth not enabled!");
                }
                else {
                    AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Is Active", " :D !");
                }
            }
            else {
                Log.d("TRACING", "GPS OFF");
                if (!isBluetoothEnabled()){
                    AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active", "GPS and Bluetooth not enabled!");
                }
                else {
                    AppPersistentNotificationManager.getInstance(getApplicationContext()).updateNotification("BeeSafe Not Active", "GPS not enabled!");
                }
            }
        }
    };

    Timer myTimer = new Timer ();

    public BackgroundScanWork(){
        super();
        //super(BackgroundScanWork.class.getSimpleName());
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        //Registering Bluetooth broadcast receiver
        IntentFilter bluetoothFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothBroadcastReceiver, bluetoothFilter);

        IntentFilter gpsFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(gpsBroadcastReceiver, gpsFilter);



        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
        Log.d("TRACING", String.valueOf(state));

        super.onStartCommand(intent, flags, startId);
        startForeground(1, getNotification());
        //Profile.getInstance().activateFavoritePlaceListeners(); //Activate the listeners for saved locations (To get notifications)
        tracingServiceLoop();



        return START_STICKY;
    }

//    protected void onHandleIntent(@Nullable Intent intent) {
//        startForeground(1, getNotification());
//        //Profile.getInstance().activateFavoritePlaceListeners(); //Activate the listeners for saved locations (To get notifications)
//        tracingServiceLoop();
//    }

    private void tracingServiceLoop () {
        TimerTask myTask = new TimerTask () {
            @Override
            public void run () {
                Scan.tracingAlgorithm(getApplicationContext(), Scan.ONLINE);
            }
        };

        myTimer.scheduleAtFixedRate(myTask , 0l, 10*1000); // Runs every 5 mins

//        while (activeService){
//            try{
//                if(activeService){
//                    Log.d("TRACING", "About to call the tracing algorithm");
//                    Scan.tracingAlgorithm(this, Scan.ONLINE);
//                    Log.d("TRACING", "Tracing algorithm finished.");
//                }
//                Thread.sleep(SCAN_TIME_WITH_UPLOAD * 1000);
//            }catch (InterruptedException e){
//                Log.i("TRACING","Thread Interrupted");
//            }
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activeService=false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification getNotification(){
        return App.getMyAppsNotificationManager().getNotification(MapsActivity.class,
                "BeeSafe Tracing Algorithm is Active",
                1,
                false,
                1);
    }

    private boolean isGpsEnabled () {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isBluetoothEnabled () {
        return BleManager.getInstance().isBlueEnable();
    }




}
