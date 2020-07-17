package com.example.studygroup.eventCreation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.adapters.FileViewAdapter;
import com.example.studygroup.models.FileExtended;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileViewFragment extends Fragment {

    public static final int FILE_PICKER_REQUEST_CODE = 1940;

    private FileViewAdapter mAdapter;
    private List<FileExtended> mFilesList;

    private RecyclerView mFileViewRecyclerView;
    private ImageButton mCreateDocImageButton;
    private ImageButton mTakePhotoImageButton;
    private ImageButton mUploadFilesImageButton;

    public FileViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFilesList = new ArrayList<>();
        mFileViewRecyclerView = view.findViewById(R.id.uploadedFilesRecyclerView);
        mCreateDocImageButton = view.findViewById(R.id.googleDocsImageButton);
        mTakePhotoImageButton = view.findViewById(R.id.takePhotoImageButton);
        mUploadFilesImageButton = view.findViewById(R.id.uploadFilesImageButton);

        mAdapter = new FileViewAdapter(getContext(), mFilesList);
        mFileViewRecyclerView.setAdapter(mAdapter);

        mFileViewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUploadFilesImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, FILE_PICKER_REQUEST_CODE);
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
            Uri uri = data.getData();
            ContentResolver contentResolver = getContext().getContentResolver();

            String filename = queryName(contentResolver, uri);
            File fileToUpload = createTempFile(filename);
            fileToUpload = saveContentToFile(uri, fileToUpload, contentResolver);

            ParseFile file = new ParseFile(fileToUpload, filename);
            long fileSize = fileToUpload.length();
            saveFileToParse(file, filename, fileSize);
        }
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor = resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    private File createTempFile(String name) {
        File file = null;
        try {
            file = File.createTempFile(name, null, getContext().getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private File saveContentToFile(Uri uri, File file, ContentResolver contentResolver) {
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


    public void saveFileToParse(ParseFile fileData, String filename, long fileSize) {
        FileExtended file = new FileExtended();

        file.setFile(fileData);
        file.setFileName(filename);
        file.setFileSize(fileSize);

        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Toast.makeText(getContext(), "Error uploading file!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
