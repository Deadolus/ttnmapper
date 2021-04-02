package ch.deadolus.ttnmapper;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GPSLoggerService extends Service {
    private static final String TAG = GPSLoggerService.class.getName();
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL = 5000;
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private static final long FASTEST_UPDATE_INTERVAL = 5000;
    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL;
    private static final String tag = "GPSLoggerService";
    private static long minTimeMillis = 1000;
    private static long minDistanceMeters = 5;
    private static final float minAccuracyMeters = 50;
    private static BluetoothWriter bluetoothWriter = new BluetoothWriter();
    private final int NOTIFICATION_ID = 0xdead;
    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    private NotificationManager mNotificationManager;

    public static void sendLocation(Location location, Context context) {
        String locationString = location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + "," + location.getAccuracy();
        if (MapperDevice.getMapper() == null) return;
        if (bluetoothWriter == null) {
            bluetoothWriter = new BluetoothWriter();
        }
        bluetoothWriter.writeValue(context, MapperDevice.getMapper(), LoraLEScanner.SERVICE_UUID, LoraLEScanner.CHARACTERISTIC_UUID, locationString.getBytes(), success -> {
            Log.d(TAG, "Written? " + success);
        });
    }


    // Below is the service framework methods

    public static long getMinTimeMillis() {
        return minTimeMillis;
    }

    public static void setMinTimeMillis(long _minTimeMillis) {
        minTimeMillis = _minTimeMillis;
    }

    public static long getMinDistanceMeters() {
        return minDistanceMeters;
    }

    public static void setMinDistanceMeters(long _minDistanceMeters) {
        minDistanceMeters = _minDistanceMeters;
    }

    public static float getMinAccuracyMeters() {
        return minAccuracyMeters;
    }

    /**
     * Called when the activity is first created.
     */

    @SuppressLint("MissingPermission")
    private void startLoggerService() {
        Log.d(TAG, "Starting logger service");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
    }

    private void shutdownLoggerService() {
        Log.d(TAG, "Shut down logger service");
        removeLocationUpdates();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startLoggerService();

        // Display a notification about us starting. We put an icon in the
        // status bar.
        showNotification();
        requestLocationUpdates();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Unbinding GPSLoggerService");
        return super.onUnbind(intent);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroying GPSLoggerService");

        shutdownLoggerService();

        // Cancel the persistent notification.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(NOTIFICATION_ID);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped,
                Toast.LENGTH_SHORT).show();
        notificationManager.cancel(0);
        bluetoothWriter.stop();
        bluetoothWriter = null;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        startForeground(NOTIFICATION_ID, Utils.getNotification(this, "TTNMapper background service running"));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "On bind");
        return mBinder;
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

    private PendingIntent getPendingIntent() {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates");
            Utils.setRequestingLocationUpdates(this, true);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            Utils.setRequestingLocationUpdates(this, false);
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        Utils.setRequestingLocationUpdates(this, false);
        mFusedLocationClient.removeLocationUpdates(getPendingIntent());
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        GPSLoggerService getService() {
            return GPSLoggerService.this;
        }
    }

}