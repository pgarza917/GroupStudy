package com.example.studygroup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studygroup.eventCreation.TitleDescAndSubjectFragment;
import com.example.studygroup.eventCreation.dateTime.DateTimeAndPrivacyFragment;
import com.example.studygroup.eventCreation.files.FileViewAdapter;
import com.example.studygroup.eventCreation.files.FileViewFragment;
import com.example.studygroup.eventCreation.location.MapsFragment;
import com.example.studygroup.eventCreation.users.AddUsersFragment;
import com.example.studygroup.eventCreation.users.UsersAdapter;
import com.example.studygroup.eventFeed.FeedFragment;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
import com.example.studygroup.models.Subject;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmEventFragment extends Fragment {
    public static final String TAG = ConfirmEventFragment.class.getSimpleName();
    public static final int RC_EDIT_LOCATION = 3051;
    public static final int RC_EDIT_DATE_PRIVACY = 2054;
    public static final int RC_EDIT_USERS = 9203;
    public static final int RC_EDIT_FILES = 8420;

    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private ImageButton mDateImageButton;
    private ImageButton mLocationImageButton;
    private ImageButton mPrivacyImageButton;
    private ImageButton mUsersImageButton;
    private ImageButton mFilesImageButton;
    private TextView mDateTextView;
    private TextView mLocationTextView;
    private TextView mPrivacyTextView;
    private TextView mSubjectTextView;

    private UsersAdapter mUsersAdapter;
    private FileViewAdapter mFilesAdapter;
    private List<ParseUser> mUsersList;
    private List<FileExtended> mFilesList;

    private Event mEvent;

    public ConfirmEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirm_event, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_check) {
            launchConfirmDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));
        mUsersList = new ArrayList<>();
        mFilesList = new ArrayList<>();

        List<ParseUser> alreadyAddedUsers = mEvent.getUsers();
        if(alreadyAddedUsers != null) {
            mUsersList.addAll(alreadyAddedUsers);
        }

        List<FileExtended> alreadyAddedFiles = mEvent.getFiles();
        if(alreadyAddedFiles != null) {
            mFilesList.addAll(alreadyAddedFiles);
        }

        mTitleEditText = view.findViewById(R.id.confirmEventTitleEditText);
        mDescriptionEditText = view.findViewById(R.id.confirmEventDescriptionEditText);
        mDateImageButton = view.findViewById(R.id.confirmEventsDateImageButton);
        mLocationImageButton = view.findViewById(R.id.confirmEventLocationImageButton);
        mPrivacyImageButton = view.findViewById(R.id.confirmEventPrivacyImageButton);
        mUsersImageButton = view.findViewById(R.id.confirmEventUsersImageButton);
        mFilesImageButton = view.findViewById(R.id.confirmEventFilesImageButton);
        mDateTextView = view.findViewById(R.id.confirmEventDateTextView);
        mLocationTextView = view.findViewById(R.id.confirmEventLocationTextView);
        mPrivacyTextView = view.findViewById(R.id.confirmEventPrivacyTextView);
        mSubjectTextView = view.findViewById(R.id.confirmEventSubjectTextView);
        RecyclerView mUsersRecyclerView = view.findViewById(R.id.confirmEventUsersRecyclerView);
        RecyclerView mFilesRecyclerView = view.findViewById(R.id.confirmEventFilesRecyclerView);

        mUsersAdapter = new UsersAdapter(getContext(), mUsersList, new UsersAdapter.CheckBoxListener() {
            @Override
            public void onBoxChecked(int position) {

            }

            @Override
            public void onBoxUnchecked(int position) {

            }
        });
        mFilesAdapter = new FileViewAdapter(getContext(), mFilesList);

        mUsersRecyclerView.setAdapter(mUsersAdapter);
        mFilesRecyclerView.setAdapter(mFilesAdapter);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mFilesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mTitleEditText.setText(mEvent.getTitle());
        mDescriptionEditText.setText(mEvent.getDescription());
        setDateTimeText(mEvent);
        mLocationTextView.setText(mEvent.getLocationName());
        mPrivacyTextView.setText(mEvent.getPrivacy());

        String subjectId = mEvent.getSubject().getObjectId();
        ParseQuery<Subject> subjectQuery = ParseQuery.getQuery(Subject.class);
        subjectQuery.whereEqualTo("objectId", subjectId);

        subjectQuery.findInBackground(new FindCallback<Subject>() {
            @Override
            public void done(List<Subject> subjects, ParseException e) {
                Subject subject = subjects.get(0);
                String subjectName = subject.getSubjectName();
                mSubjectTextView.setText(subjectName);
                HashMap<String, Integer> colorMap = MainActivity.createColorMap();
                Integer color = colorMap.get(subjectName);
                Drawable background = ContextCompat.getDrawable(getContext(), R.drawable.subject_tag);
                background.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), color), PorterDuff.Mode.SRC_IN));
                mSubjectTextView.setBackground(background);
            }
        });


        View.OnClickListener datePrivacyClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new DateTimeAndPrivacyFragment();
                Bundle data = new Bundle();
                data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));
                fragment.setArguments(data);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutContainer, fragment, DateTimeAndPrivacyFragment.TAG)
                        .addToBackStack(null)
                        .commit();
            }
        };

        View.OnClickListener locationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new MapsFragment();
                Bundle data = new Bundle();
                data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));
                fragment.setArguments(data);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutContainer, fragment, MapsFragment.TAG)
                        .addToBackStack(null)
                        .commit();
            }
        };

        View.OnClickListener usersClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new AddUsersFragment();
                Bundle data = new Bundle();
                data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));
                fragment.setArguments(data);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutContainer, fragment, AddUsersFragment.TAG)
                        .addToBackStack(null)
                        .commit();
            }
        };

        View.OnClickListener filesClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new FileViewFragment();
                Bundle data = new Bundle();
                data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));
                fragment.setArguments(data);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutContainer, fragment, FileViewFragment.TAG)
                        .addToBackStack(null)
                        .commit();
            }
        };

        View.OnClickListener subjectClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new TitleDescAndSubjectFragment();
                Bundle data = new Bundle();
                data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));
                fragment.setArguments(data);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        };

        mDateImageButton.setOnClickListener(datePrivacyClickListener);
        mDateTextView.setOnClickListener(datePrivacyClickListener);
        mLocationImageButton.setOnClickListener(locationClickListener);
        mLocationTextView.setOnClickListener(locationClickListener);
        mPrivacyImageButton.setOnClickListener(datePrivacyClickListener);
        mPrivacyTextView.setOnClickListener(datePrivacyClickListener);
        mSubjectTextView.setOnClickListener(subjectClickListener);
        mFilesImageButton.setOnClickListener(filesClickListener);
        mUsersImageButton.setOnClickListener(usersClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK) {
            Log.i(TAG, "Error returning to Confirm Event Fragment from other fragment");
            return;
        }
        if(requestCode == RC_EDIT_DATE_PRIVACY) {

        } else if(requestCode == RC_EDIT_LOCATION) {

        } else if(requestCode == RC_EDIT_USERS) {

        } else if(requestCode == RC_EDIT_FILES) {

        }
    }

    private void launchConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Create Event?");

        builder.setNegativeButton("No", null);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mEvent.addUnique("owners", ParseUser.getCurrentUser());
                mEvent.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e != null) {
                            Log.e(TAG, "Error saving new event to Parse: ", e);
                            return;
                        }
                        Toast.makeText(getContext(), "Event Created Successfully!", Toast.LENGTH_SHORT).show();
                        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
                        bottomNavigationView.setSelectedItemId(R.id.action_home);
                        Fragment fragment = new FeedFragment();
                        ((MainActivity) getContext()).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frameLayoutContainer, fragment)
                                .commit();
                    }
                });
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
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

        String dateTime = date + " at " + time;
        mDateTextView.setText(dateTime);
    }
}