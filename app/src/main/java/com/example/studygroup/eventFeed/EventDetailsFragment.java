package com.example.studygroup.eventFeed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.TextView;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.files.FileViewAdapter;
import com.example.studygroup.eventCreation.CreateEventFragment;
import com.example.studygroup.eventCreation.files.FileViewFragment;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
import com.example.studygroup.profile.ProfileFragment;
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
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new FeedFragment();
                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.frameLayoutContainer, fragment)
                        .commit();
            }
        });

        mDateTextView = view.findViewById(R.id.detailsDateTextView);
        mTimeTextView = view.findViewById(R.id.detailsTimeTextView);
        mLocationTextView = view.findViewById(R.id.detailsLocationTextView);
        mDescriptionTextView = view.findViewById(R.id.detailsDescriptionTextView);
        mDateImageButton = view.findViewById(R.id.detailsCalendarImageButton);
        mTimeImageButton = view.findViewById(R.id.detailsTimeImageButton);
        mLocationImageButton = view.findViewById(R.id.detailsLocationImageButton);

        mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));
        Log.i(TAG, "Received Bundled Event Data!");

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
}