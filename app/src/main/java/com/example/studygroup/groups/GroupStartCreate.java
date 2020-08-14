package com.example.studygroup.groups;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.files.FileViewFragment;
import com.example.studygroup.eventCreation.users.AddUsersFragment;
import com.example.studygroup.models.FileExtended;
import com.example.studygroup.models.Group;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupStartCreate extends Fragment {

    public static final String TAG = GroupStartCreate.class.getSimpleName();
    public static final int FILE_PICKER_REQUEST_CODE = 4038;
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1092;

    private ImageButton mGroupPictureImageButton;
    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private Switch mPrivacySwitch;

    private Group mGroup;
    private boolean mIsOpen = false;

    // Member variables for camera functionality
    private ParseFile mParsePhotoFile;
    public String photoFileName = "photo.jpg";
    private File photoFile;

    public GroupStartCreate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_start_create, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.create_event_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutContainer);
        String fragmentName = currentFragment.getClass().getSimpleName();

        if(fragmentName.equals(TAG)) {
            if(item.getItemId() == R.id.action_check) {
                saveGroupChanges();

                Fragment fragment = new AddUsersFragment();
                Bundle data = new Bundle();
                data.putParcelable(Group.class.getSimpleName(), Parcels.wrap(mGroup));
                if(mParsePhotoFile != null) {
                    data.putParcelable("groupImage", mParsePhotoFile);
                    FileExtended file = new FileExtended();
                    file.setFile(mParsePhotoFile);
                    file.setFileName(mParsePhotoFile.getName());
                    file.saveInBackground();
                }
                fragment.setArguments(data);
                fragment.setTargetFragment(currentFragment, 100);
                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .add(R.id.frameLayoutContainer, fragment)
                        .addToBackStack(null)
                        .commit();

                return true;
            }
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGroup = Parcels.unwrap(getArguments().getParcelable(Group.class.getSimpleName()));

        mGroupPictureImageButton = view.findViewById(R.id.groupCreatePictureImageButton);
        mNameEditText = view.findViewById(R.id.groupCreateNameEditText);
        mDescriptionEditText = view.findViewById(R.id.groupCreateDescriptionEditText);
        mPrivacySwitch = view.findViewById(R.id.groupCreatePrivacySwitch);

        mPrivacySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsOpen = b;
            }
        });

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
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                    onLaunchCamera();
                } else {
                    onLaunchCamera();
                }
            }
        };

        mGroupPictureImageButton.setOnClickListener(new View.OnClickListener() {
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

        Glide.with(getContext())
                .load(mGroup.getGroupImage().getUrl())
                .circleCrop()
                .into(mGroupPictureImageButton);
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

            mParsePhotoFile = new ParseFile(fileToUpload, filename);

            Glide.with(getContext()).load(fileToUpload).circleCrop().into(mGroupPictureImageButton);
        }
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            // by this point we have the camera photo on disk
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            String filename = photoFile.getName();
            mParsePhotoFile = new ParseFile(photoFile, filename);

            Glide.with(getContext()).load(takenImage).circleCrop().into(mGroupPictureImageButton);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Camera Permission Granted");
                onLaunchCamera();
            } else {
                Log.i(TAG, "Camera Permission Denied");
            }
        }
    }

    // Method to handle the launching of the camera activity for users to take photos
    public void onLaunchCamera() {
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

    private void saveGroupChanges() {
        String groupName = mNameEditText.getText().toString();
        String groupDescription = mDescriptionEditText.getText().toString();

        mGroup.setGroupName(groupName);
        mGroup.setGroupDescription(groupDescription);

        if(mIsOpen) {
            mGroup.setGroupPrivacy("Open");
        } else {
            mGroup.setGroupPrivacy("Closed");
        }
    }
}