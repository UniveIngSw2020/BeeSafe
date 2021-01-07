package com.bufferoverflow.beesafe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bufferoverflow.beesafe.BackgroundService.App;
import com.bufferoverflow.beesafe.BackgroundService.BackgroundScanWork;
import com.clj.fastble.BleManager;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_ALL = 1;
    @SuppressLint("InlinedApi")
    private final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            android.Manifest.permission.ACTIVITY_RECOGNITION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!hasPermissions(PERMISSIONS)) //Doesn't has permissions
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL); //Requesting them
        else {
            if (!App.isServiceActive())
                App.startService(getApplicationContext());
        }
        BleManager.getInstance().init(getApplication());
    }

    public void openMap(View view) {
        if (BackgroundScanWork.isBluetoothEnabled() && BackgroundScanWork.isGpsEnabled(getApplicationContext())) { //Check if Bluetooth and GPS are enabled
            if (!App.isServiceActive()) //if trying to open map without service active, we start the service before opening the map
                App.startService(getApplicationContext());
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        } else
            Toast.makeText(getApplicationContext(), "You need to enable GPS and Bluetooth.", Toast.LENGTH_LONG).show();
    }

    private boolean hasPermissions(String[] permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    public void service(View view) {
        final Button button = findViewById(R.id.serviceButton);
        if (App.isServiceActive()) { //service is active, we need to stop it
            button.setText("Start Service");
            App.stopService(getApplicationContext());
        } else { //service is not active, we need to start it
            button.setText("Stop Service");
            App.startService(getApplicationContext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted.", Toast.LENGTH_SHORT).show();
                App.startService(getApplicationContext()); //Permissions granted, we start the service
            } else {
                Toast.makeText(this, "You need to enable all permissions to use BeeSafe!", Toast.LENGTH_LONG).show();
                if (App.isServiceActive())
                    App.stopService(getApplicationContext()); //Stopping the service
                finish();
            }
        }
    }

    public void privacyPolicy(View view) {
        new LovelyInfoDialog(this)
                .setTopColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.privacy_icon)
                .setTitle("Privacy Policy")
                .setMessage(getString(R.string.privacy_text))
                .show();
    }

    public void help(View view) {
        new LovelyInfoDialog(this)
                .setTopColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.help_icon)
                .setTitle("Help")
                .setMessage(getString(R.string.help_text))
                .show();
    }
}