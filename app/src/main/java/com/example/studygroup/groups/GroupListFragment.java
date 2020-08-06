package com.example.studygroup.groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.models.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupListFragment extends Fragment {

    private RecyclerView mGroupsRecyclerView;
    private ProgressBar mProgressBar;

    private List<Group> mGroupsList;
    private GroupsAdapter mGroupsAdapter;

    public GroupListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getContext()).getSupportActionBar().setTitle("Groups");

        mGroupsList = new ArrayList<>();
        mGroupsAdapter = new GroupsAdapter(getContext(), mGroupsList);

        mGroupsRecyclerView = view.findViewById(R.id.groupsRecyclerView);
        mProgressBar = view.findViewById(R.id.groupsListProgressBar);

        mGroupsRecyclerView.setAdapter(mGroupsAdapter);
        mGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


    }
}