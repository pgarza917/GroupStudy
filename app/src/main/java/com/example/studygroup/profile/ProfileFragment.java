package com.example.studygroup.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studygroup.profile.EditProfileFragment;
import com.example.studygroup.MainActivity;
import com.example.studygroup.loginAndRegister.LoginActivity;
import com.example.studygroup.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private Button mLogoutButton;
    private TextView mProfileNameTextView;
    private TextView mBioTextView;
    private ImageView mProfilePictureImageView;
    private TextView mEmailTextView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLogoutButton = view.findViewById(R.id.logoutButton);
        mProfileNameTextView = view.findViewById(R.id.profileNameTextView);
        mBioTextView = view.findViewById(R.id.profileBioTextView);
        mProfilePictureImageView = view.findViewById(R.id.profilePictureImageView);
        mEmailTextView = view.findViewById(R.id.emailTextView);

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        ParseUser currentUser = ParseUser.getCurrentUser();
        String displayName = currentUser.getString("displayName");

        mProfileNameTextView.setText(displayName);

        String bio = (String) currentUser.get("bio");
        if(bio == null || bio.isEmpty()) {
            mBioTextView.setText(getString(R.string.no_bio));
        } else {
            mBioTextView.setText(bio);
        }

        ParseFile profileImage = ParseUser.getCurrentUser().getParseFile("profileImage");
        if(profileImage != null) {
            mProfilePictureImageView.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(profileImage.getUrl()).circleCrop().into(mProfilePictureImageView);
        } else {
            mProfilePictureImageView.setVisibility(View.GONE);
        }
        mEmailTextView.setText(currentUser.getEmail());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_edit_profile) {
            Fragment fragment = new EditProfileFragment();
            ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
        }

        return super.onOptionsItemSelected(item);
    }
}