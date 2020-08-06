package com.example.studygroup.profile;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studygroup.eventFeed.EventsAdapter;
import com.example.studygroup.models.Event;
import com.example.studygroup.MainActivity;
import com.example.studygroup.loginAndRegister.LoginActivity;
import com.example.studygroup.R;
import com.example.studygroup.models.Subject;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private TextView mProfileNameTextView;
    private TextView mBioTextView;
    private ImageView mProfilePictureImageView;
    private RecyclerView mUserEventsRecyclerView;
    private RecyclerView mSubjectRecyclerView;
    private ProgressBar mUserEventsProgressBar;

    private EventsAdapter mUserEventsAdapter;
    private SubjectAdapter mSubjectAdapter;
    private List<Event> mUserEventList;
    private List<Subject> mSubjectList;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Profile");

        mUserEventList = new ArrayList<>();
        mSubjectList = new ArrayList<>();
        mUserEventsAdapter = new EventsAdapter(getContext(), mUserEventList);
        mSubjectAdapter = new SubjectAdapter(getContext(), mSubjectList, null);

        mProfileNameTextView = view.findViewById(R.id.profileNameTextView);
        mBioTextView = view.findViewById(R.id.profileBioTextView);
        mProfilePictureImageView = view.findViewById(R.id.profilePictureImageView);
        mUserEventsRecyclerView = view.findViewById(R.id.userEventsRecyclerView);
        mSubjectRecyclerView = view.findViewById(R.id.subjectsRecyclerView);
        mUserEventsProgressBar = view.findViewById(R.id.userEventsProgressBar);

        mUserEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUserEventsRecyclerView.setAdapter(mUserEventsAdapter);

        mSubjectRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mSubjectRecyclerView.setAdapter(mSubjectAdapter);

        DividerItemDecoration itemDecor = new DividerItemDecoration(mUserEventsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mUserEventsRecyclerView.addItemDecoration(itemDecor);

        ParseUser currentUser = ParseUser.getCurrentUser();
        String displayName = currentUser.getString("displayName");

        mProfileNameTextView.setText(displayName);

        String bio = (String) currentUser.get("bio");
        if(bio == null || bio.isEmpty()) {
            mBioTextView.setText(getString(R.string.no_bio));
        } else {
            mBioTextView.setText(bio);
        }

        ParseFile profileImage = ParseUser.getCurrentUser().getParseFile("profileImage");
        if(profileImage != null) {
            mProfilePictureImageView.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(profileImage.getUrl()).circleCrop().into(mProfilePictureImageView);
        } else {
            mProfilePictureImageView.setVisibility(View.GONE);
        }

        ParseRelation<Subject> subjectRelation = currentUser.getRelation("subjectInterests");

        subjectRelation.getQuery().findInBackground(new FindCallback<Subject>() {
            @Override
            public void done(List<Subject> subjects, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error querying Parse for user subjects: ", e);
                    return;
                }
                mSubjectList.clear();
                mSubjectList.addAll(subjects);
                mSubjectAdapter.notifyDataSetChanged();

                mUserEventsProgressBar.setVisibility(View.VISIBLE);
                getUserEvents();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_edit_profile) {
            Fragment fragment = new EditProfileFragment();
            ((MainActivity) getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayoutContainer, fragment)
                    .commit();
        }
        if(id == R.id.action_logout) {
            logoutUser();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();

        GoogleSignInClient googleSignInClient=GoogleSignIn.getClient(getContext(), gso);
        googleSignInClient.signOut();
        ParseUser.logOut();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void getUserEvents() {
        ParseQuery<Event> ownersQuery = ParseQuery.getQuery(Event.class);
        ownersQuery.whereEqualTo("owners", ParseUser.getCurrentUser());

        ParseQuery<Event> usersQuery = ParseQuery.getQuery(Event.class);
        usersQuery.whereEqualTo("users", ParseUser.getCurrentUser());

        List<ParseQuery<Event>> queries = new ArrayList<ParseQuery<Event>>();
        queries.add(ownersQuery);
        queries.add(usersQuery);

        ParseQuery<Event> mainQuery = ParseQuery.or(queries);
        mainQuery.orderByDescending(Event.KEY_TIME);
        mainQuery.include(Event.KEY_FILES);

        mainQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error finding the user's events", e);
                    return;
                }
                Log.i(TAG, "Successfully found the user's events");
                mUserEventsAdapter.clear();
                mUserEventsAdapter.addAll(events);

                mUserEventsProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}