package com.example.weathertunes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    private static boolean ACCESS_FINE_LOCATION_GRANTED = false;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int hasReadContactPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
        Log.d("MYR", "HAS Read Location Permissions "+hasReadContactPermission);

        if(hasReadContactPermission == PackageManager.PERMISSION_GRANTED){
            Log.d("MYR", "Permission Granted");
            ACCESS_FINE_LOCATION_GRANTED = true;
        }
        else{
            Log.d("MYR", "Requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
        }

        if(ACCESS_FINE_LOCATION_GRANTED) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Log.v("ΜΥR", "Longitude: " + Double.toString(location.getLongitude()));
                                Log.v("ΜΥR", "Latitude: " + Double.toString(location.getLatitude()));


                            }
                        }
                    });
                }
                else{
                    //do something when permission isn't granted
                }
            }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("MYR", "Permission Granted!");
                    ACCESS_FINE_LOCATION_GRANTED = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("MYR", "Permission refused");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
