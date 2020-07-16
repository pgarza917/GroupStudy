package com.example.studygroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.studygroup.fragments.CreateEventFragment;
import com.example.studygroup.fragments.DatePickerFragment;
import com.example.studygroup.fragments.FeedFragment;
import com.example.studygroup.fragments.ProfileFragment;
import com.example.studygroup.fragments.TimePickerFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int ERROR_DIALOG_REQUEST = 8001;

    final FragmentManager mFragmentManager = getSupportFragmentManager();
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_create_event:
                        fragment = new CreateEventFragment();
                        break;
                    case R.id.action_messages:
                        fragment = new ProfileFragment();
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        break;
                    default:
                        fragment = new FeedFragment();
                        break;
                }
                mFragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
                return true;
            }
        });

        // Set default selection as home action icon
        mBottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    // Helper method for determining if the current device is able to work with Google Play services
    // so that we do not run into errors when using the Google Map API
    public boolean isGoogleServicesOk() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play services are working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "A Google Play services error occurred, but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests!", Toast.LENGTH_LONG).show();
        }

        return false;
    }

}