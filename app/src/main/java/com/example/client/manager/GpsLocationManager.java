package com.example.client.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GpsLocationManager {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Looper workerLooper;
    private Context context;
    private LocationManager locationManager;

    public GpsLocationManager(Context context, Looper workerLooper) {
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.context = context;
        this.workerLooper = workerLooper;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void setupLocationRequest(int accuracy, int intervalSecond, @Nullable LocationCallback innerCallback) throws SecurityException {
        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (innerCallback != null)
                    innerCallback.onLocationResult(locationResult);
            }
        };
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(accuracy);
        locationRequest.setInterval(intervalSecond * 1000);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, callback, workerLooper);
    }

    public boolean isProviderEnbaled() {
        boolean GPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean NET = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return GPS && NET;
    }

    public Location getLastLocation() throws SecurityException, InterruptedException, ExecutionException, TimeoutException, OutdatedLocationException, LocationProviderDisabledException {
        /* Test if the provider is enabled */
        if (!isProviderEnbaled())
            throw new LocationProviderDisabledException();
        /* Query the availability of location */
        Task<LocationAvailability> taskAvailability = fusedLocationProviderClient.getLocationAvailability();
        /* Also query the location at the same time */
        Task<Location> taskLocation = fusedLocationProviderClient.getLastLocation();

        /* If the location availability meet the requirement we set at LocationRequest, we pass the location */
        /* Otherwise we raise a exception as the last known location is probably out-dated */
        LocationAvailability availability = Tasks.await(taskAvailability, 3000, TimeUnit.MILLISECONDS);
        if (availability.isLocationAvailable())
            return Tasks.await(taskLocation, 3000, TimeUnit.MILLISECONDS);
        else
            throw new OutdatedLocationException();
    }

    public final static int permissionRequestCode = 5566;

    public static boolean checkPermissionGranted(Context context) {
        boolean perm1 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean perm2 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return perm1 && perm2;
    }

    public static void ensurePermissionGranted(Activity context) {
        if (!checkPermissionGranted(context)) {
            /* Request permission here */
            ActivityCompat.requestPermissions(context, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            }, permissionRequestCode);
        }
    }

    public class OutdatedLocationException extends Throwable {
        public OutdatedLocationException() {
        }
    }

    public class LocationProviderDisabledException extends Throwable {
        public LocationProviderDisabledException() {
        }
    }
}
