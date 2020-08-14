package com.example.studygroup.groups;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventFeed.EventDetailsRootFragment;
import com.example.studygroup.eventFeed.EventsAdapter;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.Group;
import com.example.studygroup.profile.ProfileFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupEventsFragment extends Fragment {
    public static final String TAG = GroupEventsFragment.class.getSimpleName();
    public static final int ADD_EVENTS_REQUEST_CODE = 3692;

    private TextView mNoEventsTextView;

    private EventsAdapter mEventsAdapter;
    private Group mGroup;

    public GroupEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGroup = Parcels.unwrap(getArguments().getParcelable(Group.class.getSimpleName()));

        RecyclerView EventsRecyclerView = view.findViewById(R.id.groupEventsRecyclerView);
        FloatingActionButton AddEventsFloatingActionButton = view.findViewById(R.id.addGroupEventsFloatingActionButton);
        mNoEventsTextView = view.findViewById(R.id.noEventsTextView);

        List<Event> mEventsList = new ArrayList<>();
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

        AddEventsFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new AddEventsFragment();
                Bundle data = new Bundle();
                data.putParcelable(Group.class.getSimpleName(), Parcels.wrap(mGroup));
                fragment.setArguments(data);
                fragment.setTargetFragment(GroupEventsFragment.this, ADD_EVENTS_REQUEST_CODE);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .add(R.id.frameLayoutContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        EventsRecyclerView.setAdapter(mEventsAdapter);
        EventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getGroupEvents();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK) {
            Log.i(TAG, "Error returning from adding events!");
            return;
        }

        if(requestCode == ADD_EVENTS_REQUEST_CODE) {
            mGroup = Parcels.unwrap(data.getParcelableExtra(Group.class.getSimpleName()));
            mGroup.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    getGroupEvents();
                }
            });
        }
    }

    private void getGroupEvents() {
        List<Event> groupEvents = mGroup.getGroupEvents();
        if(groupEvents != null && groupEvents.size() > 0) {
            mEventsAdapter.clear();
            mEventsAdapter.addAll(groupEvents);
        } else {
            mNoEventsTextView.setVisibility(View.VISIBLE);
        }
    }
}