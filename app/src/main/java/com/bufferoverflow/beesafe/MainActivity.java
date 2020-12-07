package com.bufferoverflow.beesafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bufferoverflow.beesafe.AuxTools.AuxDateTime;
import com.bufferoverflow.beesafe.BackgroundService.App;
import com.bufferoverflow.beesafe.BackgroundService.AppPersistentNotificationManager;
import com.bufferoverflow.beesafe.BackgroundService.BackgroundScanWork;
import com.clj.fastble.BleManager;
import com.google.android.gms.maps.model.Marker;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
TODO:
   Listeners for saved places
   Privacy Policy
   Help
   -Service Stop/Start Notification + Buttons :
   -Service Notification Change for current place (offline) [nr devices using only bluetooth]
   -Register broadcast listener for bluetooht/gps on off


 */

public class MainActivity extends AppCompatActivity {

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            android.Manifest.permission.ACTIVITY_RECOGNITION
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean permissions = hasPermissions(PERMISSIONS);
        if (!permissions)
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        else
            App.startService(getApplicationContext());
        BleManager.getInstance().init(getApplication());
    }

    //Open Map
    public void openMap (View view) {
        if (BackgroundScanWork.isBluetoothEnabled() && BackgroundScanWork.isGpsEnabled(getApplicationContext())) { //Check if Bluetooth and GPS are enabled
            if (!App.isServiceActive())
                App.startService(getApplicationContext());
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        }
        else
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

    public void service(View view) {
        final Button button = findViewById(R.id.serviceButton);
        if (App.isServiceActive()){ //service is active, we need to stop it
            button.setText("Start Service");
            App.stopService(getApplicationContext());
        }
        else { //service is not active, we need to start it
            button.setText("Stop Service");
            App.startService(getApplicationContext());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Permissions Granted.", Toast.LENGTH_SHORT).show();
                App.startService(getApplicationContext()); //Permissions granted, we start the service
            }
            else {
                Toast.makeText(this,"You need to enable all permissions to use BeeSafe!", Toast.LENGTH_LONG).show();
                if (App.isServiceActive())
                    App.stopService(getApplicationContext()); //Stoping service
                finish();
            }
        }
    }

    public void privacyPolicy(View view) {
        new LovelyInfoDialog(this)
                .setTopColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.privacy_icon)
                .setTitle("Privacy Policy")
                .setMessage("BufferOverflow built the BeeSafe app as a Free app. This SERVICE is provided by BufferOverflow at no cost and is intended for use as is.\n" +
                        "\n" +
                        "This page is used to inform visitors regarding our policies with the collection, use, and disclosure of Personal Information if anyone decided to use our Service.\n" +
                        "\n" +
                        "If you choose to use our Service, then you agree to the collection and use of information in relation to this policy. The Personal Information that we collect is used for providing and improving the Service. We will not use or share your information with anyone except as described in this Privacy Policy.\n" +
                        "\n" +
                        "The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which is accessible at BeeSafe unless otherwise defined in this Privacy Policy.\n" +
                        "\n" +
                        "**Information Collection and Use**\n" +
                        "\n" +
                        "For a better experience, while using our Service, we may require you to provide us with certain personally identifiable information. The information that we request will be retained by us and used as described in this privacy policy.\n" +
                        "\n" +
                        "The app does use third party services that may collect information used to identify you.\n" +
                        "\n" +
                        "Link to privacy policy of third party service providers used by the app\n" +
                        "\n" +
                        "*   [Google Play Services](https://www.google.com/policies/privacy/)\n" +
                        "\n" +
                        "**Log Data**\n" +
                        "\n" +
                        "We want to inform you that whenever you use our Service, in a case of an error in the app we collect data and information (through third party products) on your phone called Log Data. This Log Data may include information such as your device Internet Protocol (“IP”) address, device name, operating system version, the configuration of the app when utilizing our Service, the time and date of your use of the Service, and other statistics.\n" +
                        "\n" +
                        "**Cookies**\n" +
                        "\n" +
                        "Cookies are files with a small amount of data that are commonly used as anonymous unique identifiers. These are sent to your browser from the websites that you visit and are stored on your device's internal memory.\n" +
                        "\n" +
                        "This Service does not use these “cookies” explicitly. However, the app may use third party code and libraries that use “cookies” to collect information and improve their services. You have the option to either accept or refuse these cookies and know when a cookie is being sent to your device. If you choose to refuse our cookies, you may not be able to use some portions of this Service.\n" +
                        "\n" +
                        "**Service Providers**\n" +
                        "\n" +
                        "We may employ third-party companies and individuals due to the following reasons:\n" +
                        "\n" +
                        "*   To facilitate our Service;\n" +
                        "*   To provide the Service on our behalf;\n" +
                        "*   To perform Service-related services; or\n" +
                        "*   To assist us in analyzing how our Service is used.\n" +
                        "\n" +
                        "We want to inform users of this Service that these third parties have access to your Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are obligated not to disclose or use the information for any other purpose.\n" +
                        "\n" +
                        "**Security**\n" +
                        "\n" +
                        "We value your trust in providing us your Personal Information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of transmission over the internet, or method of electronic storage is 100% secure and reliable, and we cannot guarantee its absolute security.\n" +
                        "\n" +
                        "**Links to Other Sites**\n" +
                        "\n" +
                        "This Service may contain links to other sites. If you click on a third-party link, you will be directed to that site. Note that these external sites are not operated by us. Therefore, we strongly advise you to review the Privacy Policy of these websites. We have no control over and assume no responsibility for the content, privacy policies, or practices of any third-party sites or services.\n" +
                        "\n" +
                        "**Children’s Privacy**\n" +
                        "\n" +
                        "These Services do not address anyone under the age of 13. We do not knowingly collect personally identifiable information from children under 13\\. In the case we discover that a child under 13 has provided us with personal information, we immediately delete this from our servers. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact us so that we will be able to do necessary actions.\n" +
                        "\n" +
                        "**Changes to This Privacy Policy**\n" +
                        "\n" +
                        "We may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. We will notify you of any changes by posting the new Privacy Policy on this page.\n" +
                        "\n" +
                        "This policy is effective as of 2020-11-30\n" +
                        "\n" +
                        "**Contact Us**\n" +
                        "\n" +
                        "If you have any questions or suggestions about our Privacy Policy, do not hesitate to contact us at 877028@stud.unive.it.\n" +
                        "\n" +
                        "This privacy policy page was created at [privacypolicytemplate.net](https://privacypolicytemplate.net) and modified/generated by [App Privacy Policy Generator](https://app-privacy-policy-generator.nisrulz.com/)")
                .show();
    }


    public void help(View view) {
        new LovelyInfoDialog(this)
                .setTopColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.help_icon)
                .setTitle("Help")
                .setMessage(
                        "This Android Application is made for the course Software Engineering AA 20/21 Ca' Foscari University. " +
                                "It shows the crowds nearby your location and represent them using HeatMap." +
                                "It has a background service which scans the current location continuously by a tracing algorithm." +
                                "The scan gets uploaded to a realtime database. A location is represented using a GeoHash for privacy purposes" +
                                " and efficiency. No personal data are stored on servers.\n\n" +
                                "The user has the ability to save a favorite location and get notified every time the location gets crowded." +
                                "To add a favorite place, the user should long click on a location on the map. Then, a popup will be displayed " +
                                "where can enter a custom name for that location and enable or disable the notifications." +
                                " To see information about a saved place, the users clicks on the pinpoint created by the app, where a new dialog" +
                                " shows information about this place like the density, last update and an approximation made by the tracing algorithm." +
                                "The user has tha ability to remove a favorite place from saved.\n\n" +
                                "Data on the realtime database gets refreshed often to improve the approximation, and moved to a new database, " +
                                "which holds one-week data. These data is used byt the prevision algorithm to predict a crowd place.\n" +
                                "Predication is not yet supported, and will be available in the Beta version."
                )
                .show();
    }
}