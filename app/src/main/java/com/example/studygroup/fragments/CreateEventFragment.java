package com.example.studygroup.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != Activity.RESULT_OK){
            Log.e(TAG, "Issue when returing from dialog");
            return;
        }
        int hour, minute, day, month, year;
        if(requestCode == TIME_PICKER_REQUEST_CODE) {
            hour = data.getIntExtra("hour", 0);
            minute = data.getIntExtra("minute", 0);
            String min = String.format("%02d", minute);
            String time = (hour % 12) + ":" + min + " " + ((hour > 12) ? "pm" : "am");
            mSelectedTimeTextView.setText(time);

        } else if (requestCode == DATE_PICKER_REQUEST_CODE) {
            day = data.getIntExtra("day", 0);
            month = data.getIntExtra("month", 0);
            year = data.getIntExtra("year", 0);

            String date = month + "/" + day + "/" + year;
            mSelectedDateTextView.setText(date);
        }
    }
}