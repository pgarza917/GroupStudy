package com.example.studygroup.groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.studygroup.eventFeed.DiscussionFragment;
import com.example.studygroup.eventFeed.EventDetailsFragment;
import com.example.studygroup.eventFeed.FilesFragment;
import com.example.studygroup.models.Group;

import org.parceler.Parcels;

public class GroupDetailsViewPagerAdapter extends FragmentStateAdapter {
    public static final int GROUP_DETAILS_ITEM_SIZE = 4;

    Group mGroup;

    public GroupDetailsViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, Group group) {
        super(fragmentActivity);
        this.mGroup = group;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle data = new Bundle();
        data.putParcelable(Group.class.getSimpleName(), Parcels.wrap(mGroup));

        if(position == 0) {
            Fragment fragment = new DiscussionFragment();
            fragment.setArguments(data);
            return fragment;
        } else if(position == 1) {
            Fragment fragment = new GroupEventsFragment();
            fragment.setArguments(data);
            return fragment;
        } else if(position == 2) {
            Fragment fragment = new FilesFragment();
            fragment.setArguments(data);
            return fragment;
        } else {
            Fragment fragment = new GroupDetailsFragment();
            fragment.setArguments(data);
            return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return GROUP_DETAILS_ITEM_SIZE;
    }
}
