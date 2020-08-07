package com.example.studygroup.eventFeed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.CreateEventFragment;
import com.example.studygroup.models.Event;
import com.example.studygroup.profile.ProfileFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventDetailsRootFragment extends Fragment {

    public static final String TAG = EventDetailsRootFragment.class.getSimpleName();

    private Event mEvent;

    public EventDetailsRootFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details_root, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.edit_event_menu, menu);

        MenuItem editIcon = menu.findItem(R.id.action_edit_event);
        if(!eventOwnersContainsUser(mEvent, ParseUser.getCurrentUser())) {
            editIcon.setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_edit_event) {
            Fragment fragment = new CreateEventFragment();
            Bundle data = new Bundle();
            data.putParcelable("event", mEvent);
            fragment.setArguments(data);
            fragment.setTargetFragment(EventDetailsRootFragment.this, 123);
            ((MainActivity) getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.frameLayoutContainer, fragment)
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 mViewPager = view.findViewById(R.id.eventDetailsViewPager);
        TabLayout mTabLayout = view.findViewById(R.id.eventDetailsTabLayout);

        mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));
        Log.i(TAG, "Received Bundled Event Data!");

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(mEvent.getTitle());

        mViewPager.setAdapter(new EventDetailsViewPagerAdapter(getActivity(), mEvent));
        new TabLayoutMediator(mTabLayout, mViewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if(position == 0) {
                            tab.setText("Details");
                        } else if(position == 1) {
                            tab.setText("Discussion");
                        } else {
                            tab.setText("Files");
                        }
                    }
                }).attach();

        EventDetailsRootFragment fragment = (EventDetailsRootFragment) getFragmentManager().findFragmentById(R.id.frameLayoutContainer);
        fragment.getView().setFocusableInTouchMode(true);
        fragment.getView().requestFocus();
        fragment.getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(getTargetFragment() != null) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Fragment fragment1 = new ProfileFragment();
                        ((MainActivity) getContext()).getSupportFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                                .replace(R.id.frameLayoutContainer, fragment1)
                                .commit();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public static boolean eventOwnersContainsUser(Event event, ParseUser user) {
        List<ParseUser> eventOwners = event.getOwners();
        for(int i = 0; i < eventOwners.size(); i++) {
            ParseUser currentOwner = eventOwners.get(i);
            if(currentOwner.getObjectId().equals(user.getObjectId())) {
                return true;
            }
        }
        return false;
    }
}