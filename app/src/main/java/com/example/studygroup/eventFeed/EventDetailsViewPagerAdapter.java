package com.example.studygroup.eventFeed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.studygroup.eventFeed.EventDetailsFragment;
import com.example.studygroup.eventFeed.EventDiscussionFragment;
import com.example.studygroup.models.Event;

import org.parceler.Parcels;

public class EventDetailsViewPagerAdapter extends FragmentStateAdapter {
    public static final int EVENT_DETAILS_ITEM_SIZE = 2;

    Event mEvent;

    public EventDetailsViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, Event event) {
        super(fragmentActivity);
        this.mEvent = event;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle data = new Bundle();
        data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));

        if(position == 0) {
            Fragment fragment = new EventDetailsFragment();
            fragment.setArguments(data);

            return fragment;
        } else {
            Fragment fragment = new EventDiscussionFragment();
            fragment.setArguments(data);

            return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return EVENT_DETAILS_ITEM_SIZE;
    }
}