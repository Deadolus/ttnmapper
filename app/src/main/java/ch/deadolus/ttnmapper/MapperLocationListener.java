package ch.deadolus.ttnmapper;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Observable;
import java.util.Optional;

public class MapperLocationListener extends Observable implements LocationListener {
    private static final String TAG = LocationListener.class.getName();
    private static Optional<Location> latestLocation = Optional.empty();
    private static final boolean showingDebugToast = false;
    private Context context = null;
    private int lastStatus = 0;

    MapperLocationListener(Context context) {
        this.context = context;
    }

    public static Optional<Location> getLatestLocation() {
        return latestLocation;
    }

    @Override
    public void onLocationChanged(final Location location) {
        String locationString = location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + "," + location.getAccuracy();
        Log.d(TAG, "New location: " + locationString);
        latestLocation = Optional.of(location);
        setChanged();
        notifyObservers(location);
    }

    public void onProviderDisabled(String provider) {
        if (showingDebugToast) Toast.makeText(context, "onProviderDisabled: " + provider,
                Toast.LENGTH_SHORT).show();

    }

    public void onProviderEnabled(String provider) {
        if (showingDebugToast) Toast.makeText(context, "onProviderEnabled: " + provider,
                Toast.LENGTH_SHORT).show();

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        String showStatus = null;
        if (status == LocationProvider.AVAILABLE)
            showStatus = "Available";
        if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
            showStatus = "Temporarily Unavailable";
        if (status == LocationProvider.OUT_OF_SERVICE)
            showStatus = "Out of Service";
        if (status != lastStatus && showingDebugToast) {
            Toast.makeText(context,
                    "new status: " + showStatus,
                    Toast.LENGTH_SHORT).show();
        }
        lastStatus = status;
    }
}
