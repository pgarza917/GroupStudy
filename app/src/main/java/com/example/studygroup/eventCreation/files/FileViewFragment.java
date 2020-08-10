package com.example.studygroup.eventCreation.files;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.studygroup.ConfirmEventFragment;
import com.example.studygroup.DriveServiceHelper;
import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.users.UsersAdapter;
import com.example.studygroup.eventCreation.users.AddUsersFragment;
import com.example.studygroup.eventCreation.CreateEventFragment;
import com.example.studygroup.eventFeed.DiscussionFragment;
import com.example.studygroup.groups.AddEventsFragment;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
import com.example.studygroup.models.Group;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileViewFragment extends Fragment {

    public static final String TAG = FileViewFragment.class.getSimpleName();
    public static final int FILE_PICKER_REQUEST_CODE = 1940;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static final int RC_REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION = 6730;
    public static final int ADD_USERS_REQUEST_CODE = 3490;

    private FileViewAdapter mFileViewAdapter;
    private UsersAdapter mUsersAdapter;
    private List<FileExtended> mFilesList;
    private List<ParseUser> mEventUsers;
    private List<ParseUser> mNewEventUsers;
    private List<ParseUser> mUsers;
    private DriveServiceHelper mDriveServiceHelper;
    private String mOpenFileId;
    private AlertDialog mFileDialog;
    private Event mEvent;
    private Group mGroup;

    private EditText mFileTitleEditText;
    private ProgressBar mProgressBar;

    // Member variables for camera functionality
    public String photoFileName = "photo.jpg";
    File photoFile;

    public FileViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_view, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.create_event_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutContainer);
        String fragmentName = fragment.getClass().getSimpleName();

        if(fragmentName.equals(FileViewFragment.class.getSimpleName())) {
            switch(item.getItemId()) {
                case R.id.action_check:
                    if(mEvent != null) {
                        for (ParseUser user : mNewEventUsers) {
                            mEvent.addUnique("users", user);
                        }
                        for (FileExtended file : mFilesList) {
                            mEvent.addUnique("files", file);
                        }
                    } else {
                        for(ParseUser user : mNewEventUsers) {
                            mGroup.addUnique("users", user);
                        }
                        for(FileExtended file : mFilesList) {
                            mGroup.addUnique("files", file);
                        }
                    }
                    endFileSelection();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        else {
            return false;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Files");

        List<FileExtended> alreadyAttachedFiles;
        List<ParseUser> alreadySelectedUsers;
        if(getArguments().containsKey(Group.class.getSimpleName())) {
            mGroup = Parcels.unwrap(getArguments().getParcelable(Group.class.getSimpleName()));
            alreadyAttachedFiles = mGroup.getGroupFiles();
            alreadySelectedUsers = mGroup.getGroupUsers();
        }
        else {
            mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));
            alreadyAttachedFiles = mEvent.getFiles();
            alreadySelectedUsers = mEvent.getUsers();
        }
        if(alreadyAttachedFiles != null) {
            if(mFilesList == null) mFilesList = new ArrayList<>();
            mFilesList.addAll(alreadyAttachedFiles);
        } else {
            mFilesList = new ArrayList<>();
        }

        if(alreadySelectedUsers != null) {
            if(mEventUsers == null) mEventUsers = new ArrayList<>();
            mEventUsers.addAll(alreadySelectedUsers);
        } else {
            mEventUsers = new ArrayList<>();
        }

        mNewEventUsers = new ArrayList<>();
        mUsers = new ArrayList<>();

        mProgressBar = view.findViewById(R.id.fileViewProgressBar);
        RecyclerView mFileViewRecyclerView = view.findViewById(R.id.uploadedFilesRecyclerView);
        ImageButton mCreateDocImageButton = view.findViewById(R.id.googleDocsImageButton);
        ImageButton mTakePhotoImageButton = view.findViewById(R.id.takePhotoImageButton);
        ImageButton mUploadFilesImageButton = view.findViewById(R.id.uploadFilesImageButton);

        UsersAdapter.CheckBoxListener checkBoxListener = new UsersAdapter.CheckBoxListener() {
            @Override
            public void onBoxChecked(int position) {

            }

            @Override
            public void onBoxUnchecked(int position) {
                ParseUser user = mEventUsers.get(position);
                mUsersAdapter.remove(user);
            }
        };
        mUsersAdapter = new UsersAdapter(getContext(), mUsers, checkBoxListener);

        mFileViewAdapter = new FileViewAdapter(getContext(), mFilesList);
        mFileViewRecyclerView.setAdapter(mFileViewAdapter);

        mFileViewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration itemDecor = new DividerItemDecoration(mFileViewRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mFileViewRecyclerView.addItemDecoration(itemDecor);

        mUploadFilesImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Launching file picker activity");
                // Here we start a file picker activity by allowing android to use the
                // most appropriate application it had to handle the picking. It will
                // then return to us the selected file
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, FILE_PICKER_REQUEST_CODE);
            }
        });

        mTakePhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Launching camera activity");
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                    onLaunchCamera();
                } else {
                    onLaunchCamera();
                }
            }
        });

        mCreateDocImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Beginning to create new Google Doc for event");

                if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getActivity()), new Scope(DriveScopes.DRIVE_FILE))) {
                    GoogleSignIn.requestPermissions(
                            getActivity(),
                            RC_REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION,
                            GoogleSignIn.getLastSignedInAccount(getActivity()),
                            new Scope(DriveScopes.DRIVE_FILE));
                } else {
                    gsuiteConfigureCreateDialog();
                }
            }
        });

        FileViewFragment fragment = (FileViewFragment) getFragmentManager().findFragmentById(R.id.frameLayoutContainer);
        fragment.getView().setFocusableInTouchMode(true);
        fragment.getView().requestFocus();
        fragment.getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    endFileSelection();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        if(requestCode == FILE_PICKER_REQUEST_CODE) {
            // Retrieves the URI of the file the user has picked
            Uri uri = data.getData();
            ContentResolver contentResolver = getContext().getContentResolver();

            String filename = queryName(contentResolver, uri);
            File fileToUpload = createTempFile(filename, getContext());
            fileToUpload = saveContentToFile(uri, fileToUpload, contentResolver);

            Date lastModDate = new Date(fileToUpload.lastModified());

            ParseFile parseFile = new ParseFile(fileToUpload, filename);
            long fileSize = fileToUpload.length();
            FileExtended file = new FileExtended();

            // Sets the necessary fields of the FileExtended object so that it can be uploaded
            // to the Parse DB
            file.setFile(parseFile);
            file.setFileName(filename);
            file.setFileSize(fileSize);
            file.setLastModified(lastModDate);

            // Adds the new attached file to the Recycler View so the user knows the file is now
            // attached to their event for upload
            mFilesList.add(0, file);
            mFileViewAdapter.notifyItemInserted(0);
        }
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            // by this point we have the camera photo on disk
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            String filename = photoFile.getName();
            ParseFile fileData = new ParseFile(photoFile, filename);
            FileExtended fileToUpload = new FileExtended();

            // Sets the necessary fields of the FileExtended object so that it can be uploaded
            // to the Parse DB
            fileToUpload.setFileName(filename);
            fileToUpload.setFile(fileData);
            fileToUpload.setFileSize(photoFile.length());

            // Adds the new attached file to the Recycler View so the user knows the file is now
            // attached to their event for upload
            mFilesList.add(0, fileToUpload);
            mFileViewAdapter.notifyItemInserted(0);
        }
        if(requestCode == RC_REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION) {
            gsuiteConfigureCreateDialog();
        }
        if(requestCode == ADD_USERS_REQUEST_CODE) {
            mFileDialog.show();
            List<ParseUser> newUsers = data.getParcelableArrayListExtra("eventUsers");
            if(newUsers != null) {
                addNewUsers(newUsers);

                mUsersAdapter.clear();
                mUsersAdapter.addAll(newUsers);
            }
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

    private void gsuiteConfigureCreateDialog() {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
        Account account = GoogleSignIn.getLastSignedInAccount(getContext()).getAccount();
        credential.setSelectedAccount(account);
        Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName("Study Group")
                .build();
        mDriveServiceHelper = new DriveServiceHelper(googleDriveService);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Drive File Creation");

        final View customDialogLayout = ((MainActivity) getContext()).getLayoutInflater().inflate(R.layout.gsuite_create_dialog_layout, null);
        builder.setView(customDialogLayout);

        mFileTitleEditText = customDialogLayout.findViewById(R.id.dialogFileTitleEditText);
        CheckBox addAllEventUsersCheckBox = customDialogLayout.findViewById(R.id.addEventUsersCheckBox);
        Spinner typeSelectSpinner = customDialogLayout.findViewById(R.id.googleFileTypeSpinner);
        LinearLayout addOtherUsersLinearLayout = customDialogLayout.findViewById(R.id.addOtherUsersLinearLayout);

        addAllEventUsersCheckBox.setChecked(false);

        RecyclerView usersForShareRecyclerView = customDialogLayout.findViewById(R.id.usersForShareRecyclerView);
        usersForShareRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersForShareRecyclerView.setAdapter(mUsersAdapter);

        List<String> fileTypeOptions = new ArrayList<String>();
        fileTypeOptions.add("Select File Type");
        fileTypeOptions.add("Doc");
        fileTypeOptions.add("Sheet");
        fileTypeOptions.add("Slide");

        int[] fileType = {0};

        ArrayAdapter<String> fileTypeSpinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, fileTypeOptions);
        fileTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSelectSpinner.setAdapter(fileTypeSpinnerAdapter);
        typeSelectSpinner.setSelection(0);
        typeSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                fileType[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(fileType[0] == 0) {
                    Toast.makeText(getContext(), "Please Select a File Type", Toast.LENGTH_SHORT).show();
                    return;
                } else if(fileType[0] == 1) {
                    createDriveFile("application/vnd.google-apps.document", -1);
                } else if(fileType[0] == 2) {
                    createDriveFile("application/vnd.google-apps.spreadsheet", -2);
                } else {
                    createDriveFile("application/vnd.google-apps.presentation", -3);
                }
            }
        });

        addAllEventUsersCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    mUsersAdapter.addAll(mEventUsers);
                } else {
                    mUsersAdapter.removeAll(mEventUsers);
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        mFileDialog = builder.create();
        mFileDialog.show();

        addOtherUsersLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFileDialog.hide();
                Fragment fragment = new AddUsersFragment();
                fragment.setTargetFragment(FileViewFragment.this, ADD_USERS_REQUEST_CODE);
                Bundle data = new Bundle();
                data.putParcelableArrayList("eventUsers", (ArrayList<? extends Parcelable>) mEventUsers);
                fragment.setArguments(data);
                ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction()
                        .add(R.id.frameLayoutContainer, fragment, AddUsersFragment.class.getSimpleName())
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    // This method helps in determining the filename from a content URI using a content resolver
    public static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor = resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    // This method creates a temporary file in the application cache and returns it
    public static File createTempFile(String name, Context context) {
        File file = null;
        try {
            file = File.createTempFile(name, null, context.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    // This method reads a content by opening an input stream from its uri and feeding that into
    // file to write all the data to that file from the accessed content. Users the Okio IO library
    // to accomplish this
    public static File saveContentToFile(Uri uri, File file, ContentResolver contentResolver) {
        try {
            InputStream stream = contentResolver.openInputStream(uri);
            BufferedSource source = Okio.buffer(Okio.source(stream));
            BufferedSink sink = Okio.buffer(Okio.sink(file));
            sink.writeAll(source);
            sink.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file;
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

    /**
     * Creates a new file via the Drive REST API.
     */
    private void createDriveFile(String mimeType, int googleType) {
        if (mDriveServiceHelper != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            Log.d(TAG, "Creating a file.");

            String fileName = mFileTitleEditText.getText().toString();
            if(fileName.isEmpty()) {
                fileName = "Untitled";
            }

            final String finalFileName = fileName;
            final int finalGoogleType = googleType;
            mDriveServiceHelper.createFile(fileName, mimeType)
                    .addOnSuccessListener(fileId -> {
                        mOpenFileId = fileId;

                        mDriveServiceHelper.updatePermissions(fileId, mUsers)
                                .addOnSuccessListener((Void) ->
                                        Log.i(TAG, "Updated permissions"))
                                .addOnFailureListener(exception ->
                                        Log.i(TAG, "Failure to update permissions"));

                        FileExtended driveFile = new FileExtended();
                        driveFile.setFileName(finalFileName);
                        driveFile.setFileSize(finalGoogleType);

                        // Adds the new attached file to the Recycler View so the user knows the file is now
                        // attached to their event for upload
                        mFilesList.add(0, driveFile);
                        mFileViewAdapter.notifyItemInserted(0);
                        mProgressBar.setVisibility(View.INVISIBLE);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't create file.", exception));
        }
    }

    private void addNewUsers(List<ParseUser> newUsers) {
        for(ParseUser user : newUsers) {
            int i = 0;
            while(i < mEventUsers.size() && !user.getObjectId().equals(mEventUsers.get(i).getObjectId())) {
                i++;
            }
            if(i != mEventUsers.size()) {
                mNewEventUsers.add(user);
            }
        }
    }

    private void endFileSelection() {
        Fragment targetFragment = getTargetFragment();
        if(targetFragment != null) {
            // This case implementation handles the returning of the list of files that the user has
            // added to the event back to the create event fragment for upload of the event
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("uploadFiles", (ArrayList<? extends Parcelable>) mFilesList);

            String targetFragmentName = targetFragment.getClass().getSimpleName();

            if (targetFragmentName.equals(CreateEventFragment.class.getSimpleName())) {
                intent.putParcelableArrayListExtra("newUsers", (ArrayList<? extends Parcelable>) mNewEventUsers);
                getTargetFragment().onActivityResult(CreateEventFragment.FILE_UPLOAD_REQUEST_CODE, Activity.RESULT_OK, intent);
            } else {
                getTargetFragment().onActivityResult(DiscussionFragment.FILE_ADD_REQUEST_CODE, Activity.RESULT_OK, intent);
            }

            FragmentManager fm = getActivity().getSupportFragmentManager();
            // This is used so that the state of the previous create-event fragment is
            // not changed when we return to it
            fm.popBackStackImmediate();
        }
        Fragment fragment;
        Bundle data = new Bundle();
        if(mEvent != null){
            fragment = new ConfirmEventFragment();
            data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));
        } else {
            fragment = new AddEventsFragment();
            data.putParcelable(Group.class.getSimpleName(), Parcels.wrap(mGroup));
            if(getArguments().containsKey("groupImage")){
                data.putParcelable("groupImage", getArguments().getParcelable("groupImage"));
            }
        }
        fragment.setArguments(data);

        ((MainActivity) getContext()).getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                .add(R.id.frameLayoutContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
