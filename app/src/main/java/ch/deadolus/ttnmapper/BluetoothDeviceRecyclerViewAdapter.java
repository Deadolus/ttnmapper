package ch.deadolus.ttnmapper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link BluetoothDeviceDetails}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BluetoothDeviceRecyclerViewAdapter extends RecyclerView.Adapter<BluetoothDeviceRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = BluetoothDeviceRecyclerViewAdapter.class.getName();

    private static final List<BluetoothDeviceDetails> mValues = new ArrayList<>();
    private Fragment attachedFragment = null;
    private BluetoothLeScanner scanner = null;


    public BluetoothDeviceRecyclerViewAdapter(Fragment fragment) {
        startScan();
        attachedFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).mac);
        holder.mContentView.setText(mValues.get(position).name);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public BluetoothDeviceDetails mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
            itemView.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        @Override
        public void onClick(View v) {
        Log.d(TAG, "Device clicked: "+mItem.name);
        MainActivity.setMapper(mItem);
            NavHostFragment.findNavController(attachedFragment)
                    .navigate(R.id.action_itemFragment_to_FirstFragment);
            if(scanner != null) {
                scanner.stopScan(mLeScanCallback);
            }
        }

    }
    private ScanCallback mLeScanCallback = new LoraLEScanner(this, mValues);

    private void startScan() {
        Log.d(TAG, "Starting LE scan");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth not enabled!");
            //Toast.makeText(TAG, "Please enable bluetooth to search for Bluetooth devices", Toast.LENGTH_SHORT).show();
            return;
        }
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(LoraLEScanner.SERVICE_UUID)).build();
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        scanner.startScan(Collections.singletonList(filter), settings, mLeScanCallback);

    }


}