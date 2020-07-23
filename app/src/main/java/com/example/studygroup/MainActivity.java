package com.example.studygroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.accounts.Account;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.studygroup.eventCreation.CreateEventFragment;
import com.example.studygroup.eventFeed.FeedFragment;
import com.example.studygroup.profile.ProfileFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Account currentGoogleAccount = GoogleSignIn.getLastSignedInAccount(this).getAccount();

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