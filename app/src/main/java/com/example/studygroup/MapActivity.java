package com.example.studygroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.dynamic.SupportFragmentWrapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = MapActivity.class.getSimpleName();
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1254;
    public static final float DEFAULT_ZOOM = 15f;

    private EditText mLocationSearchEditText;

    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mLocationSearchEditText = findViewById(R.id.locationSearchEditText);
    }

    // Attempts to find the location inputted in the search location edit text
    private void geoLocate() {
        Log.d(TAG, "geoLocate: Attempting to find location...");

        String searchString = mLocationSearchEditText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(searchString, 1);
        } catch(IOException e) {
            Log.e(TAG, "geoLocate: IO Exception: ", e);
        }

        if(addressList.size() > 0) {

        }
    }

    private void initMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    // Method to help find the current location of the device
    private void findDeviceLocation() {
        Log.i(TAG, "Getting device's current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Found current location!");
                            // Store the retrieved current location from task result
                            Location currentLocation = (Location) task.getResult();
                            // Move camera to the stored current location
                            moveMapCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "Unable to get current location!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security Exception: ", e);
        }
    }

    // Method that facilitates the moving of the Google Map camera to a given location and zoom level
    private void moveMapCamera(LatLng location, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
    }

    // Checks to see if the location permissions have been granted by the current client. If not,
    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COURSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            initMapFragment();
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Method to handle checking if the permissions request was satisfied
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    // Initialize our map
                    initMapFragment();
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            findDeviceLocation();

            // Permission check once again to make sure user hasn't reverted previous permissions
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // Display a dot on map of device's current location
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            mLocationSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                            || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                        // Now execute search of inputted location
                        geoLocate();
                    }
                    return true;
                }
            });

            getLocationPermission();
        }
    }
}