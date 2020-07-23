package com.example.studygroup.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.FileViewFragment;
import com.example.studygroup.models.FileExtended;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    public static final String TAG = EditProfileFragment.class.getSimpleName();
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 3578;
    public static final int FILE_PICKER_REQUEST_CODE = 9376;

    private ImageButton mEditProfilePictureImageButton;
    private ImageView mEditPictureIndicatorImageView;
    private EditText mDisplayNameEditText;
    private EditText mPasswordEditText;
    private EditText mBioEditText;

    private ParseFile mNewPhotoFile;

    // Member variables for camera functionality
    public String photoFileName = "photo.jpg";
    File photoFile;

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
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditProfilePictureImageButton = view.findViewById(R.id.editProfilePictureImageButton);
        mEditPictureIndicatorImageView = view.findViewById(R.id.editPictureIndicatorImageView);
        mDisplayNameEditText = view.findViewById(R.id.editDisplayNameEditText);
        mPasswordEditText = view.findViewById(R.id.editPasswordEditText);
        mBioEditText = view.findViewById(R.id.editBioEditText);

        ParseFile profilePictureFile = ParseUser.getCurrentUser().getParseFile("profileImage");

        Glide.with(getContext()).load(profilePictureFile.getUrl()).circleCrop().into(mEditProfilePictureImageButton);

        final DialogInterface.OnClickListener uploadListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "Launching file picker activity");
                // Here we start a file picker activity by allowing android to use the
                // most appropriate application it had to handle the picking. It will
                // then return to us the selected file
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("image/png image/jpeg");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, FILE_PICKER_REQUEST_CODE);
            }
        };

        final DialogInterface.OnClickListener takePhotoListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onLaunchCamera(view);
            }
        };

        mEditProfilePictureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("Select Photo Edit Method");

                builder.setPositiveButton("Upload Photo", uploadListener);
                builder.setNeutralButton("Take a Photo", takePhotoListener);
                builder.setNegativeButton("Cancel", null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK) {
            Log.i(TAG, "Error returning from launched activity");
            return;
        }

        if(requestCode == FILE_PICKER_REQUEST_CODE) {
            // Retrieves the URI of the file the user has picked
            Uri uri = data.getData();
            ContentResolver contentResolver = getContext().getContentResolver();

            String filename = FileViewFragment.queryName(contentResolver, uri);
            File fileToUpload = FileViewFragment.createTempFile(filename, getContext());
            fileToUpload = FileViewFragment.saveContentToFile(uri, fileToUpload, contentResolver);

            Date lastModDate = new Date(fileToUpload.lastModified());

            mNewPhotoFile = new ParseFile(fileToUpload, filename);

            Glide.with(getContext()).load(fileToUpload).circleCrop().into(mEditProfilePictureImageButton);
        }
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            // by this point we have the camera photo on disk
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            String filename = photoFile.getName();
            mNewPhotoFile = new ParseFile(photoFile, filename);

            Glide.with(getContext()).load(takenImage).circleCrop().into(mEditProfilePictureImageButton);
        }
    }

    private void updateCurrentUserSettings(String newDisplayName, String newPassword, String newBio) {
        ParseUser currentUser = ParseUser.getCurrentUser();

        if(!newDisplayName.isEmpty()) {
            currentUser.put("displayName", newDisplayName);
        }
        if(!newPassword.isEmpty()) {
            currentUser.setPassword(newPassword);
        }
        if(!newBio.isEmpty()) {
            currentUser.put("bio", newBio);
        }
        if(mNewPhotoFile != null) {
            currentUser.put("profileImage", mNewPhotoFile);
        }

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

    // Method to handle the launching of the camera activity for users to take photos
    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.studygroup", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }
}