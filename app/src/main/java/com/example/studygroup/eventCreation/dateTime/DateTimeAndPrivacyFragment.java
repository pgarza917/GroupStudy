package com.example.studygroup.eventCreation.dateTime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.CreateEventFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class DateTimeAndPrivacyFragment extends Fragment {

    public static final String TAG = DateTimeAndPrivacyFragment.class.getSimpleName();
    public static final int DATE_PICKER_REQUEST_CODE = 4521;
    public static final int TIME_PICKER_REQUEST_CODE = 5201;

    private ImageButton mDateImageButton;
    private ImageButton mTimeImageButton;
    private TextView mSelectDateTextView;
    private TextView mSelectTimeTextView;
    private TextView mDateTextView;
    private TextView mTimeTextView;
    private Switch mPrivacySwitch;

    public DateTimeAndPrivacyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_date_time_and_privacy, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.create_event_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {



        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDateImageButton = view.findViewById(R.id.dateImageButton);
        mTimeImageButton = view.findViewById(R.id.timeImageButton);
        mSelectDateTextView = view.findViewById(R.id.selectDateTextView);
        mSelectTimeTextView = view.findViewById(R.id.selectTimeTextView);
        mDateTextView = view.findViewById(R.id.dateTextView);
        mTimeTextView = view.findViewById(R.id.timeTextView);
        mPrivacySwitch = view.findViewById(R.id.privacySwitch);

        View.OnClickListener dateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };

        View.OnClickListener timeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
    }

    // This method handles the launching of the Date Picker dialog fragment so users have a nice
    // UI to select and event date
    private void launchDatePicker() {
        Log.i(TAG, "Launching Date Picker Dialog!");
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.AlertDialogCustom);
        datePickerFragment.setTargetFragment(DateTimeAndPrivacyFragment.this, DATE_PICKER_REQUEST_CODE);
        datePickerFragment.show(((MainActivity)getContext()).getSupportFragmentManager(), "datePicker");
    }

    // This method handles the launching of the Time Picker dialog fragment so users have a nice
    // UI to select the event time
    private void launchTimePicker() {
        Log.i(TAG, "Launching Time Picker Dialog!");
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setTargetFragment(DateTimeAndPrivacyFragment.this, TIME_PICKER_REQUEST_CODE);
        timePickerFragment.show(((MainActivity)getContext()).getSupportFragmentManager(), "timePicker");
    }
}