package com.example.studygroup.eventCreation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.studygroup.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = MapActivity.class.getSimpleName();
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1254;
    public static final float DEFAULT_ZOOM = 17f;

    private ImageButton mGpsImageButton;
    private AutocompleteSupportFragment mAutoCompleteSupportFragment;
    private Button mConfirmSelectionButton;

    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient mPlacesClient;

    private LatLng mSelectedLocationLatLng;
    private String mSelectedLocationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mGpsImageButton = findViewById(R.id.gpsIconImageButton);
        mConfirmSelectionButton = findViewById(R.id.confirmSelectionButton);

        if(!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.google_api_key));
        }
        mPlacesClient = Places.createClient(this);

        mAutoCompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocompleteFragment);
        mAutoCompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutoCompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mSelectedLocationLatLng = place.getLatLng();
                mSelectedLocationName = place.getName();

                Log.i(TAG, "onPlaceSelected: " + mSelectedLocationLatLng.latitude + " " + mSelectedLocationLatLng.longitude);
                moveMapCamera(mSelectedLocationLatLng, DEFAULT_ZOOM, ((place.getName() == null) ? "" : place.getName()));

                mConfirmSelectionButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "onError: Could not find place " + status.getStatusMessage());
            }
        });

        mConfirmSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelectedLocationLatLng == null || mSelectedLocationName == null) {
                    Toast.makeText(getApplicationContext(), "You must select a location", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("name", mSelectedLocationName);
                intent.putExtra("lat", mSelectedLocationLatLng.latitude);
                intent.putExtra("lng", mSelectedLocationLatLng.longitude);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        getLocationPermission();
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
                            Log.i(TAG, "onComplete: Found current location!");
                            // Store the retrieved current location from task result
                            Location currentLocation = (Location) task.getResult();
                            // Move camera to the stored current location
                            moveMapCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
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
    private void moveMapCamera(LatLng location, float zoom, String locationTitle) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));

        if(!locationTitle.equals("My Location")) {
            // Creates and adds a marker on the map of the passed location
            MarkerOptions options = new MarkerOptions().position(location).title(locationTitle);
            mMap.addMarker(options);
        }
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


            mGpsImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "GPS Icon tapped!");
                    findDeviceLocation();
                }
            });
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

}