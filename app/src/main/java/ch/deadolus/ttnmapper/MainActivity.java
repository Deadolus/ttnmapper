package ch.deadolus.ttnmapper;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.FOREGROUND_SERVICE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL = 1000; // Every 60 seconds.
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private static final long FASTEST_UPDATE_INTERVAL = 1000; // Every 30 seconds
    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL; // Every 5 minutes.
    static Fragment navHost = null;
    private static Context applicationContext = null;
    //private static LocationListener mLocationListener = null;
    private static GPSLoggerService mService;
    private static boolean mBound = false;
    private static final MapperDevice mapperDevice = new MapperDevice();
    private static boolean fromNotification = false;
    FloatingActionButton fab = null;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    public static void setMapper(BluetoothDeviceDetails device) {
        if (mapperDevice != null) {
            mapperDevice.setMapper(device.device);
        }
        Log.d(TAG, "Assigned mapper device");
        startService();
    }

    @SuppressLint("NewApi")
    public static void startService() {
        Log.d(TAG, "Binding to service...");
        Intent intent = new Intent(applicationContext, GPSLoggerService.class);
        applicationContext.startForegroundService(intent);
        mBound = true;
    }

    public static void stopService() {
        if (mBound) {
            Log.d(TAG, "Unbinding service");
            applicationContext.stopService(new Intent(applicationContext, GPSLoggerService.class));
            mBound = false;
            if (navHost == null) {
                Log.e(TAG, "navHost not found");
                return;
            }
            mapperDevice.setMapper(null);
            NavController test = NavHostFragment.findNavController(navHost);
            //Log.d(TAG, "Find node?"+test.getGraph().);

            NavHostFragment.findNavController(navHost).navigateUp();
            //.navigate(R.id.action_SecondFragment_to_FirstFragment);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        applicationContext = getApplicationContext();
        requestAppPermission();
        navHost = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fab = findViewById(R.id.fab);
        FirstFragment.setFab(fab);
        Button startScan = findViewById(R.id.startScan);
        startScan.setOnClickListener(d -> {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
            scanner.startScan(new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    Log.i(TAG, "Found Device: " + result.getDevice().getName());

                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Log.i(TAG, "error");
                }
            });

        });

        fab.setOnClickListener(view -> {

            if (navHost == null) {
                Log.e(TAG, "navHost not found");
                return;
            }
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth not enabled!");
                Toast.makeText(getApplicationContext(), "Please enable bluetooth to search for Bluetooth devices", Toast.LENGTH_SHORT).show();
                stopService();
                return;
            }
            NavHostFragment.findNavController(navHost)
                    .navigate(R.id.action_FirstFragment_to_itemFragment2);

        });

        Boolean menuFragment = getIntent().getBooleanExtra("from_notification", false);


        // If menuFragment is defined, then this activity was launched with a fragment selection
        if (menuFragment != false) {
            Log.d(TAG, "Opened from notification, mapper" + MapperDevice.getMapper());
            fromNotification = true;
        } else {
            Log.d(TAG, "Not opened by notification");
            // Activity was not launched with a menuFragment selected -- continue as if this activity was opened from a launcher (for example)

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestAppPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED

        ) {
            Log.d(TAG, "All permissions granted");
            // You can use the API that requires the permission.
            //performAction(...);
            // else if (shouldShowRequestPermissionRationale(...)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            //showInContextUI(...);
            Log.d(TAG, "ACCESS_FINE_LOCATION: " + ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION));
        } else {
            Log.d(TAG, "Requesting permissions");
            Log.d(TAG, "BLUETOOTH_ADMIN: " + ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH_ADMIN));
            Log.d(TAG, "BLUETOOTH: " + ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH));
            Log.d(TAG, "ACCESS_BACKGROUND_LOCATION: " + ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_BACKGROUND_LOCATION));
            Log.d(TAG, "ACCESS_COARSE_LOCATION: " + ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION));
            Log.d(TAG, "ACCESS_FINE_LOCATION: " + ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION));
            requestPermissions(new String[]{
                    BLUETOOTH_ADMIN,
                    BLUETOOTH,
                    ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION,
                    FOREGROUND_SERVICE
            }, 1);


        }

    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void requestBackgroundPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Background permissions granted");
            // You can use the API that requires the permission.
            //performAction(...);
        } else if (shouldShowRequestPermissionRationale(ACCESS_BACKGROUND_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            showExplanation("Background location needed", "We need background location", ACCESS_BACKGROUND_LOCATION, 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "New intent received");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroying Main Activity");
        if (!fromNotification) {
            stopService();
        }
        fromNotification = false;

    }

}