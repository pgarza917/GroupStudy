package com.example.studygroup.eventFeed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.studygroup.R;
import com.example.studygroup.eventCreation.files.FileViewAdapter;
import com.example.studygroup.eventCreation.files.FileViewFragment;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventFilesFragment extends Fragment {

    public static final String TAG = EventFilesFragment.class.getSimpleName();

    private RecyclerView mFilesRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mNoFilesTextView;

    private List<FileExtended> mFilesList;
    private FileViewAdapter mFilesAdapter;
    private Event mEvent;

    public EventFilesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));

        mFilesList = new ArrayList<>();
        mFilesAdapter = new FileViewAdapter(getContext(), mFilesList);

        mFilesRecyclerView = view.findViewById(R.id.eventFilesRecyclerView);
        mProgressBar = view.findViewById(R.id.eventFilesProgressBar);
        mNoFilesTextView = view.findViewById(R.id.noFilesTextView);

        mFilesRecyclerView.setAdapter(mFilesAdapter);
        mFilesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<FileExtended> files = mEvent.getFiles();
        if(files != null) {
            mFilesList.clear();
            mFilesList.addAll(files);
            mFilesAdapter.notifyDataSetChanged();
        } else {
            mNoFilesTextView.setVisibility(View.VISIBLE);
        }
    }

}