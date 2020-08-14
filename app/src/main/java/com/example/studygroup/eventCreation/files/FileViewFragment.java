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
 * FileViewFragment is a subclass of {@link Fragment}. It handles the functionality of
 * allowing users to create Google Drive files, take and upload pictures using their
 * device's camera, and upload files from their device. Users can also see files they've
 * created or uploaded in a recycler view
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
        // Get the current fragment being displayed on the device (frameLayoutContainer is the view
        // always used for displaying fragments in MainActivity)
        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutContainer);
        String fragmentName = fragment.getClass().getSimpleName();

        // Make sure that the current displayed fragment is an instance of this class to make sure
        // that the user's taps on the menu items execute the code for these action in the
        // correct fragment
        if(fragmentName.equals(FileViewFragment.class.getSimpleName())) {
            switch(item.getItemId()) {
                case R.id.action_check:
                    if(mEvent != null) {
                        // If we are in the process of creating an event, we want to save
                        // the added users and added files specified by the user to the
                        // event Pars object
                        for (ParseUser user : mNewEventUsers) {
                            mEvent.addUnique("users", user);
                        }
                        for (FileExtended file : mFilesList) {
                            mEvent.addUnique("files", file);
                        }
                    } else {
                        // If we are in the process of creating a group, we want to save
                        // the added users and added files specified by the user to the
                        // event Pars object
                        for(ParseUser user : mNewEventUsers) {
                            mGroup.addUnique("users", user);
                        }
                        for(FileExtended file : mFilesList) {
                            mGroup.addUnique("files", file);
                        }
                    }
                    // Being the transition to the next fragment in the event or group
                    // UI flow
                    endFileSelection();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        else {
            // Returning false if this is not an instance of the fragment class being displayed
            // so the next fragment class can be checked to run the correct code of the action
            return false;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setting the action bar title
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Files");

        List<ParseUser> alreadySelectedUsers;
        // This if/else block determines if we are in the process of creating an event or
        // group by checking what arguments were passed to this fragment
        if(getArguments().containsKey(Group.class.getSimpleName())) {
            // If a group object was passed (designated by the Group class name as a key), then
            // we want to retrieve and open it and get its already selected users
            mGroup = Parcels.unwrap(getArguments().getParcelable(Group.class.getSimpleName()));
            alreadySelectedUsers = mGroup.getGroupUsers();
        }
        else {
            // Otherwise an event must have been passed (designated by the Event class name as a key),
            // then we want to retrieve and open it and get its already selected users
            mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));
            alreadySelectedUsers = mEvent.getUsers();
        }

        // List to hold all the files created (drive, picture, or uploaded) by the user
        mFilesList = new ArrayList<>();

        // Here we check to see if there are any already selected users attached to this event or
        // group object and add them if they exist
        mEventUsers = new ArrayList<>();
        if(alreadySelectedUsers != null) {
            mEventUsers.addAll(alreadySelectedUsers);
        }

        // Separate list to hold users added to the event or group in this fragment
        mNewEventUsers = new ArrayList<>();
        // Separate list to hold all users in general for this event or group
        mUsers = new ArrayList<>();

        mProgressBar = view.findViewById(R.id.fileViewProgressBar);
        RecyclerView mFileViewRecyclerView = view.findViewById(R.id.uploadedFilesRecyclerView);
        ImageButton mCreateDocImageButton = view.findViewById(R.id.googleDocsImageButton);
        ImageButton mTakePhotoImageButton = view.findViewById(R.id.takePhotoImageButton);
        ImageButton mUploadFilesImageButton = view.findViewById(R.id.uploadFilesImageButton);

        // Creating a listener to remove users when the user taps to uncheck the check box
        // that each added user item in user recycler views has
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

        // Adding a divider between user items for clearer UI design
        DividerItemDecoration itemDecor = new DividerItemDecoration(mFileViewRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mFileViewRecyclerView.addItemDecoration(itemDecor);

        // Adding listener to upload icon to launch the native Android file picker application
        // so that users can select files from their device to upload
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

        // Adding listener to camera icon to launch the user's device's camera to take a picture
        // and bring that picture into this app
        mTakePhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Launching camera activity");
                // Before launching the camera we need to ensure that the necessary permissions
                // are in place that state that this app can open and take pictures with the device's
                // camera. If not, we need to request those permissions first
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                } else {
                    onLaunchCamera();
                }
            }
        });

        // Adding listener to Google Drive icon to launch dialog to configure Google Drive file
        // creation
        mCreateDocImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Beginning to create new Google Doc for event");

                // Before launching the Google Drive file creation dialog, we need to ensure the
                // current user has a valid an authenticated Google account associated with it. We
                // also need to make sure that the user has given this app permission to create
                // files on their Google Drive on their behalf. If we do not have the permissions to
                // do this, we must request these Google permissions
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

        // Adding a key listener to the current fragment, which should be this one, to register
        // when the user taps the back button. We want to ensure this action leads the user to
        // a logical place
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
            // As long as the added users from the AddUser fragment exist, we want to
            // add them to the users list and new users list
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
            // If camera permissions were granted, launch the device's camera to allow users to
            // take pictures
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Camera Permission Granted");
                onLaunchCamera();
            } else {
                Log.i(TAG, "Camera Permission Denied");
            }
        }
        if(requestCode == RC_REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION) {
            // Check all requested permissions and, if all are granted, launch the dialog
            // to continue with creating Google Drive files on the user's behalf
            boolean permissionCheck = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    permissionCheck = false;
                    break;
                }
            }
            if(permissionCheck){
                gsuiteConfigureCreateDialog();
            }
        }
    }

    // This method handles the creation and launching of the dialog that allows users to configure
    // the Google Drive file they'd like to create and add to the event or group. This method also
    // largely handles that functionality
    private void gsuiteConfigureCreateDialog() {
        // Now that we have the necessary permissions to create Google Drive files on the user's
        // Google account, we need to communicate with the Google Drive API v3. As with all Google
        // APIs, this dialog needs to begin with authentication and authorization using the OAuth2
        // protocol. A credential is created for this account in order to request an access token to
        // access private data using the Google Drive API. Since we checked permissions already, the
        // access token should be granted and received in the background, allowing us to create a
        // Drive Service object that will facilitate further communication with the Google Drive API
        // for file creation in Java
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
        Account account = GoogleSignIn.getLastSignedInAccount(getContext()).getAccount();
        credential.setSelectedAccount(account);
        Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName("Study Group")
                .build();
        // Instantiating an instance of a helper class that assists in communicating with the Google
        // Drive API to create Google Drive files
        mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
        mUsersAdapter.clear();

        // Beginning the creation of the dialog and setting its title
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Drive File Creation");

        // Getting a reference to the layout file created and setting it as the dialog's layout to
        // guide the user in creating a Google Drive file from this app
        final View customDialogLayout = ((MainActivity) getContext()).getLayoutInflater().inflate(R.layout.gsuite_create_dialog_layout, null);
        builder.setView(customDialogLayout);

        // Getting references to the various components in our dialog layout view
        mFileTitleEditText = customDialogLayout.findViewById(R.id.dialogFileTitleEditText);
        CheckBox addAllEventUsersCheckBox = customDialogLayout.findViewById(R.id.addEventUsersCheckBox);
        Spinner typeSelectSpinner = customDialogLayout.findViewById(R.id.googleFileTypeSpinner);
        LinearLayout addOtherUsersLinearLayout = customDialogLayout.findViewById(R.id.addOtherUsersLinearLayout);

        addAllEventUsersCheckBox.setChecked(false);

        RecyclerView usersForShareRecyclerView = customDialogLayout.findViewById(R.id.usersForShareRecyclerView);
        usersForShareRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersForShareRecyclerView.setAdapter(mUsersAdapter);

        // Creating a list of options for Google Drive files the user can create that will be used
        // to populate the spinner
        List<String> fileTypeOptions = new ArrayList<String>();
        fileTypeOptions.add("Select File Type");
        fileTypeOptions.add("Doc");
        fileTypeOptions.add("Sheet");
        fileTypeOptions.add("Slide");

        int[] fileType = {0};

        // Initializing the spinner to load the correct options in a default format
        ArrayAdapter<String> fileTypeSpinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, fileTypeOptions);
        fileTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSelectSpinner.setAdapter(fileTypeSpinnerAdapter);
        typeSelectSpinner.setSelection(0);
        typeSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                // Changing the selected type every time the user taps on a different option in
                // the spinner menu
                fileType[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Setting a positive button on the dialog to handle creating the a Google Drive file given
        // the various configurations the user has elected throughout the dialog
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(fileType[0] == 0) {
                    Toast.makeText(getContext(), "Please Select a File Type", Toast.LENGTH_SHORT).show();
                    return;
                } else if(fileType[0] == 1) {
                    // Creating a Google Document file
                    createDriveFile("application/vnd.google-apps.document", -1);
                } else if(fileType[0] == 2) {
                    // Creating a Google Sheet file
                    createDriveFile("application/vnd.google-apps.spreadsheet", -2);
                } else {
                    // Creating a Google Slide file
                    createDriveFile("application/vnd.google-apps.presentation", -3);
                }
            }
        });

        // Adding listener on checkbox so that when the user checks the box, all already added event
        // or group users are added to users list for sharing the file to be created
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

        // Deploying the dialog
        mFileDialog = builder.create();
        mFileDialog.show();

        // Adding a listener to a linear layout that contains an add user button and text view so
        // that an Add Users fragment with an expected return result to allow the user to add
        // other users to the group or event and share the Google Drive file to
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

            // If the user has not set a file name, we just give the file we create a filename of
            // "Untitled"
            String fileName = mFileTitleEditText.getText().toString();
            if(fileName.isEmpty()) {
                fileName = "Untitled";
            }

            // Getting the final filename, must be final as it's used in an inner function
            final String finalFileName = fileName;
            final int finalGoogleType = googleType;
            mDriveServiceHelper.createFile(fileName, mimeType)
                    .addOnSuccessListener(fileId -> {
                        // Getting the file id for the newly-created Google Drive file
                        mOpenFileId = fileId;

                        // Calling a method within the helper class to share this newly created
                        // file with the users selected by the current user via permissions handling
                        // and communication with the Google Drive API
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
        } else {
            Fragment fragment;
            Bundle data = new Bundle();
            if (mEvent != null) {
                fragment = new ConfirmEventFragment();
                data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));
            } else {
                fragment = new AddEventsFragment();
                data.putParcelable(Group.class.getSimpleName(), Parcels.wrap(mGroup));
                if (getArguments().containsKey("groupImage")) {
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
}
