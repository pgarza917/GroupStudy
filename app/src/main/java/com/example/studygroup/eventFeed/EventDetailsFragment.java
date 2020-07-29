package com.example.studygroup.eventFeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
import com.example.studygroup.adapters.FileViewAdapter;
import com.example.studygroup.eventCreation.CreateEventFragment;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
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

    private TextView mTitleTextView;
    private TextView mDateTextView;
    private TextView mTimeTextView;
    private TextView mLocationTextView;
    private TextView mDescriptionTextView;
    private ImageButton mDateImageButton;
    private ImageButton mTimeImageButton;
    private ImageButton mLocationImageButton;
    private RecyclerView mEventFilesRecyclerView;
    private Menu mOptionsMenu;

    private Event mEvent;
    private List<FileExtended> mEventFiles;
    private FileViewAdapter mFileViewAdapter;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.edit_event_menu, menu);

        mOptionsMenu = menu;
        MenuItem editIcon = mOptionsMenu.findItem(R.id.action_edit_event);
        if(!eventOwnersContainsUser(mEvent, ParseUser.getCurrentUser())) {
            editIcon.setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_edit_event) {
            Fragment fragment = new CreateEventFragment();
            Bundle data = new Bundle();
            data.putParcelable("event", mEvent);
            fragment.setArguments(data);
            fragment.setTargetFragment(EventDetailsFragment.this, 123);
            ((MainActivity) getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayoutContainer, fragment)
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("");

        mTitleTextView = view.findViewById(R.id.eventTitleTextView);
        mDateTextView = view.findViewById(R.id.detailsDateTextView);
        mTimeTextView = view.findViewById(R.id.detailsTimeTextView);
        mLocationTextView = view.findViewById(R.id.detailsLocationTextView);
        mDescriptionTextView = view.findViewById(R.id.detailsDescriptionTextView);
        mDateImageButton = view.findViewById(R.id.detailsCalendarImageButton);
        mTimeImageButton = view.findViewById(R.id.detailsTimeImageButton);
        mLocationImageButton = view.findViewById(R.id.detailsLocationImageButton);
        mEventFilesRecyclerView = view.findViewById(R.id.detailsFilesRecyclerView);

        mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));
        Log.i(TAG, "Received Bundled Event Data!");

        mTitleTextView.setText(mEvent.getTitle());
        mLocationTextView.setText(mEvent.getLocationName());
        mDescriptionTextView.setText(mEvent.getDescription());

        setDateTimeText(mEvent);

        List<FileExtended> files = mEvent.getFiles();
        if(mEventFiles == null) {
            mEventFiles = new ArrayList<>();
        }
        if(files != null) {
            mEventFiles.addAll((Collection<? extends FileExtended>) files.get(0));
        }

        mFileViewAdapter = new FileViewAdapter(getContext(), mEventFiles);
        mEventFilesRecyclerView.setAdapter(mFileViewAdapter);
        mEventFilesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration itemDecor = new DividerItemDecoration(mEventFilesRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mEventFilesRecyclerView.addItemDecoration(itemDecor);

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

    private boolean eventOwnersContainsUser(Event event, ParseUser user) {
        List<ParseUser> eventOwners = event.getOwners();
        for(int i = 0; i < eventOwners.size(); i++) {
            ParseUser currentOwner = eventOwners.get(i);
            if(currentOwner.getObjectId().equals(user.getObjectId())) {
                return true;
            }
        }
        return false;
    }
}