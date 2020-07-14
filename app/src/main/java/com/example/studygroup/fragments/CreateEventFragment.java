package com.example.studygroup.fragments;

import android.app.Activity;
import android.content.Intent;

import java.security.AlgorithmParameterGenerator;
import java.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
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
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateEventFragment extends Fragment {

    public static final String TAG = CreateEventFragment.class.getSimpleName();
    public static final int TIME_PICKER_REQUEST_CODE = 300;
    public static final int DATE_PICKER_REQUEST_CODE = 500;

    private ImageButton mSelectTimeImageButton;
    private ImageButton mSelectDateImageButton;
    private TextView mSelectedTimeTextView;
    private TextView mSelectedDateTextView;
    private Button mSubmitButton;
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;

    private String mFormattedDate;
    private String mFormattedTime;

    public CreateEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSelectTimeImageButton = view.findViewById(R.id.timePickerImageButton);
        mSelectDateImageButton = view.findViewById(R.id.datePickerImageButton);
        mSelectedTimeTextView = view.findViewById(R.id.selectTimeTextView);
        mSelectedDateTextView = view.findViewById(R.id.selectDateTextView);
        mSubmitButton = view.findViewById(R.id.submitButton);
        mTitleEditText = view.findViewById(R.id.titleEditText);
        mDescriptionEditText = view.findViewById(R.id.descriptionEditText);

        mSelectDateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.setTargetFragment(CreateEventFragment.this, DATE_PICKER_REQUEST_CODE);
                datePickerFragment.show(((MainActivity)getContext()).getSupportFragmentManager(), "datePicker");
            }
        });

        mSelectTimeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.setTargetFragment(CreateEventFragment.this, TIME_PICKER_REQUEST_CODE);
                timePickerFragment.show(((MainActivity)getContext()).getSupportFragmentManager(), "timePicker");
            }
        });

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

                String formattedDateTime = mFormattedDate + " " + mFormattedTime;
                try {
                    Date eventDateTime = stringToDate(formattedDateTime, "dd.MM.yyyy HH:mm", Locale.ENGLISH);
                    saveEvent(ParseUser.getCurrentUser(), title, description, eventDateTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
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
            mFormattedTime = String.format("%02d:%s", hour, min);

        }
        if (requestCode == DATE_PICKER_REQUEST_CODE) {
            day = data.getIntExtra("day", 0);
            month = data.getIntExtra("month", 0);
            year = data.getIntExtra("year", 0);

            String date = month + "/" + day + "/" + year;
            mSelectedDateTextView.setText(date);
            mFormattedDate = String.format("%02d.%02d.%04d", day, month, year);
        }
    }

    public static Date stringToDate(String string, final String format, final Locale locale) throws ParseException {
        ThreadLocal formatter = new ThreadLocal() {
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat(format, locale);
            }
        };
        return ((SimpleDateFormat)formatter.get()).parse(string);
    }

    private void saveEvent(ParseUser user, String title, String description, Date dateTime) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setTime(dateTime);
        event.addUnique(Event.KEY_OWNERS, user);

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
