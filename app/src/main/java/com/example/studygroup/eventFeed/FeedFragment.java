package com.example.studygroup.eventFeed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.example.studygroup.models.Event;
import com.example.studygroup.models.Subject;
import com.example.studygroup.profile.ProfileFragment;
import com.example.studygroup.search.SearchFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A simple {@link Fragment} subclass.

 */
public class FeedFragment extends Fragment {

    public static final String TAG = FeedFragment.class.getSimpleName();

    protected EventsAdapter mEventsAdapter;
    protected List<Event> mEventsList;
    protected SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mProgressBarLoading;

    int lastPosition = 0;

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
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

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

        RecyclerView mEventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        mEventsList = new ArrayList<>();

        // Recycler View steps:
        // 0. Create layout for one row in the list
        // 1. Create the adapter
        // 2. Create the data source
        // 3. Set the adapter on the Recycler View
        mEventsAdapter = new EventsAdapter(getContext(), mEventsList, new EventsAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                Event event = mEventsList.get(position);

                if(position != RecyclerView.NO_POSITION) {

                    Fragment fragment = new EventDetailsRootFragment();

                    Bundle data = new Bundle();
                    data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(event));
                    data.putInt("position", position);
                    fragment.setArguments(data);

                    Fragment currentFragment = ((MainActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.frameLayoutContainer);
                    if(currentFragment.getClass().getSimpleName().equals(ProfileFragment.TAG)) {
                        fragment.setTargetFragment(currentFragment, ProfileFragment.RC_DETAILS);
                    }

                    ((MainActivity) getContext()).getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.frameLayoutContainer, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        mEventsRecyclerView.setAdapter(mEventsAdapter);
        // 4. Set the layout manager on the Recycler View
        mEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //DividerItemDecoration itemDecor = new DividerItemDecoration(mEventsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        //mEventsRecyclerView.addItemDecoration(itemDecor);

        mProgressBarLoading = view.findViewById(R.id.progressBarFeedLoading);
        mProgressBarLoading.setVisibility(View.VISIBLE);

        queryEvents();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
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
        mainQuery.orderByDescending(Event.KEY_TIME);
        mainQuery.include(Event.KEY_FILES);
        mainQuery.include(Event.KEY_USERS);

        // Using findInBackground to pull events from DB
        mainQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Issue with getting events from Parse DB: ", e);
                    return;
                }
                for(Event event : events) {
                    Log.i(TAG, "Event: " + event.getTitle());
                }
                mEventsList.clear();
                mEventsList.addAll(events);

                queryPotentialSuggestions();
                // Now we call setRefreshing(false) to signal refresh has finished
            }
        });
    }

    protected void queryPotentialSuggestions() {
        ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        query.whereNotEqualTo("owners", ParseUser.getCurrentUser());
        query.whereNotEqualTo("users", ParseUser.getCurrentUser());
        query.whereEqualTo("privacy", "open");

        query.orderByDescending(Event.KEY_CREATED_AT);
        query.include(Event.KEY_FILES);
        query.include(Event.KEY_USERS);
        query.include("subject");

        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue getting suggestion events from Parse DB: ", e);
                    return;
                }
                if (events.size() > 0) {
                    for (Event event : events) {
                        addSuggestions(event);
                    }
                } else {
                    mEventsAdapter.notifyDataSetChanged();
                }
                mSwipeContainer.setRefreshing(false);
                mProgressBarLoading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void addSuggestions(final Event event) {
        ParseUser user = ParseUser.getCurrentUser();
        ParseRelation<Subject> subjectInterests = user.getRelation("subjectInterests");

        ParseObject subject = event.getParseObject("subject");

        ParseQuery<Subject> query = subjectInterests.getQuery();
        query.whereEqualTo("objectId", subject.getObjectId());

        query.findInBackground(new FindCallback<Subject>() {
            @Override
            public void done(List<Subject> subjects, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error querying for subject interests: ", e);
                    return;
                }
                event.setSuggestion(true);
                if(lastPosition < mEventsList.size() - 1) {
                    lastPosition = ThreadLocalRandom.current().nextInt(lastPosition, mEventsList.size() - 1);
                }
                if(subjects.size() == 0) {
                    mEventsList.add(event);
                } else {
                    mEventsList.add(lastPosition, event);
                }
                mEventsAdapter.notifyDataSetChanged();
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