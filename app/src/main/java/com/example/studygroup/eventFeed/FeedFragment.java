package com.example.studygroup.eventFeed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.adapters.EventsAdapter;
import com.example.studygroup.models.Event;
import com.example.studygroup.search.SearchFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.

 */
public class FeedFragment extends Fragment {

    public static final String TAG = FeedFragment.class.getSimpleName();

    private RecyclerView mEventsRecyclerView;
    protected EventsAdapter mEventsAdapter;
    protected List<Event> mEventsList;
    protected SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mProgressBarLoading;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeContainer = view.findViewById(R.id.swipeContainer);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Events");

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryEvents();
            }
        });

        // Configure the refreshing colors
        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mEventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        mEventsList = new ArrayList<>();

        // Recycler View steps:
        // 0. Create layout for one row in the list
        // 1. Create the adapter
        // 2. Create the data source
        // 3. Set the adapter on the Recycler View
        mEventsAdapter = new EventsAdapter(getContext(), mEventsList);
        mEventsRecyclerView.setAdapter(mEventsAdapter);
        // 4. Set the layout manager on the Recycler View
        mEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration itemDecor = new DividerItemDecoration(mEventsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mEventsRecyclerView.addItemDecoration(itemDecor);

        mProgressBarLoading = view.findViewById(R.id.progressBarFeedLoading);
        mProgressBarLoading.setVisibility(View.VISIBLE);

        queryEvents();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Method to help query the Parse DB for events objects
    protected void queryEvents() {
        // Specify which class to query from Parse DB
        ParseQuery<Event> ownersQuery = ParseQuery.getQuery(Event.class);
        ownersQuery.whereEqualTo("owners", ParseUser.getCurrentUser());

        ParseQuery<Event> usersQuery = ParseQuery.getQuery(Event.class);
        usersQuery.whereEqualTo("users", ParseUser.getCurrentUser());

        List<ParseQuery<Event>> queries = new ArrayList<ParseQuery<Event>>();
        queries.add(usersQuery);
        queries.add(ownersQuery);

        ParseQuery<Event> mainQuery = ParseQuery.or(queries);
        mainQuery.orderByDescending(Event.KEY_CREATED_AT);
        mainQuery.include(Event.KEY_FILES);
        mainQuery.include(Event.KEY_USERS);

        // Using findInBackground to pull events from DB
        mainQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Issue with getting events from Parse DB", e);
                    return;
                }
                for(Event event : events) {
                    Log.i(TAG, "Event: " + event.getTitle());
                }
                // Update the events data set and notify adapter of the change
                mEventsAdapter.clear();
                mEventsAdapter.addAll(events);
                // Now we call setRefreshing(false) to signal refresh has finished
                mSwipeContainer.setRefreshing(false);
                mProgressBarLoading.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_search) {
            Fragment fragment = new SearchFragment();
            ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
        }

        return super.onOptionsItemSelected(item);
    }
}