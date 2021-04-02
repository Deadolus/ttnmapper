package ch.deadolus.ttnmapper;

import android.bluetooth.BluetoothDevice;

import java.util.Observable;

public class MapperDevice extends Observable {
    private static BluetoothDevice mapper = null;

    public static BluetoothDevice getMapper() {
        return mapper;
    }

    public void setMapper(BluetoothDevice mapper) {

        MapperDevice.mapper = mapper;
        notifyObservers();
    }
}
