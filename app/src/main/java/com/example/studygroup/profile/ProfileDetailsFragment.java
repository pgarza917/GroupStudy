package com.example.studygroup.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventFeed.FeedFragment;
import com.example.studygroup.messaging.ConversationFragment;
import com.example.studygroup.models.Subject;
import com.example.studygroup.models.User;
import com.example.studygroup.search.SearchFragment;
import com.google.common.collect.Iterables;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileDetailsFragment extends Fragment {

    public static final String TAG = ProfileDetailsFragment.class.getSimpleName();

    private ImageView mUserPictureImageView;
    private TextView mUserNameTextView;
    private TextView mUserEmailTextView;
    private TextView mUserBioTextView;
    private TextView mUserSubjectsTextView;
    private RecyclerView mUserSubjectsRecyclerView;
    private ProgressBar mProgressBar;
    private ImageButton mFriendUserImageButton;
    private ImageButton mMessageUserImageButton;

    private List<Subject> mUserSubjects;
    private SubjectAdapter mSubjectsAdapter;
    private ParseUser mUser;

    public ProfileDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SearchFragment();
                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutContainer, fragment)
                        .commit();
            }
        });

        mUserSubjects = new ArrayList<>();
        mSubjectsAdapter = new SubjectAdapter(getContext(), mUserSubjects, null);
        mUser = Parcels.unwrap(getArguments().getParcelable(ParseUser.class.getSimpleName()));

        mUserPictureImageView = view.findViewById(R.id.profileDetailsPictureImageView);
        mUserNameTextView = view.findViewById(R.id.profileDetailsNameTextView);
        mUserEmailTextView = view.findViewById(R.id.profileDetailsEmailTextView);
        mUserBioTextView = view.findViewById(R.id.profileDetailsBioTextView);
        mUserSubjectsRecyclerView = view.findViewById(R.id.userSubjectTagsRecyclerView);
        mUserSubjectsTextView = view.findViewById(R.id.userSubjectTagsTextView);
        mProgressBar = view.findViewById(R.id.profileDetailsProgressBar);
        mFriendUserImageButton = view.findViewById(R.id.friendUserImageButton);
        mMessageUserImageButton = view.findViewById(R.id.messageUserImageButton);

        mUserSubjectsRecyclerView.setAdapter(mSubjectsAdapter);
        mUserSubjectsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mProgressBar.setVisibility(View.VISIBLE);

        mUserNameTextView.setText(mUser.getString("displayName"));
        mUserEmailTextView.setText(mUser.getString("openEmail"));
        mUserBioTextView.setText(mUser.getString("bio"));

        String subjectsTextViewText = mUser.getString("displayName") + "'s Tags";
        mUserSubjectsTextView.setText(subjectsTextViewText);

        ParseFile userPicture = mUser.getParseFile("profileImage");
        Glide.with(getContext()).load(userPicture.getUrl()).circleCrop().into(mUserPictureImageView);

        mMessageUserImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchNewMessageDialog();
            }
        });

        queryUserSubjects();
    }

    private void launchNewMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Would you like to message " + mUser.getString("displayName") + "?");

        builder.setNegativeButton("No", null);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                launchConversation();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }

    private void launchConversation() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        String email = mUser.getString("openEmail");
        Query query = reference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot snapshot = Iterables.get(dataSnapshot.getChildren(), 0);

                User user = snapshot.getValue(User.class);
                String uid = user.getId();

                Fragment fragment = new ConversationFragment();
                Bundle data = new Bundle();
                data.putString("userId", uid);
                fragment.setArguments(data);
                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.frameLayoutContainer, fragment, ConversationFragment.class.getSimpleName())
                        .commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error querying selected user: ", error.toException());
            }
        });
    }

    private void queryUserSubjects() {
        ParseRelation<Subject> subjectsRelation = mUser.getRelation("subjectInterests");

        subjectsRelation.getQuery().findInBackground(new FindCallback<Subject>() {
            @Override
            public void done(List<Subject> subjects, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error querying Parse for user subjects: ", e);
                    return;
                }
                mUserSubjects.clear();
                mUserSubjects.addAll(subjects);
                mSubjectsAdapter.notifyDataSetChanged();

                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}