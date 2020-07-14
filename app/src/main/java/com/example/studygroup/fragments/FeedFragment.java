package com.example.studygroup.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.studygroup.R;
import com.example.studygroup.adapters.EventsAdapter;
import com.example.studygroup.models.Event;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;

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

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        queryEvents();
    }

    // Method to help query the Parse DB for events objects
    protected void queryEvents() {
        // Specify which class to query from Parse DB
        ParseQuery<Event> eventsQuery = ParseQuery.getQuery(Event.class);
        eventsQuery.setLimit(20);
        eventsQuery.orderByDescending(Event.KEY_CREATED_AT);
        // Using findInBackground to pull events from DB
        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Issue with getting events from Parse DB", e);
                    return;
                }
                // Update the events data set and notify adapter of the change
                mEventsAdapter.clear();
                mEventsAdapter.addAll(events);
            }
        });
    }
}