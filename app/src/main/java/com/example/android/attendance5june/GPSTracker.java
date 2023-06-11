package com.example.android.attendance5june;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class  GPSTracker extends Service implements LocationListener {
    public static final String TAG = GPSTracker.class.getSimpleName();

    Context context;
    boolean canGetLocation = false;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    Location location;
    double latitude;
    double longitude;
    boolean result;
    protected LocationManager locationManager;
    AlertDialog.Builder alertDialog;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    public GPSTracker(){ }

    public GPSTracker(Context context){
        this.context = context;
        alertDialog = new AlertDialog.Builder(context);

    }

    public boolean checkPermission(Context context){
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showSettingsAlert();
                } else {
                    showSettingsAlert();
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public void showSettingsAlert() {
        // Setting DialogHelp Title
        alertDialog.setTitle("GPS is required");

        // Setting DialogHelp Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        // Showing Alert Message
        alertDialog.show();
    }

    public Location getLocation(){
        if(!result){
            result  = checkPermission(context);
            if(!result){
                Toast.makeText(context,"Cannot access location, GPS permission not granted", Toast.LENGTH_LONG);
            }
        }
        try{
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
//            boolean backgroundLocationPermissionApproved = ActivityCompat.checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.e(TAG, isGPSEnabled+" "+isNetworkEnabled);
            if(!isGPSEnabled && !isNetworkEnabled){
                int requestPermission = 50;
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestPermission);
                    getLocation();      // ???
                }
            }
            else{
                this.canGetLocation = true;
                if(isNetworkEnabled){
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if(locationManager != null){
//                        Log.e(TAG, "network location manager not null");
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(location != null) {
//                            Log.e(TAG, "network location manager got location");
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                else{
                    if(location ==null){
//                        Log.e(TAG, "failed to get location via network");
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if(locationManager!=null){
//                            Log.e(TAG, "location manager not null for gps");
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if(location != null){
//                                Log.e(TAG, "gps location manager got location");
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    // lat - 37.42342342342342, long - -122.08395287867832
}
