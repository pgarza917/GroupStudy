package com.example.studygroup.eventCreation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.studygroup.R;
import com.example.studygroup.adapters.FileViewAdapter;
import com.example.studygroup.models.FileExtended;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileViewFragment extends Fragment {

    private FileViewAdapter mAdapter;
    private List<FileExtended> mFilesList;

    private RecyclerView mFileViewRecyclerView;

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

        mAdapter = new FileViewAdapter(getContext(), mFilesList);
        mFileViewRecyclerView.setAdapter(mAdapter);

        mFileViewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}