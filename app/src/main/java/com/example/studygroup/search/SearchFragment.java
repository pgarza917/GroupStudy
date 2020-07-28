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
import android.widget.ProgressBar;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.adapters.EventsAdapter;
import com.example.studygroup.models.Event;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    public static final String TAG = SearchFragment.class.getSimpleName();

    private RecyclerView mEventsResultsRecyclerView;
    private ProgressBar mProgressBar;

    private EventsAdapter mEventsAdapter;
    private List<Event> mEventsResultList;

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

        mEventsResultList = new ArrayList<>();
        mEventsAdapter = new EventsAdapter(getContext(), mEventsResultList);

        mProgressBar = view.findViewById(R.id.searchFragmentProgressBar);
        mEventsResultsRecyclerView = view.findViewById(R.id.eventsResultsRecyclerView);

        mEventsResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mEventsResultsRecyclerView.setAdapter(mEventsAdapter);

        DividerItemDecoration itemDecor = new DividerItemDecoration(mEventsResultsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mEventsResultsRecyclerView.addItemDecoration(itemDecor);

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
                searchEvents(query);
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
        eventsFullQuery.include(Event.KEY_FILES);
        eventsFullQuery.include(Event.KEY_USERS);

        eventsFullQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error executing search query: ", e);
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