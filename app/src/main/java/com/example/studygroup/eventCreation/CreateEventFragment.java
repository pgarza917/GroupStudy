package com.example.studygroup.eventCreation;

import android.app.Activity;
import android.content.Intent;

import java.text.SimpleDateFormat;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.adapters.FileViewAdapter;
import com.example.studygroup.adapters.UsersAdapter;
import com.example.studygroup.eventFeed.EventDetailsFragment;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateEventFragment extends Fragment {

    public static final String TAG = CreateEventFragment.class.getSimpleName();
    public static final int TIME_PICKER_REQUEST_CODE = 300;
    public static final int DATE_PICKER_REQUEST_CODE = 500;
    public static final int LOCATION_SELECT_REQUEST_CODE = 450;
    public static final int FILE_UPLOAD_REQUEST_CODE = 901;
    public static final int ADD_USERS_REQUEST_CODE = 876;

    private TextView mSelectedTimeTextView;
    private TextView mSelectedDateTextView;
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private TextView mSelectedLocationTextView;
    private TextView mAddFilesTextView;

    private String mFormattedDate;
    private String mFormattedTime;
    private ParseGeoPoint mSelectedLocationGeoPoint;
    private String mSelectedLocationName;
    private List<FileExtended> mEventFiles;
    private List<ParseUser> mEventUsers;
    private FileViewAdapter mFileAdapter;
    private UsersAdapter mUsersAdapter;

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
        menu.clear();
        inflater.inflate(R.menu.create_event_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Create");

        if(mEventFiles == null) {
            mEventFiles = new ArrayList<>();
        }
        if(mEventUsers == null) {
            mEventUsers = new ArrayList<>();
        }

        UsersAdapter.CheckBoxListener checkBoxListener = new UsersAdapter.CheckBoxListener() {
            @Override
            public void onBoxChecked(int position) {

            }

            @Override
            public void onBoxUnchecked(int position) {

            }
        };

        // Setting member variables
        mSelectedTimeTextView = view.findViewById(R.id.selectTimeTextView);
        mSelectedDateTextView = view.findViewById(R.id.selectDateTextView);
        mTitleEditText = view.findViewById(R.id.titleEditText);
        mDescriptionEditText = view.findViewById(R.id.descriptionEditText);
        mSelectedLocationTextView = view.findViewById(R.id.selectLocationTextView);
        mAddFilesTextView = view.findViewById(R.id.addFilesTextView);

        mFileAdapter = new FileViewAdapter(getContext(), mEventFiles);
        mUsersAdapter = new UsersAdapter(getContext(), mEventUsers, checkBoxListener);

        // Local UI component variables (do not require member variable scope)
        ImageButton mSelectTimeImageButton = view.findViewById(R.id.timePickerImageButton);
        ImageButton mSelectDateImageButton = view.findViewById(R.id.datePickerImageButton);
        ImageButton mSelectLocationImageButton = view.findViewById(R.id.locationPickerImageButton);
        ImageButton mAddFilesImageButton = view.findViewById(R.id.addFileImageButton);
        ImageButton mAddUsersImageButton = view.findViewById(R.id.addUsersToEventImageButton);
        RecyclerView mAttachedFilesRecyclerView = view.findViewById(R.id.attachedFilesRecyclerView);
        RecyclerView mEventUsersRecyclerView = view.findViewById(R.id.eventUsersRecyclerView);
        Button mSubmitButton = view.findViewById(R.id.submitButton);
        final Spinner privacySpinner = view.findViewById(R.id.privacySelectSpinner);

        // Setup for file recycler view
        mAttachedFilesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAttachedFilesRecyclerView.setAdapter(mFileAdapter);

        // Setup for the user Recycler View
        mEventUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mEventUsersRecyclerView.setAdapter(mUsersAdapter);

        DividerItemDecoration fileViewDivider = new DividerItemDecoration(mAttachedFilesRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mAttachedFilesRecyclerView.addItemDecoration(fileViewDivider);

        DividerItemDecoration userViewDivider = new DividerItemDecoration(mEventUsersRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mEventUsersRecyclerView.addItemDecoration(userViewDivider);

        List<String> spinnerOptions = new ArrayList<String>();
        spinnerOptions.add("Open");
        spinnerOptions.add("Closed");
        final int[] selectedPrivacy = {0};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privacySpinner.setAdapter(spinnerAdapter);
        privacySpinner.setSelection(0);
        privacySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedPrivacy[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                Bundle data = new Bundle();
                data.putParcelableArrayList("filesAttached", (ArrayList<? extends Parcelable>) mEventFiles);
                data.putParcelableArrayList("eventUsers", (ArrayList<? extends Parcelable>) mEventUsers);
                fragment.setArguments(data);
                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction().add(R.id.frameLayoutContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        };

        View.OnClickListener addUsersListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new AddUsersFragment();
                fragment.setTargetFragment(CreateEventFragment.this, ADD_USERS_REQUEST_CODE);
                Bundle data = new Bundle();
                data.putParcelableArrayList("eventUsers", (ArrayList<? extends Parcelable>) mEventUsers);
                fragment.setArguments(data);
                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction().add(R.id.frameLayoutContainer, fragment)
                        .addToBackStack(null)
                        .commit();
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

        mAddUsersImageButton.setOnClickListener(addUsersListener);

        Fragment prevFragment = getTargetFragment();
        Event eventToEdit = null;
        boolean inEdit = false;
        if(prevFragment != null) {
            String prevFragmentName = prevFragment.getClass().getSimpleName();
            if (prevFragmentName.equals(EventDetailsFragment.class.getSimpleName())) {
                mSubmitButton.setText(R.string.update_button_text);

                eventToEdit = getArguments().getParcelable("event");

                inEdit = true;
                mTitleEditText.setText(eventToEdit.getTitle());
                mDescriptionEditText.setText(eventToEdit.getDescription());
                setDateTime(eventToEdit);
                mSelectedLocationTextView.setText(eventToEdit.getLocationName());
                /*
                mEventFiles.addAll((Collection<? extends FileExtended>) eventToEdit.getFiles().get(0));
                List<ParseUser> users = eventToEdit.getUsers();
                mUsersAdapter.addAll(users);
                 */
            }
        }

        View.OnClickListener submitListener = new View.OnClickListener() {
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
                    // This for loop will go through all the files in the event files list and
                    // upload them individually to the Parse DB
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
                    // Finally, we save the entire event to Parse
                    saveEvent(ParseUser.getCurrentUser(), title, description, eventDateTime, mSelectedLocationGeoPoint,
                                mSelectedLocationName, mEventFiles, mEventUsers);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        Event finalEventToEdit = eventToEdit;
        View.OnClickListener updateListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date eventDateTime = null;
                if(mFormattedDate != null && mFormattedTime != null && !mFormattedDate.isEmpty() && !mFormattedTime.isEmpty()) {
                    String formattedDateTime = mFormattedDate + " " + mFormattedTime;
                    try {
                        eventDateTime = stringToDate(formattedDateTime, "dd.MM.yyyy HH:mm", Locale.ENGLISH);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                updateEvent(finalEventToEdit, mTitleEditText.getText().toString(), mDescriptionEditText.getText().toString(),
                            eventDateTime, mSelectedLocationGeoPoint, mSelectedLocationName, mEventFiles, mEventUsers, selectedPrivacy);
            }
        };

        if(inEdit) {
            mSubmitButton.setOnClickListener(updateListener);
        } else {
            mSubmitButton.setOnClickListener(submitListener);
        }
    }

    // This method handles the launching of the Time Picker dialog fragment so users have a nice
    // UI to select the event time
    private void launchTimePicker() {
        Log.i(TAG, "Launching Time Picker Dialog!");
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setTargetFragment(CreateEventFragment.this, TIME_PICKER_REQUEST_CODE);
        timePickerFragment.show(((MainActivity)getContext()).getSupportFragmentManager(), "timePicker");
    }

    // This method handles the launching of the Date Picker dialog fragment so users have a nice
    // UI to select and event date
    private void launchDatePicker() {
        Log.i(TAG, "Launching Date Picker Dialog!");
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.AlertDialogCustom);
        datePickerFragment.setTargetFragment(CreateEventFragment.this, DATE_PICKER_REQUEST_CODE);
        datePickerFragment.show(((MainActivity)getContext()).getSupportFragmentManager(), "datePicker");
    }

    // This method handles the launching of a Map activity where users will be able to view a map
    // and search/select a location for their event
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
            mSelectedTimeTextView.setText(time);
            mSelectedTimeTextView.setTypeface(Typeface.DEFAULT_BOLD);
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
            mSelectedDateTextView.setText(date);
            mSelectedDateTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mFormattedDate = String.format("%02d.%02d.%04d", day, month, year);
        }
        if(requestCode == LOCATION_SELECT_REQUEST_CODE) {
            // Extracting the latitude and longitude of the location the user selected in the
            // Map activity
            String locationName = data.getStringExtra("name");
            Double lat = data.getDoubleExtra("lat", 0.0);
            Double lng = data.getDoubleExtra("lng", 0.0);

            // Creating a Parse GeoPoint object to add to the event
            mSelectedLocationGeoPoint = new ParseGeoPoint(lat, lng);
            mSelectedLocationName = locationName;

            // Changing the appropriate fields with the name of the selected location to let
            // the user know they have successfully selected an event location
            mSelectedLocationTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mSelectedLocationTextView.setText(locationName);
        }
        if(requestCode == FILE_UPLOAD_REQUEST_CODE) {
            // Retrieving the list of files a user would like to upload attached to their event
            // that they selected in the file view fragment
            List<FileExtended> files = data.getParcelableArrayListExtra("uploadFiles");
            mEventFiles.addAll(files);
            mFileAdapter.notifyDataSetChanged();

            mAddFilesTextView.setText("Selected Files: ");
            Log.i(TAG, "Received selected files");
        }
        if(requestCode == ADD_USERS_REQUEST_CODE) {
            List<ParseUser> users = data.getParcelableArrayListExtra("eventUsers");
            mUsersAdapter.addUnique(users);

            Log.i(TAG, "Received selected users");
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
    private void saveEvent(ParseUser user, String title, String description, Date dateTime, ParseGeoPoint location,
                           String locationName, List<FileExtended> files, List<ParseUser> users) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setTime(dateTime);
        event.setLocation(location);
        event.setLocationName(locationName);
        event.addUnique(Event.KEY_OWNERS, user);
        event.addUnique(Event.KEY_FILES, files);
        for(int i = 0; i < users.size(); i++) {
            event.addUnique("users", users.get(i));
        }
        for(int i = 0; i < files.size(); i++) {
            event.addUnique("files", files.get(i));
        }

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
                mUsersAdapter.clear();
            }
        });
    }

    private void setDateTime(Event event) {
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

        mSelectedDateTextView.setText(date);
        mSelectedTimeTextView.setText(time);
    }

    private void updateEvent(Event eventToEdit, String newTitle, String newDescription, Date dateTime, ParseGeoPoint location,
                             String locationName, List<FileExtended> files, List<ParseUser> users, int[] privacySetting) {
        if(newTitle != null && !newTitle.isEmpty()) {
            eventToEdit.setTitle(newTitle);
        }
        if(newDescription != null && !newDescription.isEmpty()) {
            eventToEdit.setDescription(newDescription);
        }
        if(dateTime != null) {
            eventToEdit.setTime(dateTime);
        }
        if(location != null) {
            eventToEdit.setLocation(location);
        }
        if(location != null && !locationName.isEmpty()) {
            eventToEdit.setLocationName(locationName);
        }
        if(files != null) {
            for(int i = 0; i < files.size(); i++) {
                eventToEdit.put("files", files.get(i));
            }
        }
        if(users != null) {
            for(int i = 0; i < users.size(); i++) {
                eventToEdit.put("users", users.get(i));
            }
        }
        if(privacySetting != null) {
            if(privacySetting[0] == 0) {
                eventToEdit.put("privacy", "open");
            } else {
                eventToEdit.put("privacy", "closed");
            }
        }

        eventToEdit.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                Toast.makeText(getContext(), "Saved Edits Successfully!", Toast.LENGTH_SHORT).show();

                Fragment fragment = new EventDetailsFragment();
                Bundle data = new Bundle();
                data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(eventToEdit));
                fragment.setArguments(data);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutContainer, fragment)
                        .commit();
            }
        });
    }
}