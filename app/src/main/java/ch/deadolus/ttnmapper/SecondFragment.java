package ch.deadolus.ttnmapper;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class SecondFragment extends Fragment {
    private static final String TAG = SecondFragment.class.getName();
    private static TextView fragmentLabel = null;

    public static void updateLocation(Context context, Location location) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth not enabled!");
            Toast.makeText(context, "Please enable bluetooth to search for Bluetooth devices", Toast.LENGTH_SHORT).show();
            MainActivity.stopService();
            return;
        }
        if (fragmentLabel == null) {
            Log.d(TAG, "Received update, but fragmentLabel is null");
            return;
        }
        fragmentLabel.setText(String.format("Latest location: \r\nlat:%s,\r\nlon:%s,\r\nalt:%s,\r\nacc:%s,\r\n", location.getLatitude(), +location.getLongitude(), location.getAltitude(), location.getAccuracy()));


    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);

    }

    @Override
    public void onResume() {
        super.onResume();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth not enabled!");
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_FirstFragment);
            MainActivity.stopService();
            return;
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.stopService();
            }
        });
        fragmentLabel = view.findViewById(R.id.textview_second);
    }
}