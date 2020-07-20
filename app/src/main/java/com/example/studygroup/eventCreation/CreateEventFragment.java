package com.example.studygroup.eventCreation;

import android.app.Activity;
import android.content.Intent;

import java.text.SimpleDateFormat;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateEventFragment extends Fragment {

    public static final String TAG = CreateEventFragment.class.getSimpleName();
    public static final int TIME_PICKER_REQUEST_CODE = 300;
    public static final int DATE_PICKER_REQUEST_CODE = 500;
    public static final int LOCATION_SELECT_REQUEST_CODE = 450;
    public static final int FILE_UPLOAD_REQUEST_CODE = 901;

    private ImageButton mSelectTimeImageButton;
    private ImageButton mSelectDateImageButton;
    private ImageButton mSelectLocationImageButton;
    private ImageButton mAddFilesImageButton;
    private TextView mSelectedTimeTextView;
    private TextView mSelectedDateTextView;
    private Button mSubmitButton;
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private TextView mSelectedLocationTextView;
    private TextView mAddFilesTextView;

    private String mFormattedDate;
    private String mFormattedTime;
    private ParseGeoPoint mSelectedLocationGeoPoint;
    private String mSelectedLocationName;
    private List<FileExtended> mEventFiles;

    public CreateEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.create_event_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mEventFiles == null) {
            mEventFiles = new ArrayList<>();
        }

        mSelectTimeImageButton = view.findViewById(R.id.timePickerImageButton);
        mSelectDateImageButton = view.findViewById(R.id.datePickerImageButton);
        mSelectedTimeTextView = view.findViewById(R.id.selectTimeTextView);
        mSelectedDateTextView = view.findViewById(R.id.selectDateTextView);
        mSelectLocationImageButton = view.findViewById(R.id.locationPickerImageButton);
        mSubmitButton = view.findViewById(R.id.submitButton);
        mTitleEditText = view.findViewById(R.id.titleEditText);
        mDescriptionEditText = view.findViewById(R.id.descriptionEditText);
        mSelectedLocationTextView = view.findViewById(R.id.selectLocationTextView);
        mAddFilesImageButton = view.findViewById(R.id.addFileImageButton);
        mAddFilesTextView = view.findViewById(R.id.addFilesTextView);

        View.OnClickListener dateSelectListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchDatePicker();
            }
        };

        View.OnClickListener timeSelectListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTimePicker();
            }
        };

        View.OnClickListener locationSelectListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMapActivity();
            }
        };

        View.OnClickListener addFilesListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new FileViewFragment();
                fragment.setTargetFragment(CreateEventFragment.this, FILE_UPLOAD_REQUEST_CODE);
                ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutContainer, fragment).addToBackStack("frame1").commit();
            }
        };

        // Launching a Date picker widget when user taps on calendar icon button or taps on the
        // text view right next to the calendar icon button
        mSelectDateImageButton.setOnClickListener(dateSelectListener);
        mSelectedDateTextView.setOnClickListener(dateSelectListener);

        // Launching a Time picker widget when user taps on clock icon button or taps on the
        // text view right next to the clock icon button
        mSelectTimeImageButton.setOnClickListener(timeSelectListener);
        mSelectedTimeTextView.setOnClickListener(timeSelectListener);

        // Launching the Map activity when the user taps on the location icon button or taps on the
        // text view right next to the location icon
        mSelectLocationImageButton.setOnClickListener(locationSelectListener);
        mSelectedLocationTextView.setOnClickListener(locationSelectListener);


        mAddFilesImageButton.setOnClickListener(addFilesListener);
        mAddFilesTextView.setOnClickListener(addFilesListener);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = mTitleEditText.getText().toString();
                String description = mDescriptionEditText.getText().toString();

                // Error checking for empty required fields
                if(title.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a title for the event!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(description.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a description for the event!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(mFormattedTime == null) {
                    Toast.makeText(getContext(), "Please select a time for the event!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(mFormattedDate == null) {
                    Toast.makeText(getContext(), "Please select a date for the event!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(mSelectedLocationGeoPoint == null && mSelectedLocationName == null) {
                    Toast.makeText(getContext(), "Please select a location for the event!", Toast.LENGTH_LONG).show();
                    return;
                }

                String formattedDateTime = mFormattedDate + " " + mFormattedTime;
                try {
                    Date eventDateTime = stringToDate(formattedDateTime, "dd.MM.yyyy HH:mm", Locale.ENGLISH);
                    for(final FileExtended file : mEventFiles) {
                        file.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(com.parse.ParseException e) {
                                if (e != null) {
                                    Toast.makeText(getContext(), "Error uploading file", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Log.i(TAG, "Uploaded file: " + file.getFileName());
                            }
                        });
                    }
                    saveEvent(ParseUser.getCurrentUser(), title, description, eventDateTime, mSelectedLocationGeoPoint, mSelectedLocationName, mEventFiles);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void launchTimePicker() {
        Log.i(TAG, "Launching Time Picker Dialog!");
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setTargetFragment(CreateEventFragment.this, TIME_PICKER_REQUEST_CODE);
        timePickerFragment.show(((MainActivity)getContext()).getSupportFragmentManager(), "timePicker");
    }

    private void launchDatePicker() {
        Log.i(TAG, "Launching Date Picker Dialog!");
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setTargetFragment(CreateEventFragment.this, DATE_PICKER_REQUEST_CODE);
        datePickerFragment.show(((MainActivity)getContext()).getSupportFragmentManager(), "datePicker");
    }

    private void launchMapActivity() {
        if(((MainActivity) getContext()).isGoogleServicesOk()) {
            Intent intent = new Intent(getActivity(), MapActivity.class);
            startActivityForResult(intent, LOCATION_SELECT_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != Activity.RESULT_OK){
            Log.e(TAG, "Issue when returning from dialog");
            return;
        }
        int hour, minute, day, month, year;

        if(requestCode == TIME_PICKER_REQUEST_CODE) {
            hour = data.getIntExtra("hour", 0);
            minute = data.getIntExtra("minute", 0);
            String min = String.format("%02d", minute);
            String time = ((hour == 12) ? 12 : hour % 12) + ":" + min + " " + ((hour >= 12) ? "pm" : "am");
            mSelectedTimeTextView.setText(time);
            mSelectedTimeTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mFormattedTime = String.format("%02d:%s", hour, min);

        }
        if (requestCode == DATE_PICKER_REQUEST_CODE) {
            day = data.getIntExtra("day", 0);
            month = data.getIntExtra("month", 0);
            year = data.getIntExtra("year", 0);

            String date = month + "/" + day + "/" + year;
            mSelectedDateTextView.setText(date);
            mSelectedDateTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mFormattedDate = String.format("%02d.%02d.%04d", day, month, year);
        }
        if(requestCode == LOCATION_SELECT_REQUEST_CODE) {
            String locationName = data.getStringExtra("name");
            Double lat = data.getDoubleExtra("lat", 0.0);
            Double lng = data.getDoubleExtra("lng", 0.0);
            mSelectedLocationGeoPoint = new ParseGeoPoint(lat, lng);
            mSelectedLocationName = locationName;

            mSelectedLocationTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mSelectedLocationTextView.setText(locationName);
        }
        if(requestCode == FILE_UPLOAD_REQUEST_CODE) {
            List<FileExtended> files = data.getParcelableArrayListExtra("uploadFiles");
            mEventFiles.addAll(files);
            Log.i(TAG, "Received selected files");
        }
    }

    // Method for helping parse a Date format and creating a Date object
    public static Date stringToDate(String string, final String format, final Locale locale) throws ParseException {
        ThreadLocal formatter = new ThreadLocal() {
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat(format, locale);
            }
        };
        return ((SimpleDateFormat)formatter.get()).parse(string);
    }

    // Method for submitting the user-created event to the Parse database with the correct
    // details
    private void saveEvent(ParseUser user, String title, String description, Date dateTime, ParseGeoPoint location, String locationName, List<FileExtended> files) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setTime(dateTime);
        event.setLocation(location);
        event.setLocationName(locationName);
        event.addUnique(Event.KEY_OWNERS, user);
        event.addUnique(Event.KEY_FILES, files);

        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error saving event", e);
                    return;
                }
                Toast.makeText(getContext(), "Event Saved Successfully!", Toast.LENGTH_LONG).show();
                // Clear out fields for visual confirmation of save
                mTitleEditText.setText("");
                mDescriptionEditText.setText("");
                mSelectedTimeTextView.setText("");
                mSelectedDateTextView.setText("");
            }
        });
    }
}