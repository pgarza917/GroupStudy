package com.example.studygroup.groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.users.UsersAdapter;
import com.example.studygroup.models.Group;
import com.example.studygroup.search.UserSearchResultAdapter;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupDetailsFragment extends Fragment {

    public GroupDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Group group = Parcels.unwrap(getArguments().getParcelable(Group.class.getSimpleName()));

        ImageView GroupPictureImageView = view.findViewById(R.id.groupDetailsPictureImageView);
        TextView GroupDescriptionTextView = view.findViewById(R.id.groupDescriptionTextView);
        TextView GroupNumberOfUsersTextView = view.findViewById(R.id.groupNumberOfUsersTextView);
        TextView GroupPrivacyTextView = view.findViewById(R.id.groupPrivacyTextView);
        RecyclerView GroupUsersRecyclerView = view.findViewById(R.id.groupUsersRecyclerView);

        List<ParseUser> groupUsers = new ArrayList<>();
        if(group.getGroupUsers() != null) {
            groupUsers.addAll(group.getGroupUsers());
        }

        UserSearchResultAdapter usersAdapter = new UserSearchResultAdapter(getContext(), groupUsers, null);

        GroupUsersRecyclerView.setAdapter(usersAdapter);
        GroupUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        GroupDescriptionTextView.setText(group.getGroupDescription());

        int numberOfUsers = group.getNumberOfUsers();
        String numUsersStr = numberOfUsers + ((numberOfUsers == 1) ? " User" : " Users");
        GroupNumberOfUsersTextView.setText(numUsersStr);

        String privacyStr = group.getGroupPrivacy() + " Group";
        GroupPrivacyTextView.setText(privacyStr);

        ParseFile groupPicture = group.getGroupImage();
        if(groupPicture != null) {
            Glide.with(getContext())
                    .load(groupPicture.getUrl())
                    .circleCrop()
                    .into(GroupPictureImageView);
        }
    }
}