package com.example.smart_dosage.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationWatcher {
    private static FusedLocationProviderClient client;
    private static LocationCallback callback;

    @SuppressLint("MissingPermission")
    public static void start(Context context) {
        if (client == null) client = LocationServices.getFusedLocationProviderClient(context);
        LocationRequest req = LocationRequest.create().setInterval(60_000).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null) return;
                Location loc = result.getLastLocation();
                if (loc == null) return;
                SharedPreferences sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                float lastLat = sp.getFloat("last_lat", Float.NaN);
                float lastLon = sp.getFloat("last_lon", Float.NaN);
                if (!Float.isNaN(lastLat) && !Float.isNaN(lastLon)) {
                    float[] dist = new float[1];
                    android.location.Location.distanceBetween(lastLat, lastLon, (float) loc.getLatitude(), (float) loc.getLongitude(), dist);
                    if (dist[0] > 5000) {
                        sp.edit().putLong("location_changed_at", System.currentTimeMillis()).apply();
                    }
                }
                sp.edit().putFloat("last_lat", (float) loc.getLatitude()).putFloat("last_lon", (float) loc.getLongitude()).apply();
            }
        };
        client.requestLocationUpdates(req, callback, android.os.Looper.getMainLooper());
    }

    public static void stop(Context context) {
        if (client != null && callback != null) client.removeLocationUpdates(callback);
    }
}
