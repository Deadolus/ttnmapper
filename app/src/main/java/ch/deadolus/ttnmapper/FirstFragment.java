package ch.deadolus.ttnmapper;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FirstFragment extends Fragment {
    private static final String TAG = FirstFragment.class.getName();
    private static FloatingActionButton fab = null;

    public static void setFab(FloatingActionButton fab) {
        FirstFragment.fab = fab;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fab != null) {
            fab.setVisibility(View.INVISIBLE);
        }
        Log.d(TAG, "First fragment paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "First fragment resumed");
        if (fab != null) {
            fab.setVisibility(View.VISIBLE);
        }
        if (MapperDevice.getMapper() != null) {
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
            return;
        }
        Log.d(TAG, "Stopping services from resume");
        MainActivity.stopService();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_first).setOnClickListener(view1 -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));


    }
}