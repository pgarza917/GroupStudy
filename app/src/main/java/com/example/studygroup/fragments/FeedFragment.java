package com.example.studygroup.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.studygroup.R;

/**
 * A simple {@link Fragment} subclass.

 */
public class FeedFragment extends Fragment {

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recycler View steps:
        // 0. Create layout for one row in the list
        // 1. Create the adapter
        // 2. Create the data source
        // 3. Set the adapter on the Recycler View
        // 4. Set the layout manager on the Recycler View
    }
}