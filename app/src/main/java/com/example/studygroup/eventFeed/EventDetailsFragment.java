package com.example.studygroup.eventFeed;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.Fade;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.files.FileViewAdapter;
import com.example.studygroup.eventCreation.CreateEventFragment;
import com.example.studygroup.eventCreation.files.FileViewFragment;
import com.example.studygroup.eventCreation.location.MapsFragment;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
import com.example.studygroup.profile.ProfileFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventDetailsFragment extends Fragment {

    public static final String TAG = EventDetailsFragment.class.getSimpleName();

    private TextView mDateTextView;
    private TextView mTimeTextView;
    private TextView mLocationTextView;
    private TextView mDescriptionTextView;
    private ImageButton mDateImageButton;
    private ImageButton mTimeImageButton;
    private ImageButton mLocationImageButton;
    private Menu mOptionsMenu;

    private Event mEvent;
    private List<FileExtended> mEventFiles;
    private FileViewAdapter mFileViewAdapter;
    private GoogleMap mMap;
    private View mMapView;

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1254;
    public static final float DEFAULT_ZOOM = 17f;

    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng mEventLatLngLocation;

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

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        setEnterTransition(new Fade());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        mMapView = mapFragment.getView();

        mDateTextView = view.findViewById(R.id.detailsDateTextView);
        mTimeTextView = view.findViewById(R.id.detailsTimeTextView);
        mLocationTextView = view.findViewById(R.id.detailsLocationTextView);
        mDescriptionTextView = view.findViewById(R.id.detailsDescriptionTextView);
        mDateImageButton = view.findViewById(R.id.detailsCalendarImageButton);
        mTimeImageButton = view.findViewById(R.id.detailsTimeImageButton);
        mLocationImageButton = view.findViewById(R.id.detailsLocationImageButton);

        mLocationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableMyLocation();
            }
        });

        mLocationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableMyLocation();
            }
        });

        mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));
        Log.i(TAG, "Received Bundled Event Data!");

        ParseGeoPoint eventLocation = mEvent.getLocation();
        mEventLatLngLocation = new LatLng(eventLocation.getLatitude(), eventLocation.getLongitude());

        mLocationTextView.setText(mEvent.getLocationName());
        mDescriptionTextView.setText(mEvent.getDescription());

        startPostponedEnterTransition();

        setDateTimeText(mEvent);

        List<FileExtended> files = mEvent.getFiles();
        if(mEventFiles == null) {
            mEventFiles = new ArrayList<>();
        }
        if(files != null) {
            mEventFiles.addAll((Collection<? extends FileExtended>) files);
        }

        mFileViewAdapter = new FileViewAdapter(getContext(), mEventFiles);

        Log.i(TAG, "onViewCreated: Successful event details load");
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

    public void setDateTimeText(Event event) {
        String timeStamp = event.getTime().toString();
        StringTokenizer tokenizer = new StringTokenizer(timeStamp);

        String weekday = tokenizer.nextToken();
        String month = tokenizer.nextToken();
        String day = tokenizer.nextToken();

        String timeInDay = tokenizer.nextToken();
        String timezone = tokenizer.nextToken();
        String year = tokenizer.nextToken();

        String date = day + " " + month + " " + year;
        int hour = Integer.parseInt(timeInDay.substring(0, 2));
        String time = ((hour == 12) ? 12 : hour % 12) + ":" + timeInDay.substring(3, 5) + " " + ((hour >= 12) ? "PM" : "AM");

        mDateTextView.setText(date);
        mTimeTextView.setText(time);
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
                            BitmapDescriptor bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                            Location currentLocation = (Location) task.getResult();
                            LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            MarkerOptions currentLocationOptions = new MarkerOptions()
                                    .position(currentLocationLatLng)
                                    .title("My Location");
                            MarkerOptions eventLocationOptions = new MarkerOptions()
                                    .position(mEventLatLngLocation)
                                    .icon(bitmapMarker)
                                    .title(mEvent.getLocationName());
                            mMap.addMarker(currentLocationOptions);
                            mMap.addMarker(eventLocationOptions);

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(currentLocationLatLng);
                            builder.include(mEventLatLngLocation);
                            LatLngBounds bounds = builder.build();
                            // Move camera to the stored current location
                            moveMapCamera(bounds, DEFAULT_ZOOM, "My Location");
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
    private void moveMapCamera(LatLngBounds bounds, float zoom, String locationTitle) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 125);
        mMap.moveCamera(cameraUpdate);
    }
}