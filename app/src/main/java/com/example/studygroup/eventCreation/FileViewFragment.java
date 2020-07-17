package com.example.studygroup.eventCreation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            String src = uri.getPath();
            String filename = uri.getLastPathSegment();

            File source = new File(src);
            long fileSize = source.length();
            byte[] fileBytes = readFileToByteArray(source);

            ParseFile file = new ParseFile(filename, fileBytes);

            saveFileToParse(file, filename, fileSize);
        }
    }

    // This method uses java.io.FileInputStream to read file content into a byte array
    private static byte[] readFileToByteArray(File file){
        FileInputStream fis = null;
        // Creating a byte array using the length of the file
        // file.length returns long which is cast to int
        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();
        }catch(IOException ioExp){
            ioExp.printStackTrace();
        }
        return bArray;
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
