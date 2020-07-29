package com.example.studygroup.messaging;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.studygroup.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationFragment extends Fragment {

    private ImageView mToolbarImageView;
    private TextView mToolbarTextView;

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar_messages);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mToolbarImageView = view.findViewById(R.id.toolbarProfilePictureImageView);
        mToolbarTextView = view.findViewById(R.id.toolbarUserNameTextView);


    }
}