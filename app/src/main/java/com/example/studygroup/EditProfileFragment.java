package com.example.studygroup;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.studygroup.fragments.ProfileFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    public static final String TAG = EditProfileFragment.class.getSimpleName();

    private ImageButton mEditProfilePictureImageButton;
    private ImageView mEditPictureIndicatorImageView;
    private EditText mDisplayNameEditText;
    private EditText mPasswordEditText;
    private EditText mBioEditText;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditProfilePictureImageButton = view.findViewById(R.id.editProfilePictureImageButton);
        mEditPictureIndicatorImageView = view.findViewById(R.id.editPictureIndicatorImageView);
        mDisplayNameEditText = view.findViewById(R.id.editDisplayNameEditText);
        mPasswordEditText = view.findViewById(R.id.editPasswordEditText);
        mBioEditText = view.findViewById(R.id.editBioEditText);

        ParseFile profilePictureFile = ParseUser.getCurrentUser().getParseFile("profileImage");

        Glide.with(getContext()).load(profilePictureFile.getUrl()).circleCrop().into(mEditProfilePictureImageButton);

        mEditProfilePictureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.create_event_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_check) {
            String newDisplayName = mDisplayNameEditText.getText().toString();
            String newPassword = mPasswordEditText.getText().toString();
            String newBio = mBioEditText.getText().toString();

            updateCurrentUserSettings(newDisplayName, newPassword, newBio);
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateCurrentUserSettings(String newDisplayName, String newPassword, String newBio) {
        ParseUser currentUser = ParseUser.getCurrentUser();

        currentUser.put("displayName", newDisplayName);
        currentUser.setPassword(newPassword);
        currentUser.put("bio", newBio);

        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error saving changes to Parse: ", e);
                    Toast.makeText(getContext(), "Unable to Save Changes!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getContext(), "Changes Saved Successfully!", Toast.LENGTH_SHORT).show();
                Fragment fragment = new ProfileFragment();
                ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
            }
        });
    }
}