package com.example.studygroup.eventCreation;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.studygroup.DriveServiceHelper;
import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.adapters.FileViewAdapter;
import com.example.studygroup.models.FileExtended;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.parse.ParseFile;

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

    private FileViewAdapter mAdapter;
    private List<FileExtended> mFilesList;
    private DriveServiceHelper mDriveServiceHelper;
    private String mOpenFileId;

    private RecyclerView mFileViewRecyclerView;
    private ImageButton mCreateDocImageButton;
    private ImageButton mTakePhotoImageButton;
    private ImageButton mUploadFilesImageButton;
    private EditText mFileTitleEditText;
    private ProgressBar mDriveProgressBar;

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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_search:
                // This case implementation handles the returning of the list of files that the user has
                // added to the event back to the create event fragment for upload of the event
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("uploadFiles", (ArrayList<? extends Parcelable>) mFilesList);
                getTargetFragment().onActivityResult(CreateEventFragment.FILE_UPLOAD_REQUEST_CODE, Activity.RESULT_OK, intent);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                // This is used so that the state of the previous create-event fragment is
                // not changed when we return to it
                fm.popBackStackImmediate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle data = getArguments();
        List<FileExtended> alreadyAttachedFiles = data.getParcelableArrayList("filesAttached");
        if(alreadyAttachedFiles != null && !alreadyAttachedFiles.isEmpty()) {
            mFilesList.addAll(alreadyAttachedFiles);
        } else {
            mFilesList = new ArrayList<>();
        }
        mFileViewRecyclerView = view.findViewById(R.id.uploadedFilesRecyclerView);
        mCreateDocImageButton = view.findViewById(R.id.googleDocsImageButton);
        mTakePhotoImageButton = view.findViewById(R.id.takePhotoImageButton);
        mUploadFilesImageButton = view.findViewById(R.id.uploadFilesImageButton);

        mAdapter = new FileViewAdapter(getContext(), mFilesList);
        mFileViewRecyclerView.setAdapter(mAdapter);

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
                onLaunchCamera(view);
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
        mDriveProgressBar = customDialogLayout.findViewById(R.id.progressBarDriveLoading);
        CheckBox addAllEventUsersCheckBox = customDialogLayout.findViewById(R.id.addEventUsersCheckBox);
        ImageButton addOtherUsersImageButton = customDialogLayout.findViewById(R.id.addOtherUsersImageButton);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDriveProgressBar.setVisibility(View.VISIBLE);
                createDriveDoc();
            }
        });

        addOtherUsersImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
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
            mAdapter.notifyItemInserted(0);
        }
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
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
                mAdapter.notifyItemInserted(0);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == RC_REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION) {
            gsuiteConfigureCreateDialog();
        }
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

    /**
     * Creates a new file via the Drive REST API.
     */
    private void createDriveDoc() {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Creating a file.");

            String fileName = mFileTitleEditText.getText().toString();

            mDriveServiceHelper.createDoc(fileName)
                    .addOnSuccessListener(fileId -> {
                        mOpenFileId = fileId;
                        mDriveProgressBar.setVisibility(View.INVISIBLE);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't create file.", exception));
        }
    }

    /**
     * Retrieves the title and content of a file identified by {@code fileId} and populates the UI.
     */
    private void readFile(String fileId) {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Reading file " + fileId);

            mDriveServiceHelper.readFile(fileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;

                        mFileTitleEditText.setText(name);
                        //mDocContentEditText.setText(content);

                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't read file.", exception));
        }
    }

    /**
     * Saves the currently opened file created if one exists.
     */
    private void saveFile() {
        if (mDriveServiceHelper != null && mOpenFileId != null) {
            Log.i(TAG, "Saving " + mOpenFileId);

            String fileName = mFileTitleEditText.getText().toString();

            mDriveServiceHelper.saveFile(mOpenFileId, fileName)
                    .addOnSuccessListener((Void) ->
                            Toast.makeText(getContext(), "Drive File Created!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Unable to save file via REST.", exception));
        }
    }
}
