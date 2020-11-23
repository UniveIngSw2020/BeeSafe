package com.bufferoverflow.beesafe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bufferoverflow.beesafe.AuxTools.LocaleHelper;
import com.bufferoverflow.beesafe.BackgroundService.BackgroundScanWork;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    //Open Map
    public void openMap (View view) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    //Starting Foreground Service
    public void startService(View view) {
        serviceIntent = new Intent(getApplicationContext(), BackgroundScanWork.class);
        ContextCompat.startForegroundService(this,serviceIntent);
    }

    public void StopService(View view) {

    }

    public void changeLanguage(View view) {
        String langs[] = {"eng", "ita", "alb"};
        List<String> l = new ArrayList<>();
        l.add("eng");
        l.add("ita");
        l.add("alb");

        new LovelyChoiceDialog(this)
                .setTopColorRes(R.color.colorPrimary)
                .setTitle("Language")
                .setIcon(R.drawable.amu_bubble_mask)
                .setMessage("Change language")
                .setItems(l, new LovelyChoiceDialog.OnItemSelectedListener<String>() {
                    @Override
                    public void onItemSelected(int i, String s) {
                        Toast.makeText(getApplicationContext(), s + " " + i, Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

}