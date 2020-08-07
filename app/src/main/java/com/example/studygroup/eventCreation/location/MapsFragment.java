package com.example.studygroup.eventCreation.location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.RelativeDateTimeFormatter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.files.FileViewFragment;
import com.example.studygroup.eventCreation.users.AddUsersFragment;
import com.example.studygroup.models.Event;
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
import com.parse.ParseGeoPoint;

import org.parceler.Parcels;

import java.util.Arrays;

import static android.app.Activity.RESULT_OK;

public class MapsFragment extends Fragment {

    private GoogleMap mMap;

    public static final String TAG = MapsFragment.class.getSimpleName();
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1254;
    public static final float DEFAULT_ZOOM = 17f;

    private AutocompleteSupportFragment mAutoCompleteSupportFragment;
    private Button mConfirmSelectionButton;
    private View mMapView;

    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient mPlacesClient;

    private LatLng mSelectedLocationLatLng;
    private String mSelectedLocationName;
    private Event mEvent;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            enableMyLocation();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));

        mConfirmSelectionButton = view.findViewById(R.id.confirmSelectionButton);
        mMapView = mapFragment.getView();

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.google_api_key));
        }
        mPlacesClient = Places.createClient(getContext());

        mAutoCompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocompleteFragment);
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
                    Toast.makeText(getContext(), "You must select a location", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseGeoPoint location = new ParseGeoPoint(mSelectedLocationLatLng.latitude, mSelectedLocationLatLng.longitude);
                mEvent.setLocation(location);
                mEvent.setLocationName(mSelectedLocationName);

                Fragment fragment = new AddUsersFragment();
                Bundle data = new Bundle();
                data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));
                fragment.setArguments(data);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .add(R.id.frameLayoutContainer, fragment, AddUsersFragment.TAG)
                        .addToBackStack(null)
                        .commit();
            }
        });
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
                    enableMyLocation();
                }
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mLocationPermissionGranted = true;
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                // Get the button view
                View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                // and next place it, on bottom right (as Google Maps app)
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        locationButton.getLayoutParams();
                // position on right bottom
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 0, 30, 30);
                findDeviceLocation();
            }
        } else {
            String[] permissions = {FINE_LOCATION, COURSE_LOCATION};
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Method to help find the current location of the device
    private void findDeviceLocation() {
        Log.i(TAG, "Getting device's current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

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
                            Toast.makeText(getContext(), "Unable to get current location!", Toast.LENGTH_LONG).show();
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

        if (!locationTitle.equals("My Location")) {
            // Creates and adds a marker on the map of the passed location
            MarkerOptions options = new MarkerOptions().position(location).title(locationTitle);
            mMap.addMarker(options);
        }
    }
}