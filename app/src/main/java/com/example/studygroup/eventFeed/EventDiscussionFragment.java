package com.example.studygroup.eventFeed;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.adapters.EventPostsAdapter;
import com.example.studygroup.adapters.FileViewAdapter;
import com.example.studygroup.eventCreation.FileViewFragment;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
import com.example.studygroup.models.Post;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventDiscussionFragment extends Fragment {
    public static final String TAG = EventDiscussionFragment.class.getSimpleName();
    public static final int FILE_ADD_REQUEST_CODE = 2094;

    private FloatingActionButton mCreatePostButton;
    private RecyclerView mPostsRecyclerView;
    private ProgressBar mProgressBar;

    private List<Post> mPostsList;
    private EventPostsAdapter mPostsAdapter;
    private Event mEvent;
    private List<FileExtended> mPostFilesList;
    private FileViewAdapter mPostFilesAdapter;
    private Dialog mCreatePostDialog;

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

        mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));

        mCreatePostButton = view.findViewById(R.id.createPostFloatingActionButton);
        mPostsRecyclerView = view.findViewById(R.id.discussionRecyclerView);
        mProgressBar = view.findViewById(R.id.discussionPostsProgressBar);

        mPostsList = new ArrayList<>();
        mPostsAdapter = new EventPostsAdapter(mPostsList, getContext(), mEvent);
        mPostsRecyclerView.setAdapter(mPostsAdapter);
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mCreatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPostCreateDialog();
            }
        });

        queryEventPosts();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK) {
            Log.i(TAG, "Error returning to bottom dialog fragment");
            return;
        }

        if(requestCode == FILE_ADD_REQUEST_CODE) {
            mCreatePostDialog.show();
            // Retrieving the list of files a user would like to upload attached to their event
            // that they selected in the file view fragment
            List<FileExtended> files = data.getParcelableArrayListExtra("uploadFiles");
            mPostFilesList.addAll(files);
            mPostFilesAdapter.notifyDataSetChanged();
        }
    }

    private void showPostCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create Post");

        View view = ((MainActivity) getContext()).getLayoutInflater().inflate(R.layout.create_post_dialog_layout, null);
        builder.setView(view);

        EditText postContentEditText = view.findViewById(R.id.postCreateContentEditText);
        CheckBox notifyUsersCheckBox = view.findViewById(R.id.notifyUsersCheckBox);
        RecyclerView filesRecyclerView = view.findViewById(R.id.postAddedFilesRecyclerView);
        ImageButton addFilesImageButton = view.findViewById(R.id.postAddFilesImageButton);
        TextView submitPostTextView = view.findViewById(R.id.postStringTextView);


        final boolean[] notifyUsers = {false};
        mPostFilesList = new ArrayList<>();
        mPostFilesAdapter = new FileViewAdapter(getContext(), mPostFilesList);

        filesRecyclerView.setAdapter(mPostFilesAdapter);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration divider = new DividerItemDecoration(filesRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        filesRecyclerView.addItemDecoration(divider);

        notifyUsersCheckBox.setChecked(false);

        mCreatePostDialog = builder.create();

        notifyUsersCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                notifyUsers[0] = b;
            }
        });

        addFilesImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCreatePostDialog.hide();
                launchFileSelect();
            }
        });

        submitPostTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = postContentEditText.getText().toString();
                saveNewFiles(mPostFilesList);
                savePost(content, mPostFilesList, notifyUsers[0]);
            }
        });

        mCreatePostDialog.show();

    }

    private void queryEventPosts() {
        mProgressBar.setVisibility(View.VISIBLE);
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.whereEqualTo("event", mEvent);
        query.orderByDescending(Event.KEY_CREATED_AT);

        query.include("creator");
        query.include("files");

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error querying event posts: ", e);
                    return;
                }
                mPostsAdapter.clear();
                mPostsAdapter.addAll(posts);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void saveNewFiles(List<FileExtended> mPostFilesList) {
        for(FileExtended file : mPostFilesList) {
            file.saveInBackground();
            mEvent.addUnique("files", file);
        }
        mEvent.saveInBackground();
    }

    private void launchFileSelect() {
        List<ParseUser> eventUsers = mEvent.getUsers();

        Fragment fragment = new FileViewFragment();
        fragment.setTargetFragment(EventDiscussionFragment.this, FILE_ADD_REQUEST_CODE);

        Bundle data = new Bundle();
        data.putParcelableArrayList("filesAttached", (ArrayList<? extends Parcelable>) mPostFilesList);
        data.putParcelableArrayList("eventUsers", (ArrayList<? extends Parcelable>) eventUsers);
        fragment.setArguments(data);

        ((MainActivity) getContext()).getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frameLayoutContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void savePost(String content, List<FileExtended> files, boolean notifyUsers) {
        if(content == null || content.isEmpty()) {
            Toast.makeText(getContext(), "Please Enter a Caption for the Post", Toast.LENGTH_SHORT).show();
            return;
        }

        Post post = new Post();

        post.setCreator(ParseUser.getCurrentUser());
        post.setEvent(mEvent);
        post.setEdited(notifyUsers);
        post.setText(content);

        for(FileExtended file : files) {
            post.addUnique("files", file);
        }

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error saving new post to Parse: ", e);
                    return;
                }
                Toast.makeText(getContext(), "Post Created!", Toast.LENGTH_SHORT).show();
                mCreatePostDialog.dismiss();
                queryEventPosts();
            }
        });
    }
}