package com.example.studygroup.eventCreation;

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
import android.widget.EditText;
import android.widget.Toast;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.dateTime.DateTimeAndPrivacyFragment;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.Subject;
import com.example.studygroup.profile.SubjectAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TitleDescAndSubjectFragment extends Fragment {

    public static final String TAG = TitleDescAndSubjectFragment.class.getSimpleName();

    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private RecyclerView mSubjectsRecyclerView;
    private RecyclerView mSelectedSubjectRecyclerView;

    private List<Subject> mAllSubjectsList;
    private List<Subject> mSelectedSubjectList;
    private SubjectAdapter mAllSubjectsAdapter;
    private SubjectAdapter mSelectedSubjectAdapter;
    private Event mEvent;

    public TitleDescAndSubjectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_title_desc_and_subject, container, false);
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

        if(fragmentName.equals(TitleDescAndSubjectFragment.class.getSimpleName())) {
            if (item.getItemId() == R.id.action_check) {
                if (!saveEventChanges(mTitleEditText.getText().toString(), mDescriptionEditText.getText().toString(), mSelectedSubjectList)) {
                    return false;
                }

                Fragment fragment = new DateTimeAndPrivacyFragment();
                Bundle data = new Bundle();
                data.putParcelable(Event.class.getSimpleName(), Parcels.wrap(mEvent));
                fragment.setArguments(data);

                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .add(R.id.frameLayoutContainer, fragment)
                        .addToBackStack(null)
                        .commit();

                return true;
            }
            return super.onOptionsItemSelected(item);
        } else {
            return false;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEvent = Parcels.unwrap(getArguments().getParcelable(Event.class.getSimpleName()));

        mTitleEditText = view.findViewById(R.id.titleEditText);
        mDescriptionEditText = view.findViewById(R.id.descriptionEditText);
        mSubjectsRecyclerView = view.findViewById(R.id.allSubjectsRecyclerView);
        mSelectedSubjectRecyclerView = view.findViewById(R.id.selectedSubjectRecyclerView);

        mAllSubjectsList = new ArrayList<>();
        mSelectedSubjectList = new ArrayList<>();

        mAllSubjectsAdapter = new SubjectAdapter(getContext(), mAllSubjectsList, new SubjectAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                Subject subject = mAllSubjectsList.get(position);

                mSelectedSubjectList.clear();
                mSelectedSubjectList.add(0, subject);
                mSelectedSubjectAdapter.notifyDataSetChanged();
            }
        });
        mSelectedSubjectAdapter = new SubjectAdapter(getContext(), mSelectedSubjectList, new SubjectAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                // Nothing should happen here
            }
        });

        mSubjectsRecyclerView.setAdapter(mAllSubjectsAdapter);
        mSubjectsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mSelectedSubjectRecyclerView.setAdapter(mSelectedSubjectAdapter);
        mSelectedSubjectRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        queryAllSubjects();
    }

    private void queryAllSubjects() {
        ParseQuery<Subject> subjectsQuery = ParseQuery.getQuery(Subject.class);
        subjectsQuery.orderByAscending("subjectName");

        subjectsQuery.findInBackground(new FindCallback<Subject>() {
            @Override
            public void done(List<Subject> subjects, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error querying Parse for all subjects: ", e);
                    return;
                }
                mAllSubjectsList.clear();
                mAllSubjectsList.addAll(subjects);
                mAllSubjectsAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean saveEventChanges(String title, String description, List<Subject> selectedSubjectList) {
        if(title == null || title.isEmpty()) {
            Toast.makeText(getContext(), "Please Enter a Title!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(description == null || description.isEmpty()) {
            Toast.makeText(getContext(), "Please Enter a Description!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(selectedSubjectList == null || selectedSubjectList.isEmpty()) {
            Toast.makeText(getContext(), "Please Select a Subject!", Toast.LENGTH_SHORT).show();
            return false;
        }

        mEvent.setTitle(title);
        mEvent.setDescription(description);
        mEvent.put("subject", ParseObject.createWithoutData("Subject", selectedSubjectList.get(0).getObjectId()));

        return true;
    }
}