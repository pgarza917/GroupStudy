package com.example.studygroup.eventCreation.users;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.models.Event;
import com.example.studygroup.search.UserSearchResultAdapter;
import com.example.studygroup.eventCreation.CreateEventFragment;
import com.example.studygroup.eventCreation.files.FileViewFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddUsersFragment extends Fragment {

    public static final String TAG = AddUsersFragment.class.getSimpleName();

    private List<ParseUser> mUserSearchResults;
    private List<ParseUser> mSelectedUsers;
    private UserSearchResultAdapter mUsersSearchAdapter;
    private UsersAdapter mSelectedUsersAdapter;

    public AddUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_users, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.create_event_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_check) {
            endUserSelection();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Add Users");

        Bundle data = getArguments();
        List<ParseUser> alreadyAddedUsers = data.getParcelableArrayList("eventUsers");

        mUserSearchResults = new ArrayList<>();
        mSelectedUsers = new ArrayList<>();
        if(alreadyAddedUsers != null) {
            mSelectedUsers.addAll(alreadyAddedUsers);
        }

        UserSearchResultAdapter.ResultClickListener resultClickListener = new UserSearchResultAdapter.ResultClickListener() {
            @Override
            public void onResultClicked(int position) {
                ParseUser user = mUserSearchResults.get(position);
                mSelectedUsersAdapter.addAll(Collections.singletonList(user));
            }
        };

        UsersAdapter.CheckBoxListener checkBoxListener = new UsersAdapter.CheckBoxListener() {
            @Override
            public void onBoxChecked(int position) {

            }

            @Override
            public void onBoxUnchecked(int position) {
                ParseUser user = mSelectedUsers.get(position);
                mSelectedUsersAdapter.remove(user);
            }
        };

        mUsersSearchAdapter = new UserSearchResultAdapter(getContext(), mUserSearchResults, resultClickListener);
        mSelectedUsersAdapter = new UsersAdapter(getContext(), mSelectedUsers, checkBoxListener);


        SearchView search = view.findViewById(R.id.addUsersSearchView);
        RecyclerView mUserSearchResultRecyclerView = view.findViewById(R.id.mUserSearchResultsRecyclerView);
        mUserSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUserSearchResultRecyclerView.setAdapter(mUsersSearchAdapter);

        RecyclerView mSelectedUsersRecyclerView = view.findViewById(R.id.selectedUsersRecyclerView);
        mSelectedUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSelectedUsersRecyclerView.setAdapter(mSelectedUsersAdapter);

        DividerItemDecoration searchUserItemDivider = new DividerItemDecoration(mUserSearchResultRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mUserSearchResultRecyclerView.addItemDecoration(searchUserItemDivider);

        DividerItemDecoration selectedUsersItemDivider = new DividerItemDecoration(mSelectedUsersRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mSelectedUsersRecyclerView.addItemDecoration(selectedUsersItemDivider);

        search.setIconified(false);
        search.requestFocus();

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                search.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        AddUsersFragment fragment = (AddUsersFragment) getFragmentManager().findFragmentByTag(AddUsersFragment.class.getSimpleName());
        fragment.getView().setFocusableInTouchMode(true);
        fragment.getView().requestFocus();
        fragment.getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    endUserSelection();
                    return true;
                }
                return false;
            }
        });
    }

    private void searchUsers(String queryString) {
        ParseQuery<ParseUser> nameQuery = ParseUser.getQuery();
        nameQuery.whereContains("lowerDisplayName", queryString.toLowerCase());

        ParseQuery<ParseUser> emailQuery = ParseUser.getQuery();
        emailQuery.whereContains("email", queryString.toLowerCase());

        List<ParseQuery<ParseUser>> userQueries = new ArrayList<ParseQuery<ParseUser>>();
        userQueries.add(nameQuery);
        userQueries.add(emailQuery);

        ParseQuery<ParseUser> usersFullQuery = ParseQuery.or(userQueries);
        usersFullQuery.orderByDescending(Event.KEY_CREATED_AT);

        usersFullQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error executing user search query: ", e);
                    return;
                }
                Log.i(TAG, "Success searching for users");
                mUsersSearchAdapter.clear();
                mUsersSearchAdapter.addAll(users);
            }
        });
    }

    private void endUserSelection() {
        String targetClassName = getTargetFragment().getClass().getSimpleName();

        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("eventUsers", (ArrayList<? extends Parcelable>) mSelectedUsers);
        if(targetClassName.equals(CreateEventFragment.class.getSimpleName())) {
            getTargetFragment().onActivityResult(CreateEventFragment.ADD_USERS_REQUEST_CODE, Activity.RESULT_OK, intent);
        } else {
            getTargetFragment().onActivityResult(FileViewFragment.ADD_USERS_REQUEST_CODE, Activity.RESULT_OK, intent);
        }
        FragmentManager fm = getActivity().getSupportFragmentManager();
        // This is used so that the state of the previous create-event fragment is
        // not changed when we return to it
        fm.popBackStackImmediate();
    }
}