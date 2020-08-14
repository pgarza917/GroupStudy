package com.example.studygroup.groups;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.models.Group;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.parceler.Parcels;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupDetailsRootFragment extends Fragment {

    public static final String TAG = GroupDetailsRootFragment.class.getSimpleName();

    private Group mGroup;

    public GroupDetailsRootFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_details_root, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_open_group_chat) {
            if(mGroup.hasChat()) {
                openGroupChat();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Would you like to create a chat for this group?");
                builder.setNegativeButton("No", null);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createGroupInFireBase();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new GroupListFragment();
                ((MainActivity) getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.frameLayoutContainer, fragment)
                        .commit();
            }
        });

        ViewPager2 mViewPager = view.findViewById(R.id.groupDetailsViewPager);
        TabLayout mTabLayout = view.findViewById(R.id.groupDetailsTabLayout);

        mGroup = Parcels.unwrap(getArguments().getParcelable(Group.class.getSimpleName()));
        Log.i(TAG, "Received Bundled Group Data!");

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(mGroup.getGroupName());

        mViewPager.setAdapter(new GroupDetailsViewPagerAdapter(getActivity(), mGroup));
        new TabLayoutMediator(mTabLayout, mViewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if(position == 0) {
                            tab.setText("Posts");
                        } else if(position == 1) {
                            tab.setText("Events");
                        } else if(position == 2) {
                            tab.setText("Files");
                        } else {
                            tab.setText("Details");
                        }
                    }
                }).attach();
    }

    private void createGroupInFireBase() {
        String id = "" + System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", "" + id);
        hashMap.put("name", mGroup.getGroupName());
        hashMap.put("icon", mGroup.getGroupImage().getUrl());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(id).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Group created in Firebase successfully");
                        HashMap<String, String> userMap = new HashMap<>();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error creating Group in Firebase: ", e);
                    }
                });
    }

    private void openGroupChat() {

    }
}