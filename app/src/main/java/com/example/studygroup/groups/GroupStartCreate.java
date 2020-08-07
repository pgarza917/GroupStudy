package com.example.studygroup.groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import com.example.studygroup.R;
import com.example.studygroup.models.Group;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupStartCreate extends Fragment {

    public static final String TAG = GroupStartCreate.class.getSimpleName();

    private ImageButton mGroupPictureImageButton;
    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private Switch mPrivacySwitch;

    private Group mGroup;
    private boolean mIsOpen = false;

    public GroupStartCreate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_start_create, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutContainer);
        String fragmentName = fragment.getClass().getSimpleName();

        if(fragmentName.equals(TAG)) {
            if(item.getItemId() == R.id.action_check) {
                saveGroupChanges();

                return true;
            }
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGroup = new Group();

        mGroupPictureImageButton = view.findViewById(R.id.groupCreatePictureImageButton);
        mNameEditText = view.findViewById(R.id.groupCreateNameEditText);
        mDescriptionEditText = view.findViewById(R.id.groupCreateDescriptionEditText);
        mPrivacySwitch = view.findViewById(R.id.groupCreatePrivacySwitch);

        mPrivacySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsOpen = b;
            }
        });
    }

    private void saveGroupChanges() {
        String groupName = mNameEditText.getText().toString();
        String groupDescription = mDescriptionEditText.getText().toString();

        mGroup.setGroupName(groupName);
        mGroup.setGroupDescription(groupDescription);

        if(mIsOpen) {
            mGroup.setGroupPrivacy("Open");
        } else {
            mGroup.setGroupPrivacy("Closed");
        }
    }
}