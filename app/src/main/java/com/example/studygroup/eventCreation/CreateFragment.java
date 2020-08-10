package com.example.studygroup.eventCreation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.example.studygroup.models.Group;

import org.parceler.Parcels;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateFragment extends Fragment {

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
                beginGroupCreationSeq();
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

    private void beginGroupCreationSeq() {
        Fragment fragment = new GroupStartCreate();
        Bundle data = new Bundle();
        Group group = new Group();
        data.putParcelable(Group.class.getSimpleName(), Parcels.wrap(group));
        fragment.setArguments(data);

        ((MainActivity) getContext()).getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                .replace(R.id.frameLayoutContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}