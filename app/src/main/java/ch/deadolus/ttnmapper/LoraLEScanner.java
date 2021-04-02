package ch.deadolus.ttnmapper;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class LoraLEScanner extends ScanCallback {
    public final static UUID SERVICE_UUID = UUID.fromString("fe40d83e-380a-4d96-964d-b9a8210e7677");
    public final static UUID CHARACTERISTIC_UUID = UUID.fromString("9d061862-437d-4d7a-9fd4-6b611e5eb496");
    private static final String TAG = LoraLEScanner.class.getName();
    static BluetoothDeviceRecyclerViewAdapter adapter;
    private final List<BluetoothDeviceDetails> mValues;

    LoraLEScanner(BluetoothDeviceRecyclerViewAdapter adapter, List<BluetoothDeviceDetails> values) {
        LoraLEScanner.adapter = adapter;
        mValues = values;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        BluetoothDevice device = result.getDevice();
        if (device.getName() == null) return;
        if (mValues.stream().anyMatch(d -> d.mac.equals(device.getAddress()))) return;
        Log.d(TAG, "New bluetooth device found: " + result.getDevice().getName());
        mValues.add(new BluetoothDeviceDetails(device.getAddress(), device.getName(), String.valueOf(device.getType()), device));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        Log.e(TAG, "Scan failed with error code: " + errorCode);

    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
        Log.d(TAG, "New batch scan results");
    }
}
