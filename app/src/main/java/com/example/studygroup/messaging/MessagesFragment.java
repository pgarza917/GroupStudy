package com.example.studygroup.messaging;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import com.applozic.mobicomkit.Applozic;
import com.example.studygroup.R;
import com.example.studygroup.adapters.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;

    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = view.findViewById(R.id.viewPager);
        mTabLayout = view.findViewById(R.id.tabLayout);

        mViewPager.setAdapter(new ViewPagerAdapter(getActivity()));
        new TabLayoutMediator(mTabLayout, mViewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if(position == 1) {
                    tab.setText("Direct Messages");
                } else {
                    tab.setText("Group Messages");
                }
            }
        }).attach();
    }
}