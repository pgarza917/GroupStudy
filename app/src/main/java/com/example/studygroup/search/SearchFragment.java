package com.example.studygroup.search;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.adapters.EventsAdapter;
import com.example.studygroup.adapters.UserSearchResultAdapter;
import com.example.studygroup.models.Event;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    public static final String TAG = SearchFragment.class.getSimpleName();

    private RecyclerView mEventsResultsRecyclerView;
    private RecyclerView mUsersResultsRecyclerView;
    private ProgressBar mProgressBar;

    private EventsAdapter mEventsAdapter;
    private List<Event> mEventsResultList;
    private UserSearchResultAdapter mUsersAdapter;
    private List<ParseUser> mUsersResultsList;
    private String mSearchCategory;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Search");
        mSearchCategory = "users";

        mEventsResultList = new ArrayList<>();
        mUsersResultsList = new ArrayList<>();
        mEventsAdapter = new EventsAdapter(getContext(), mEventsResultList);
        mUsersAdapter = new UserSearchResultAdapter(getContext(), mUsersResultsList, new UserSearchResultAdapter.ResultClickListener() {
            @Override
            public void onResultClicked(int position) {
                // Launch profile details
            }
        });

        mProgressBar = view.findViewById(R.id.searchFragmentProgressBar);
        mEventsResultsRecyclerView = view.findViewById(R.id.eventsResultsRecyclerView);
        mUsersResultsRecyclerView = view.findViewById(R.id.usersResultsRecyclerView);
        mEventsResultsRecyclerView.setVisibility(View.GONE);
        mUsersResultsRecyclerView.setVisibility(View.VISIBLE);

        mEventsResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mEventsResultsRecyclerView.setAdapter(mEventsAdapter);

        mUsersResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsersResultsRecyclerView.setAdapter(mUsersAdapter);

        DividerItemDecoration eventsDivider = new DividerItemDecoration(mEventsResultsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mEventsResultsRecyclerView.addItemDecoration(eventsDivider);

        DividerItemDecoration usersDivider = new DividerItemDecoration(mUsersResultsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mUsersResultsRecyclerView.addItemDecoration(usersDivider);


        final Spinner spinner = view.findViewById(R.id.selectionSpinner);

        List<String> options = new ArrayList<String>();
        options.add("Users");
        options.add("Events");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if(position == 0) {
                    mSearchCategory = "users";
                    mEventsResultsRecyclerView.setVisibility(View.GONE);
                    mUsersResultsRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mSearchCategory = "events";
                    mUsersResultsRecyclerView.setVisibility(View.GONE);
                    mEventsResultsRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                mProgressBar.setVisibility(View.VISIBLE);
                if(mSearchCategory.equals("events")) {
                    searchEvents(query);
                } else {
                    searchUsers(query);
                }
                // workaround to avoid issues with some emulators and keyboard devices
                //searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchItem.expandActionView();
        searchView.requestFocus();

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchUsers(String query) {
        ParseQuery<ParseUser> nameQuery = ParseUser.getQuery();
        nameQuery.whereContains("lowerDisplayName", query.toLowerCase());

        ParseQuery<ParseUser> emailQuery = ParseUser.getQuery();
        emailQuery.whereContains("email", query.toLowerCase());

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
                mUsersAdapter.clear();
                mUsersAdapter.addAll(users);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void searchEvents(String query) {
        ParseQuery<Event> eventsTitleQuery = ParseQuery.getQuery(Event.class);
        eventsTitleQuery.whereContains("title", query);

        ParseQuery<Event> eventsDescriptionQuery = ParseQuery.getQuery(Event.class);
        eventsDescriptionQuery.whereContains("description", query);

        ParseQuery<Event> eventsLocationQuery = ParseQuery.getQuery(Event.class);
        eventsLocationQuery.whereContains("locationName", query);

        List<ParseQuery<Event>> eventsQueries = new ArrayList<ParseQuery<Event>>();
        eventsQueries.add(eventsTitleQuery);
        eventsQueries.add(eventsDescriptionQuery);
        eventsQueries.add(eventsLocationQuery);

        ParseQuery<Event> eventsFullQuery = ParseQuery.or(eventsQueries);
        eventsFullQuery.orderByDescending(Event.KEY_CREATED_AT);
        eventsFullQuery.include(Event.KEY_FILES);
        eventsFullQuery.include(Event.KEY_USERS);

        eventsFullQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error executing event search query: ", e);
                    return;
                }
                Log.i(TAG, "Success searching for events");
                mEventsAdapter.clear();
                mEventsAdapter.addAll(events);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}