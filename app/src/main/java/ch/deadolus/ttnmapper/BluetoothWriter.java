package ch.deadolus.ttnmapper;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Will try to write a value to a UUID
 */
public class BluetoothWriter {
    private static final String TAG = BluetoothWriter.class.getName();
    private static final int WRITE_DEFAULT = 2;
    private static final int WRITE_NORESPONSE = 1;
    private static final int WRITE_SIGNED = 4;
    private final ArrayList<WriteEntry> values = new ArrayList<>();
    private BluetoothGatt gattConnection;
    private int connectionState = BluetoothGatt.STATE_DISCONNECTING;
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "Gatt connection state changed: " + newState);
            connectionState = newState;
            if (newState != BluetoothGatt.STATE_CONNECTED)
                return;
            if (gatt == null) return;
            gatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "Gatt Services discovered");
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered received: " + status);
                return;
            }
            Log.d(TAG, "Services discovered");
            if (gatt == null) return;
            gatt.requestMtu(200);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "Gatt Characteristic written " + characteristic + "," + status);
            WriteEntry entry = values.stream().filter(d -> d.characteristicUUID.compareTo(characteristic.getUuid()) == 0).findFirst().orElse(null);
            if (entry == null) {
                Log.e(TAG, "No corresponding entry write entry found");
                return;
            }
            if (entry.callback != null) {
                entry.callback.accept(status == BluetoothGatt.GATT_SUCCESS);
            }
            values.remove(entry);
            writeValues(gatt);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.d(TAG, "Gatt MTU changed " + status);
            writeValues(gatt);
        }
    };

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stop();
    }

    public void writeValue(@NotNull Context context, @NotNull BluetoothDevice device, @NotNull UUID serviceUUID, @NotNull UUID characteristicUUID, @NotNull byte[] value, Consumer<Boolean> callback) {
        values.add(new WriteEntry(context, device, value, callback, serviceUUID, characteristicUUID));
        if ((gattConnection == null) || ((connectionState != BluetoothGatt.STATE_CONNECTED) && (connectionState != BluetoothGatt.STATE_CONNECTING))) {
            gattConnection = device.connectGatt(context, false, gattCallback);
            return;
        }
        writeValues(gattConnection);
    }

    public void stop() {
        if (gattConnection != null) {
            gattConnection.close();
            gattConnection = null;
        }
        values.clear();
    }

    private void writeValues(BluetoothGatt gatt) {
        if (values.size() == 0) {
            Log.d(TAG, "No more values to write");
            return;
        }
        WriteEntry entry = values.get(0);
        BluetoothGattService service = gatt.getService(entry.serviceUUID);
        if (service == null) {
            Log.d(TAG, "Desired service not found " + entry.serviceUUID);
            entry.callback.accept(false);
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(entry.characteristicUUID);
        if (service == null) {
            Log.d(TAG, "Desired Characteristic not found " + entry.characteristicUUID);
            entry.callback.accept(false);
            return;
        }
        characteristic.setWriteType(WRITE_NORESPONSE);
        characteristic.setValue(entry.value);
        Log.d(TAG, "Writing characteristic " + gatt.writeCharacteristic(characteristic));
    }

    private class WriteEntry {
        private final BluetoothDevice device;
        private final byte[] value;
        private final Consumer<Boolean> callback;
        private final UUID serviceUUID;
        private final UUID characteristicUUID;
        Context context;

        WriteEntry(@NotNull Context context, @NotNull BluetoothDevice device, @NotNull byte[] value, @NotNull Consumer<Boolean> callback, @NotNull UUID serviceUUID, @NotNull UUID characteristicUUID) {
            this.context = context;
            this.device = device;
            this.value = value;
            this.callback = callback;
            this.serviceUUID = serviceUUID;
            this.characteristicUUID = characteristicUUID;

        }
    }
}

