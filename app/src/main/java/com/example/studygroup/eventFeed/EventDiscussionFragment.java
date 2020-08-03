package com.example.studygroup.eventFeed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.studygroup.R;
import com.example.studygroup.adapters.EventPostsAdapter;
import com.example.studygroup.models.Post;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventDiscussionFragment extends Fragment {

    private FloatingActionButton mCreatePostButton;
    private RecyclerView mPostsRecyclerView;

    private List<Post> mPostsList;
    private EventPostsAdapter mPostsAdapter;

    public EventDiscussionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_discussion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCreatePostButton = view.findViewById(R.id.createPostFloatingActionButton);
        mPostsRecyclerView = view.findViewById(R.id.discussionRecyclerView);

        mPostsList = new ArrayList<>();
        mPostsAdapter = new EventPostsAdapter(mPostsList, getContext());
        mPostsRecyclerView.setAdapter(mPostsAdapter);
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mCreatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });
    }

    private void showBottomSheetDialog() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_post_create_bottom_sheet, null);
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());

        dialog.setTitle("Create Post");
        dialog.setContentView(view);
        dialog.show();
    }
}