package com.example.studygroup.eventCreation.dateTime;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
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
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.CreateEventFragment;
import com.example.studygroup.eventCreation.location.MapsFragment;
import com.example.studygroup.models.Event;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DateTimeAndPrivacyFragment is a subclass of the {@link Fragment} class. It handles
 * the functionality that allows users to select a date, a time, and a privacy (open
 * or closed) for the event they are in the process of creating. It is the second screen
 * in the event creation UI flow
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

    private String mFormattedTime;
    private String mFormattedDate;
    private boolean mIsOpen = false;
    private Event mEvent;

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

        // Clear the menu to make sure no extra menu items we don't want appear
        menu.clear();
        // Inflate the correct menu, i.e. one with a check menu icon
        inflater.inflate(R.menu.create_event_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Get the current fragment being displayed on the device (frameLayoutContainer is the view
        // always used for displaying fragments in MainActivity)
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutContainer);
        // Get the class name of this fragment
        String fragmentName = currentFragment.getClass().getSimpleName();

        // Make sure that the current displayed fragment is an instance of this class to make sure
        // that the user's taps on the menu items execute the code for these action in the
        // correct fragment
        if(fragmentName.equals(DateTimeAndPrivacyFragment.class.getSimpleName())) {
            if (item.getItemId() == R.id.action_check) {
                // Save the event changes successfully on the Event object before proceeding
                if (!saveEventChanges()) {
                    return false;
                }
                // Start the Maps fragment to allow users to select a location for their event and
                // pass the event that now also has a date, time, and privacy
                Fragment fragment = new MapsFragment();
                Bundle data = new Bundle();
                data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));
                fragment.setArguments(data);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .add(R.id.frameLayoutContainer, fragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieving and opening the event being created
        mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));

        // Getting references to the various view components
        mDateImageButton = view.findViewById(R.id.dateImageButton);
        mTimeImageButton = view.findViewById(R.id.timeImageButton);
        mSelectDateTextView = view.findViewById(R.id.selectDateTextView);
        mSelectTimeTextView = view.findViewById(R.id.selectTimeTextView);
        mDateTextView = view.findViewById(R.id.dateTextView);
        mTimeTextView = view.findViewById(R.id.timeTextView);
        mPrivacySwitch = view.findViewById(R.id.privacySwitch);

        // Listener to handle launching a date picker dialog widget for users to select an event
        // date when they tap the calendar icon
        View.OnClickListener dateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchDatePicker();
            }
        };

        // Listener to handle launching a time picker dialog widget for users to select an event
        // time when they tap the clock icon
        View.OnClickListener timeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTimePicker();
            }
        };

        // Setting listeners mentioned above on relevant image buttons and text views to increase
        // the tap area for launching date or time pickers
        mDateImageButton.setOnClickListener(dateClickListener);
        mTimeImageButton.setOnClickListener(timeClickListener);
        mSelectDateTextView.setOnClickListener(dateClickListener);
        mSelectTimeTextView.setOnClickListener(timeClickListener);

        // Listener to handle capturing the selected privacy when users change the state of the
        // Switch button (boolean --> false = closed , true = open)
        mPrivacySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsOpen = b;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK){
            Log.e(TAG, "Issue when returning from dialog");
            return;
        }
        int hour, minute, day, month, year;

        if(requestCode == TIME_PICKER_REQUEST_CODE) {
            // Retrieving the hour and minute of the day the user has chosen as the
            // time of their event
            hour = data.getIntExtra("hour", 0);
            minute = data.getIntExtra("minute", 0);
            // Formatting the retrieved hour and minute integers into a user-friendly
            // String display
            String min = String.format("%02d", minute);
            String time = ((hour == 12) ? 12 : hour % 12) + ":" + min + " " + ((hour >= 12) ? "pm" : "am");
            // Setting the appropriate text views with the formatted event-time String
            // to let users know they have successfully selected a time
            mTimeTextView.setText(time);
            mTimeTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mFormattedTime = String.format("%02d:%s", hour, min);

        }
        if (requestCode == DATE_PICKER_REQUEST_CODE) {
            // Retrieving the day, month, and year that the user has chosen
            // as the date of their event
            day = data.getIntExtra("day", 0);
            month = data.getIntExtra("month", 0);
            year = data.getIntExtra("year", 0);

            // Formatting the retrieved date integers into a user-friendly String format
            // i.e. MM/DD/YYYY
            String date = month + "/" + day + "/" + year;
            // Setting the appropriate text vies with the formatted event-date string
            // to let users know they have successfully selected a date
            mDateTextView.setText(date);
            mDateTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mFormattedDate = String.format("%02d.%02d.%04d", day, month, year);
        }
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

    // This method handles editing relevant properties (date, time, privacy) of the Event object
    // that is in the process of being created
    private boolean saveEventChanges() {
        Date eventDateTime = null;
        // Making sure the user has selected a date and time
        if(mFormattedDate != null && mFormattedTime != null && !mFormattedDate.isEmpty() && !mFormattedTime.isEmpty()) {
            // Creating a timestamp in a "dd.MM.yyyy HH:mm" so that it can be parsed to create a
            // precise Date object that can be uploaded to Parse
            String formattedDateTime = mFormattedDate + " " + mFormattedTime;
            try {
                eventDateTime = stringToDate(formattedDateTime, "dd.MM.yyyy HH:mm", Locale.ENGLISH);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "Please Select a Date and Time!", Toast.LENGTH_SHORT).show();
            return false;
        }

        mEvent.setTime(eventDateTime);

        if(mIsOpen) {
            mEvent.setPrivacy(0);
        } else {
            mEvent.setPrivacy(1);
        }

        return true;
    }

    // Method for helping parse a formatted timestamp string and creating a precise Date
    // object out of it
    public static Date stringToDate(String string, final String format, final Locale locale) throws ParseException {
        ThreadLocal formatter = new ThreadLocal() {
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat(format, locale);
            }
        };
        return ((SimpleDateFormat)formatter.get()).parse(string);
    }
}