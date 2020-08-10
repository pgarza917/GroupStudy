package com.example.studygroup.groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.models.Group;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupListFragment extends Fragment {
    public static final String TAG = GroupListFragment.class.getSimpleName();

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
        mGroupsAdapter = new GroupsAdapter(getContext(), mGroupsList, new GroupsAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                Group group = mGroupsList.get(position);
                Fragment fragment = new GroupDetailsRootFragment();
                Bundle data = new Bundle();
                data.putParcelable(Group.class.getSimpleName(), Parcels.wrap(group));
                fragment.setArguments(data);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.frameLayoutContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        mGroupsRecyclerView = view.findViewById(R.id.groupsRecyclerView);
        mProgressBar = view.findViewById(R.id.groupsListProgressBar);

        mGroupsRecyclerView.setAdapter(mGroupsAdapter);
        mGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        queryGroups();
    }

    private void queryGroups() {
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.whereEqualTo("users", ParseUser.getCurrentUser());
        query.include("files");
        query.include("events");
        query.include("users");
        query.orderByDescending("updatedAt");
        mProgressBar.setVisibility(View.VISIBLE);
        query.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> groups, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error querying Parse for groups: ", e);
                    return;
                }
                mGroupsList.clear();
                mGroupsList.addAll(groups);
                mGroupsAdapter.setIsCurrentMember(groups);
                mGroupsAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}