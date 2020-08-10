package com.example.studygroup.groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.eventCreation.users.AddUsersFragment;
import com.example.studygroup.models.Group;

import org.parceler.Parcels;

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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.create_event_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutContainer);
        String fragmentName = currentFragment.getClass().getSimpleName();

        if(fragmentName.equals(TAG)) {
            if(item.getItemId() == R.id.action_check) {
                saveGroupChanges();

                Fragment fragment = new AddUsersFragment();
                Bundle data = new Bundle();
                data.putParcelable(Group.class.getSimpleName(), Parcels.wrap(mGroup));
                fragment.setArguments(data);
                fragment.setTargetFragment(currentFragment, 100);
                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .add(R.id.frameLayoutContainer, fragment)
                        .addToBackStack(null)
                        .commit();

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