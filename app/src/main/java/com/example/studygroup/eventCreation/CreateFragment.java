package com.example.studygroup.eventCreation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.groups.GroupListFragment;
import com.example.studygroup.groups.GroupStartCreate;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
import com.example.studygroup.models.Group;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateFragment extends Fragment {
    public static final String TAG = CreateFragment.class.getSimpleName();

    private ImageButton mCreateEventImageButton;
    private ImageButton mCreateGroupImageButton;
    private TextView mCreateEventTextView;
    private TextView mCreateGroupTextView;

    public CreateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getContext()).getSupportActionBar().setTitle("Create");

        mCreateEventImageButton = view.findViewById(R.id.createEventImageButton);
        mCreateGroupImageButton = view.findViewById(R.id.createGroupImageButton);
        mCreateEventTextView = view.findViewById(R.id.createEventTextView);
        mCreateGroupTextView = view.findViewById(R.id.createGroupTextView);

        View.OnClickListener eventClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginEventCreationSeq();
            }
        };

        View.OnClickListener groupClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findAndSetDefaultPic();
            }
        };

        mCreateEventImageButton.setOnClickListener(eventClickListener);
        mCreateEventTextView.setOnClickListener(eventClickListener);

        mCreateGroupImageButton.setOnClickListener(groupClickListener);
        mCreateGroupTextView.setOnClickListener(groupClickListener);
    }

    private void beginEventCreationSeq() {
        Fragment fragment = new TitleDescAndSubjectFragment();
        Bundle data = new Bundle();
        Event event = new Event();
        data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(event));
        fragment.setArguments(data);

        ((MainActivity) getContext()).getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                .replace(R.id.frameLayoutContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void beginGroupCreationSeq(Group group) {
        Fragment fragment = new GroupStartCreate();
        Bundle data = new Bundle();
        data.putParcelable(Group.class.getSimpleName(), Parcels.wrap(group));
        fragment.setArguments(data);

        ((MainActivity) getContext()).getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                .replace(R.id.frameLayoutContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void findAndSetDefaultPic() {
        ParseQuery<FileExtended> fileQuery = ParseQuery.getQuery(FileExtended.class);
        fileQuery.whereEqualTo("objectId", "AIq6uAFqvh");

        fileQuery.findInBackground(new FindCallback<FileExtended>() {
            @Override
            public void done(List<FileExtended> files, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error querying Parse for default group image: ", e);
                    return;
                }
                FileExtended defaultGroupPictureFile = files.get(0);
                ParseFile file = defaultGroupPictureFile.getFile();
                Group group = new Group();
                group.put("groupImage", file);
                beginGroupCreationSeq(group);
            }
        });
    }
}