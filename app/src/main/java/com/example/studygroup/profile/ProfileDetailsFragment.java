package com.example.studygroup.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventFeed.FeedFragment;
import com.example.studygroup.search.SearchFragment;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileDetailsFragment extends Fragment {

    private ImageView mUserPictureImageView;
    private TextView mUserNameTextView;
    private TextView mUserEmailTextView;
    private TextView mUserBioTextView;

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



        mUser = Parcels.unwrap(getArguments().getParcelable(ParseUser.class.getSimpleName()));

        mUserPictureImageView = view.findViewById(R.id.profileDetailsPictureImageView);
        mUserNameTextView = view.findViewById(R.id.profileDetailsNameTextView);
        mUserEmailTextView = view.findViewById(R.id.profileDetailsEmailTextView);
        mUserBioTextView = view.findViewById(R.id.profileDetailsBioTextView);

        mUserNameTextView.setText(mUser.getString("displayName"));
        mUserEmailTextView.setText(mUser.getString("openEmail"));
        mUserBioTextView.setText(mUser.getString("bio"));

        ParseFile userPicture = mUser.getParseFile("profileImage");

        Glide.with(getContext()).load(userPicture.getUrl()).circleCrop().into(mUserPictureImageView);

    }
}