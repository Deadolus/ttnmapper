package ch.deadolus.ttnmapper;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceDetails {
    public final String mac;
    public final String name;
    public final String details;
    public final BluetoothDevice device;

    public BluetoothDeviceDetails(String mac, String name, String details, BluetoothDevice device) {
        this.mac = mac;
        this.name = name;
        this.details = details;
        this.device = device;
    }

    @Override
    public String toString() {
        return name;
    }
}

