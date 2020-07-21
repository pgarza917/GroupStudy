package com.example.studygroup.eventCreation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.studygroup.R;
import com.example.studygroup.adapters.FileViewAdapter;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    private Event mEvent;
    private List<FileExtended> mEventFiles;
    private FileViewAdapter mFileViewAdapter;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mTitleTextView = view.findViewById(R.id.eventTitleTextView);
        mDateTextView = view.findViewById(R.id.detailsDateTextView);
        mTimeTextView = view.findViewById(R.id.detailsTimeTextView);
        mLocationTextView = view.findViewById(R.id.detailsLocationTextView);
        mDescriptionTextView = view.findViewById(R.id.detailsDescriptionTextView);
        mDateImageButton = view.findViewById(R.id.detailsCalendarImageButton);
        mTimeImageButton = view.findViewById(R.id.detailsTimeImageButton);
        mLocationImageButton = view.findViewById(R.id.detailsLocationImageButton);
        mEventFilesRecyclerView = view.findViewById(R.id.detailsFilesRecyclerView);

        mEvent = (Event) Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));
        Log.i(TAG, "Received Bundled Event Data!");

        mTitleTextView.setText(mEvent.getTitle());
        mLocationTextView.setText(mEvent.getLocationName());
        mDescriptionTextView.setText(mEvent.getDescription());

        setDateTimeText();

        List<FileExtended> files = (List<FileExtended>) mEvent.getFiles().get(0);
        if(mEventFiles == null) {
            mEventFiles = new ArrayList<>();
        }
        if(files != null) {
            mEventFiles.addAll(files);
        }

        mFileViewAdapter = new FileViewAdapter(getContext(), mEventFiles);
        mEventFilesRecyclerView.setAdapter(mFileViewAdapter);
        mEventFilesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Log.i(TAG, "onViewCreated: Successful event details load");
    }

    public void setDateTimeText() {
        String timeStamp = mEvent.getTime().toString();
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