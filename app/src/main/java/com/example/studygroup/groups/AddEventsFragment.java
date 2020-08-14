package com.example.studygroup.groups;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.LinkAddress;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventFeed.EventsAdapter;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.Group;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddEventsFragment extends Fragment {

    public static final String TAG = AddEventsFragment.class.getSimpleName();

    private SearchView mSearch;
    private RecyclerView mEventsSearchResultsRecyclerView;
    private RecyclerView mAddedEventsRecyclerView;

    private List<Event> mSearchResultsEventsList;
    private List<Event> mAddedEventsList;
    private EventsAdapter mSearchResultEventsAdapter;
    private EventsAdapter mAddedEventsAdapter;
    private Group mGroup;

    public AddEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_events, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.create_event_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutContainer);
        String fragmentName = currentFragment.getClass().getSimpleName();

        if (fragmentName.equals(TAG)) {
            if (item.getItemId() == R.id.action_check) {
                for (Event event : mAddedEventsList) {
                    mGroup.addUnique("events", event);
                }

                endEventsSelection();
                return true;
            }

            return super.onOptionsItemSelected(item);
        } else {
            return false;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getContext()).getSupportActionBar().setTitle("Add Events");

        mGroup = Parcels.unwrap(getArguments().getParcelable(Group.class.getSimpleName()));

        mSearch = view.findViewById(R.id.searchEventsSearchView);
        mEventsSearchResultsRecyclerView = view.findViewById(R.id.selectEventsRecyclerView);
        mAddedEventsRecyclerView = view.findViewById(R.id.addedEventsRecyclerView);

        mSearchResultsEventsList = new ArrayList<>();
        mAddedEventsList = new ArrayList<>();
        mSearchResultEventsAdapter = new EventsAdapter(getContext(), mSearchResultsEventsList, new EventsAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                Event event = mSearchResultsEventsList.get(position);
                mAddedEventsAdapter.addAll(Collections.singletonList(event));
            }
        });
        mAddedEventsAdapter = new EventsAdapter(getContext(), mAddedEventsList, new EventsAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                Event event = mSearchResultsEventsList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Remove " + event.getTitle() + " from selected events?");
                builder.setNegativeButton("No", null);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAddedEventsList.remove(position);
                        mAddedEventsAdapter.notifyItemRemoved(position);
                    }
                });

                Dialog dialog = builder.create();
                dialog.show();
            }
        });

        mEventsSearchResultsRecyclerView.setAdapter(mSearchResultEventsAdapter);
        mEventsSearchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAddedEventsRecyclerView.setAdapter(mAddedEventsAdapter);
        mAddedEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                searchEvents(query);
                return true;
            }
        });
    }

    private void searchEvents(String query) {
        ParseQuery<Event> titleQuery = ParseQuery.getQuery(Event.class);
        titleQuery.whereContains("title", query);

        ParseQuery<Event> descriptionQuery = ParseQuery.getQuery(Event.class);
        descriptionQuery.whereContains("description", query);

        ParseQuery<Event> locationNameQuery = ParseQuery.getQuery(Event.class);
        locationNameQuery.whereContains("locationName", query);

        List<ParseQuery<Event>> queries = new ArrayList<ParseQuery<Event>>();
        queries.add(titleQuery);
        queries.add(descriptionQuery);
        queries.add(locationNameQuery);

        ParseQuery<Event> fullEventQuery = ParseQuery.or(queries);
        fullEventQuery.orderByDescending(Event.KEY_TIME);

        fullEventQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error querying Parse for events: ", e);
                    return;
                }
                mSearchResultEventsAdapter.clear();
                mSearchResultEventsAdapter.addAll(events);
            }
        });
    }

    private void endEventsSelection() {
        if(getTargetFragment() != null) {
            Intent intent = new Intent();
            intent.putExtra(Group.class.getSimpleName(), Parcels.wrap(mGroup));
            getTargetFragment().onActivityResult(GroupEventsFragment.ADD_EVENTS_REQUEST_CODE, Activity.RESULT_OK, intent);
            FragmentManager fm = getActivity().getSupportFragmentManager();

            fm.popBackStackImmediate();
        } else {
            Fragment fragment = new ConfirmGroupFragment();
            Bundle data = new Bundle();
            data.putParcelable(Group.class.getSimpleName(), Parcels.wrap(mGroup));
            if (getArguments().containsKey("groupImage")) {
                data.putParcelable("groupImage", getArguments().getParcelable("groupImage"));
            }
            fragment.setArguments(data);

            ((MainActivity) getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                    .add(R.id.frameLayoutContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}