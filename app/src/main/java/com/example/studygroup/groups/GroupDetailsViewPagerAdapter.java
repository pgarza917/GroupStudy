package com.example.studygroup.groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.studygroup.eventFeed.DiscussionFragment;
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

        Fragment fragment;
        switch(position) {
            case 0:
                fragment = new DiscussionFragment();
                break;
            case 1:
                fragment = new DiscussionFragment();
                break;
            case 2:
                fragment = new FilesFragment();
                break;
            case 3:
                fragment = new GroupDetailsFragment();
                break;
            default:
                fragment = new DiscussionFragment();
                break;
        }
        fragment.setArguments(data);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return GROUP_DETAILS_ITEM_SIZE;
    }
}
