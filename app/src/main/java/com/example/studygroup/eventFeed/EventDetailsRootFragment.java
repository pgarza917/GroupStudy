package com.example.studygroup.eventFeed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.models.Event;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.parceler.Parcels;

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
                        } else {
                            tab.setText("Discussion");
                        }
                    }
                }).attach();
    }
}