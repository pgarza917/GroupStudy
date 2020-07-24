package com.example.studygroup.eventCreation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.studygroup.R;
import com.example.studygroup.adapters.UsersAdapter;
import com.parse.FindCallback;
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
    private UsersAdapter mUsersSearchAdapter;
    private UsersAdapter mSelectedUsersAdapter;

    private RecyclerView mUserSearchResultRecyclerView;
    private RecyclerView mSelectedUsersRecyclerView;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle data = getArguments();
        List<ParseUser> alreadyAddedUsers = data.getParcelableArrayList("eventUsers");

        mUserSearchResults = new ArrayList<>();
        mSelectedUsers = new ArrayList<>();
        if(alreadyAddedUsers != null) {
            mSelectedUsers.addAll(alreadyAddedUsers);
        }

        UsersAdapter.CheckBoxListener searchCheckBoxListener = new UsersAdapter.CheckBoxListener() {
            @Override
            public void onBoxChecked(int position) {
                ParseUser user = mUserSearchResults.get(position);
                mUsersSearchAdapter.notifyItemChanged(position);
                mSelectedUsersAdapter.addAll(Collections.singletonList(user));
                addUserToEvent();
            }

            @Override
            public void onBoxUnchecked(int position) {
                ParseUser user = mUserSearchResults.get(position);
                mSelectedUsersAdapter.remove(user);
            }
        };

        UsersAdapter.CheckBoxListener selectedCheckBoxListener = new UsersAdapter.CheckBoxListener() {
            @Override
            public void onBoxChecked(int position) {

            }

            @Override
            public void onBoxUnchecked(int position) {
                ParseUser user = mSelectedUsers.get(position);
                mSelectedUsersAdapter.remove(user);
            }
        };

        mUsersSearchAdapter = new UsersAdapter(getContext(), mUserSearchResults, searchCheckBoxListener);
        mSelectedUsersAdapter = new UsersAdapter(getContext(), mSelectedUsers, selectedCheckBoxListener);


        SearchView search = view.findViewById(R.id.addUsersSearchView);
        mUserSearchResultRecyclerView = view.findViewById(R.id.mUserSearchResultsRecyclerView);
        mUserSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUserSearchResultRecyclerView.setAdapter(mUsersSearchAdapter);

        mSelectedUsersRecyclerView = view.findViewById(R.id.selectedUsersRecyclerView);
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
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void addUserToEvent() {

    }

    private void searchUsers(String queryString) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContains("displayName", queryString);
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, com.parse.ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error querying Parse for users: ", e);
                    return;
                }
                mUsersSearchAdapter.clear();
                mUsersSearchAdapter.addAll(users);
            }
        });
    }
}